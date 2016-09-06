package ro.szzsa.utils.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Used to convert objects from/to Json.
 */
public class JsonConverter implements Converter {

  private Gson gson;

  public JsonConverter() {
    gson = new GsonBuilder().create();
  }

  @Override
  public String toString(Object object) throws ConverterException {
    try {
      return gson.toJson(object);
    } catch (JsonParseException e) {
      throw new ConverterException(e);
    }
  }

  @Override
  public <T> T fromString(String json, Class<T> type) throws ConverterException {
    try {
      return gson.fromJson(json, type);
    } catch (JsonParseException e) {
      throw new ConverterException(e);
    }
  }
}
