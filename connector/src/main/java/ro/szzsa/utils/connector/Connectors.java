package ro.szzsa.utils.connector;

/**
 * Factory methods for {@link Connector} instances.
 */
public final class Connectors {

  private Connectors() {
    throw new UnsupportedOperationException();
  }

  public static Connector createDefaultHttpConnector() {
    return new HttpConnector();
  }

  public static HttpConnectorBuilder customHttpConnector() {
    return new HttpConnectorBuilder();
  }
}
