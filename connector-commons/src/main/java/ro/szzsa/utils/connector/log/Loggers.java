package ro.szzsa.utils.connector.log;

/**
 * Factory methods for {@link Logger} instances.
 */
public final class Loggers {

  private Loggers() {
    throw new UnsupportedOperationException();
  }

  public static Logger createEmptyLogger() {
    return new EmptyLogger();
  }
}
