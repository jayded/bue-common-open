package com.bbn.bue.common.serialization.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JacksonSerializer {

  private final ObjectMapper mapper;

  private JacksonSerializer(ObjectMapper mapper) {
    this.mapper = checkNotNull(mapper);
  }

  private static ObjectMapper mapperFromJSONFactory(JsonFactory jsonFactory) {
    final ObjectMapper mapper = new ObjectMapper(jsonFactory);
    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    mapper.findAndRegisterModules();
    return mapper;
  }

  public static JacksonSerializer forNormalJSON() {
    return new JacksonSerializer(mapperFromJSONFactory(new JsonFactory()));
  }

  public static JacksonSerializer forPrettyJSON() {
    final ObjectMapper ret = mapperFromJSONFactory(new JsonFactory());
    ret.enable(SerializationFeature.INDENT_OUTPUT);
    return new JacksonSerializer(ret);
  }

  private static JacksonSerializer normalJSONCached;

  public static JacksonSerializer forNormalJSONUncached() {
    if (normalJSONCached == null) {
      normalJSONCached = forNormalJSONUncached();
    }
    return normalJSONCached;
  }

  private static JacksonSerializer smileCached;

  public static JacksonSerializer forSmile() {
    if (smileCached == null) {
      smileCached = forSmileUncached();
    }
    return smileCached;
  }

  public static JacksonSerializer forSmileUncached() {
    return new JacksonSerializer(mapperFromJSONFactory(new SmileFactory()));
  }


  public void serializeTo(final Object o, final ByteSink out) throws IOException {
    final RootObject rootObj = RootObject.forObject(o);
    final OutputStream bufStream = out.openBufferedStream();
    mapper.writeValue(bufStream, rootObj);
    bufStream.close();
  }

  public Object deserializeFrom(final ByteSource source) throws IOException {
    final InputStream srcStream = source.openStream();
    final RootObject rootObj = mapper.readValue(srcStream, RootObject.class);
    srcStream.close();
    return rootObj.object();
  }

  public String writeValueAsString(Object value) throws JsonProcessingException {
    return mapper.writeValueAsString(value);
  }

  public <T> T deserializeFromString(String content, Class<T> valueType) throws IOException {
    return mapper.readValue(content, valueType);
  }

  private static final class RootObject {

    @JsonCreator
    public RootObject(@JsonProperty("obj") final Object obj) {
      this.obj = checkNotNull(obj);
    }

    public static RootObject forObject(final Object obj) {
      return new RootObject(obj);
    }

    @JsonProperty("obj")
    public Object object() {
      return obj;
    }

    private final Object obj;
  }
}
