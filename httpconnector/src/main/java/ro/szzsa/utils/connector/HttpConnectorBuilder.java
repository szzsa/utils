package ro.szzsa.utils.connector;

import ro.szzsa.utils.connector.log.Logger;

/**
 *
 */
public final class HttpConnectorBuilder {

  private final HttpConnector connector;

  public HttpConnectorBuilder() {
    connector = new HttpConnector();
  }

  public HttpConnectorBuilder(String username, String password) {
    connector = new HttpConnector();
    connector.setUsername(username);
    connector.setPassword(password);
  }

  public HttpConnectorBuilder(String apiKey) {
    connector = new HttpConnector();
    connector.setApiKey(apiKey);
  }

  public HttpConnectorBuilder setSocketTimeout(int socketTimeout) {
    connector.setSocketTimeout(socketTimeout);
    return this;
  }

  public HttpConnectorBuilder setConnectionTimeout(int connectionTimeout) {
    connector.setConnectionTimeout(connectionTimeout);
    return this;
  }

  public HttpConnectorBuilder setNumberOfRetries(int numberOfRetries) {
    connector.setNumberOfRetries(numberOfRetries);
    return this;
  }

  public HttpConnectorBuilder setLogger(Logger logger) {
    connector.setLogger(logger);
    return this;
  }

  public HttpConnector build() {
    return connector;
  }
}
