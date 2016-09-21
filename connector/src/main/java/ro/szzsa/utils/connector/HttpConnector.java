package ro.szzsa.utils.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

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
      try (CloseableHttpClient httpclient = buildHttpClient(request.getUrl().startsWith("https"));
           CloseableHttpResponse httpResponse = httpclient.execute(buildHttpPost(request))) {
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
      }
    }
    throw new ConnectorException("Cannot connect to " + request.getUrl(), exception);
  }

  private CloseableHttpClient buildHttpClient(boolean isSecure)
    throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
    HttpClientBuilder httpClientBuilder = HttpClients.custom();
    if (isSecure) {
      SSLContext sslContext = new SSLContextBuilder()
        .loadTrustMaterial((TrustStrategy) (chain, authType) -> true)
        .build();
      HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
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
    try (InputStream inputStream = entity.getContent()) {
      StringWriter writer = new StringWriter();
      IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
      response = writer.toString();
      EntityUtils.consume(entity);
    } catch (IOException e) {
      throw new ConnectorException(e);
    }
    return response;
  }

  private HttpPost buildHttpPost(Request request) throws UnsupportedEncodingException {
    HttpPost httpPost = new HttpPost(request.getUrl());
    Header contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=" + StandardCharsets.UTF_8);
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
      StringEntity entity = new StringEntity(request.getMessage(), StandardCharsets.UTF_8);
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
