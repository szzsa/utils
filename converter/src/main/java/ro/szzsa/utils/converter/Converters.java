package ro.szzsa.utils.converter;

/**
 * Factory methods for {@link Converter} instances.
 */
public final class Converters {

  private Converters() {
    throw new UnsupportedOperationException();
  }

  public static Converter createJsonConverter() {
    return new JsonConverter();
  }
}
