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

  public HttpConnectorBuilder setApiKey(String key) {
    connector.setApiKey(key);
    return this;
  }

  public HttpConnectorBuilder setUsername(String username) {
    connector.setUsername(username);
    return this;
  }

  public HttpConnectorBuilder setPassword(String password) {
    connector.setPassword(password);
    return this;
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
