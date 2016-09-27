package ro.szzsa.utils.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.commons.io.IOUtils;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.auth.UsernamePasswordCredentials;
import cz.msebera.android.httpclient.client.CredentialsProvider;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.BasicCredentialsProvider;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.ssl.SSLContextBuilder;
import cz.msebera.android.httpclient.ssl.TrustStrategy;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.szzsa.utils.connector.exception.ConnectorException;
import ro.szzsa.utils.connector.log.Logger;
import ro.szzsa.utils.connector.log.Loggers;

public class HttpConnector implements Connector {

  private int socketTimeout = 5000;

  private int connectionTimeout = 3000;

  private int numberOfRetries = 4;

  private Logger logger = Loggers.createEmptyLogger();

  private String username;

  private String password;

  private String apiKey;

  @Override
  public String sendRequest(Request request) throws ConnectorException {
    int currentRetries = 0;
    Throwable exception = null;
    logger.debug("|---> Sending request to " + request.getUrl() + "\n" +
      (request.getMessage() == null ? "" : request.getMessage()));
    while (currentRetries <= numberOfRetries) {
      CloseableHttpClient httpclient = null;
      CloseableHttpResponse httpResponse = null;
      try {
        httpclient = buildHttpClient(request.getUrl().startsWith("https"));
        httpResponse = httpclient.execute(buildHttpPost(request));
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK == statusCode) {
          String response = getResponseMessage(httpResponse.getEntity());
          logger.debug("|<--- Received response from " + request.getUrl() + "\n" + response);
          return response;
        } else {
          throw new ConnectorException("Status code: " + statusCode);
        }
      } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | IOException e) {
        currentRetries++;
        logger.warn("Cannot connect to " + request.getUrl());
        exception = e;
        if (currentRetries <= numberOfRetries) {
          logger.debug("retrying...");
        }
      } finally {
        try {
          if (httpclient != null) {
            httpclient.close();
          }
          if (httpResponse != null) {
            httpResponse.close();
          }
        } catch (IOException e) {
          throw new ConnectorException("Cannot close resources", e);
        }
      }
    }
    throw new ConnectorException("Cannot connect to " + request.getUrl(), exception);
  }

  private CloseableHttpClient buildHttpClient(boolean isSecure)
    throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
    HttpClientBuilder httpClientBuilder = HttpClients.custom();
    if (isSecure) {
      SSLContext sslContext = new SSLContextBuilder()
        .loadTrustMaterial(new TrustStrategy() {
          @Override
          public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
          }
        })
        .build();
      HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
          return true;
        }
      };
      SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
      httpClientBuilder.setSSLSocketFactory(socketFactory);
    }

    if (username != null) {
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }

    return httpClientBuilder.build();
  }

  private String getResponseMessage(HttpEntity entity) throws ConnectorException {
    String response;
    InputStream inputStream = null;
    try {
      inputStream = entity.getContent();
      StringWriter writer = new StringWriter();
      IOUtils.copy(inputStream, writer, "utf-8");
      response = writer.toString();
      EntityUtils.consume(entity);
    } catch (IOException e) {
      throw new ConnectorException(e);
    } finally {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException e) {
        throw new ConnectorException("Cannot close input stream", e);
      }
    }
    return response;
  }

  private HttpPost buildHttpPost(Request request) throws UnsupportedEncodingException {
    HttpPost httpPost = new HttpPost(request.getUrl());
    Header contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
    httpPost.addHeader(contentTypeHeader);
    if (apiKey != null) {
      Header apiKeyAuthHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "key=" + apiKey);
      httpPost.addHeader(apiKeyAuthHeader);
    }
    RequestConfig config = RequestConfig.custom()
      .setSocketTimeout(socketTimeout)
      .setConnectTimeout(connectionTimeout)
      .build();
    httpPost.setConfig(config);
    if (request.getMessage() != null) {
      StringEntity entity = new StringEntity(request.getMessage(), "utf-8");
      entity.setContentType(contentTypeHeader);
      httpPost.setEntity(entity);
    }
    return httpPost;
  }

  public void setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public void setNumberOfRetries(int numberOfRetries) {
    this.numberOfRetries = numberOfRetries;
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }
}
