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

  public void setUsername(String username) {
    connector.setUsername(username);
  }

  public void setPassword(String password) {
    connector.setPassword(password);
  }

  public void setSocketTimeout(int socketTimeout) {
    connector.setSocketTimeout(socketTimeout);
  }

  public void setConnectionTimeout(int connectionTimeout) {
    connector.setConnectionTimeout(connectionTimeout);
  }

  public void setNumberOfRetries(int numberOfRetries) {
    connector.setNumberOfRetries(numberOfRetries);
  }

  public void setLogger(Logger logger) {
    connector.setLogger(logger);
  }

  public HttpConnector build() {
    return connector;
  }
}
