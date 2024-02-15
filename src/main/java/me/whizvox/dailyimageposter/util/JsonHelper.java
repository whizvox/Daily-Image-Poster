package me.whizvox.dailyimageposter.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.OutputStream;

public class JsonHelper {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
  }

  public static <T> T read(String in, Class<T> cls) {
    try {
      return OBJECT_MAPPER.readValue(in, cls);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  public static void write(OutputStream out, Object value) throws IOException {
    OBJECT_MAPPER.writeValue(out, value);
  }

}
