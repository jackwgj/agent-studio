package com.openjiuwen.studio.agent.space.common.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JacksonUtils {
    private final ObjectMapper OBJECT_MAPPER;

    private final ObjectMapper UNFORMATTED_OBJECT_MAPPER;

    private JacksonUtils() {
        OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        UNFORMATTED_OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        // 反序列化时 遇到未知属性不引起结果失败
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OBJECT_MAPPER.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(String.class, ToStringDeserializer.INSTANCE);
        OBJECT_MAPPER.registerModule(simpleModule);
    }

    /**
     * 获取单例对象，以便访问对象属性
     *
     * @return JacksonUtils
     */
    public static JacksonUtils getInstance() {
        return Singleton.INSTANCE.getInstance();
    }

    /**
     * Java Bean 转换 Json 字符串
     *
     * @param obj v
     * @return v
     */
    public static <T> String obj2json(T obj) {
        try {
            return getInstance().OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JacksonException ex) {
            log.error("Json processing error", ex);
            throw new AgentSpaceException(AgentSpaceErrorCodes.JSON_PROCESS_ERROR);
        }
    }

    /**
     * Java Bean 转换 Json 字符串
     *
     * @param obj v
     * @return v
     */
    public static <T> String obj2jsonUnformatted(T obj) {
        try {
            return getInstance().UNFORMATTED_OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JacksonException ex) {
            log.error("Json processing error", ex);
            throw new AgentSpaceException(AgentSpaceErrorCodes.JSON_PROCESS_ERROR);
        }
    }

    /**
     * Json字符串反序列化Java对象
     *
     * @param jsonString v
     * @param valueType  v
     * @param <T>        v
     * @return T
     */
    public static <T> T json2pojo(String jsonString, Class<T> valueType) {
        if (ObjectUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return getInstance().OBJECT_MAPPER.readValue(jsonString, valueType);
        } catch (JsonProcessingException ex) {
            log.error("Json processing error", ex);
            throw new AgentSpaceException(AgentSpaceErrorCodes.JSON_PROCESS_ERROR);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (ObjectUtils.isEmpty(json)) {
            return null;
        }

        try {
            return getInstance().OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.warn("json read error, {}", e.getMessage());
        }
        return null;
    }

    /**
     * json 转换 map 对象  key默认是String value自定义类型
     *
     * @param jsonString v
     * @param valueClazz clazz value自定义类型
     * @return Map<T>
     */
    public static <T> Map<String, T> json2PojoMap(String jsonString, Class<T> valueClazz) {
        if (ObjectUtils.isEmpty(jsonString)) {
            return new HashMap<>();
        }
        try {
            return getInstance().OBJECT_MAPPER.readerForMapOf(valueClazz).readValue(jsonString);
        } catch (JsonProcessingException ex) {
            log.error("Json processing error", ex);
            throw new AgentSpaceException(AgentSpaceErrorCodes.JSON_PROCESS_ERROR);
        }
    }

    /**
     * 判断JsonNode是否为空，判断叶节点为空
     *
     * @param jsonNode jsonNode object
     * @return true or false
     */
    public static boolean isEmpty(JsonNode jsonNode) {
        return jsonNode == null || jsonNode.isNull();
    }

    private enum Singleton {
        INSTANCE;

        private final JacksonUtils instance;

        Singleton() {
            instance = new JacksonUtils();
        }

        private JacksonUtils getInstance() {
            return instance;
        }
    }

    // 自定义OffsetDateTime类型的适配器
    static class OffsetDateTimeTypeAdapter implements JsonDeserializer<OffsetDateTime>, JsonSerializer<OffsetDateTime> {
        @Override
        public OffsetDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            return OffsetDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        @Override
        public JsonElement serialize(OffsetDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
    }

    public static String transGsonToJSONString(Object obj) {
        Gson gson = new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeAdapter())
            .create();
        return gson.toJson(obj);
    }
}
