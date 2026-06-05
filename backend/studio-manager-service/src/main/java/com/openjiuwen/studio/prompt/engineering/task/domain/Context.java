/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Context工厂模式
 *
 */
public class Context implements Serializable {

    public static final String STRATEGIES_KEY = "_STRATEGIES";

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private static final long serialVersionUID = -2152514220936164220L;

    @JsonProperty
    protected final Map<String, String> context = new HashMap<>();

    @JsonProperty
    protected final Map<String, Strategy> strategies = new HashMap<>();

    protected final Strategy DEFAULT_STRATEGY =
        new Strategy().setDeliverable(true).setUpdatable(false).setClassName(String.class.getName());

    private final ObjectMapper mapper = new ObjectMapper();

    public static Context deserialize(String _context) {
        if (StringUtils.isBlank(_context)) {
            return null;
        }
        Context innerContext = null;
        try {
            ObjectMapper innerObjectMapper = new ObjectMapper();
            innerContext = innerObjectMapper.readValue(_context, Context.class);
        } catch (IOException e) {
            logger.error("deserialize(String)", e);
        }
        return innerContext;
    }

    public static String serialize(Context context) {
        if (context == null) {
            return null;
        }
        String _context = null;
        try {
            ObjectMapper serializeMapper = new ObjectMapper();
            _context = serializeMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            logger.error("serialize(Context)", e);
        }
        return _context;
    }

    public static Context factory() {
        return new Context();
    }

    public void put(String key, String value) {
        put(key, DEFAULT_STRATEGY, value);
    }

    public void put(String key, Strategy strategy, String value) {
        strategies.computeIfAbsent(key, k -> {
            context.put(key, value);
            return strategy;
        });
    }

    public void putUsingJsonSerialization(String key, Strategy strategy, Serializable value) {
        if (!strategies.containsKey(key)) {
            try {
                String str = mapper.writeValueAsString(value);
                context.put(key, str);
                strategies.put(key, strategy);
            } catch (JsonProcessingException e) {
                logger.error("putUsingJsonSerialization(String, Strategy, Serializable)", e);
            }
        }
    }

    public void update(String key, String value) {
        Strategy strategy = strategies.get(key);
        if (strategy != null) {
            if (strategy.updatable) {
                context.put(key, value);
            }
        }
    }

    public void update(String key, Serializable value) {
        Strategy strategy = strategies.get(key);
        try {
            if (strategy != null) {
                if (strategy.updatable) {
                    context.put(key, mapper.writeValueAsString(value));
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("update(String, Serializable)", e);
        }
    }

    public <T> Strategy getStrategy(String key) {
        return strategies.get(key);
    }

    public String getValue(String key) {
        return context.get(key);
    }

    public String getValueOrDefault(String key, String _default) {
        String s = context.get(key);
        if (s == null) {
            return _default;
        }
        return s;
    }

    public Integer getValueAsIntegerOrDefault(String key, Integer _default) {
        String s = context.get(key);
        if (StringUtils.isBlank(s)) {
            return _default;
        }
        Integer i = _default;
        try {
            i = NumberUtils.createInteger(s);
        } catch (Exception e) {
            logger.warn("getValueAsIntegerOrDefault(String, Integer)", e);
        }
        return i;
    }

    public Integer getValueAsInteger(String key) {
        return getValueAsIntegerOrDefault(key, null);
    }

    public Set<String> keySet() {
        return strategies.keySet();
    }

    public void putAll(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void remove(String key) {
        strategies.remove(key);
        context.remove(key);
    }

    public boolean contains(String key) {
        return strategies.containsKey(key);
    }

    /**
     * 吸收另外一个Context
     *
     * @param context
     */
    public void absorb(Context context) {
        if (context != null) {
            for (String key : context.strategies.keySet()) {
                Strategy strategy = context.strategies.get(key);
                if (strategy != null && strategy.deliverable) {
                    Strategy my = this.strategies.get(key);
                    String value = context.context.get(key);
                    if (my == null) {
                        this.strategies.put(key, strategy);
                        this.context.put(key, value);
                    } else if (my.updatable) {
                        this.context.put(key, value);
                    }
                }
            }
        }
    }

    public Map<String, String> convertToMap() {
        Map<String, String> map = new HashMap<>(context);
        try {
            map.put(STRATEGIES_KEY, mapper.writeValueAsString(strategies));
        } catch (JsonProcessingException e) {
            logger.error("[Context]write strategies failed!");
        }
        return map;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Getter
    public static class Strategy implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 5421366283540547832L;

        protected Boolean updatable = false;

        protected Boolean deliverable = false;

        protected Class<?> clazz = null;

        protected String className = String.class.getName();

        public static <T extends Serializable> Strategy of(Class<T> clazz) {
            return of(false, false, clazz);
        }

        public static Strategy of() {
            return of(false, false);
        }

        public static Strategy of(Boolean updatable, Boolean deliverable) {
            return of(updatable, deliverable, String.class);
        }

        public static <T extends Serializable> Strategy of(Boolean updatable, Boolean deliverable, Class<T> clazz) {
            return new Strategy().setUpdatable(updatable)
                .setDeliverable(deliverable)
                .setClazz(null)
                .setClassName(clazz.getName());
        }

        public Strategy setUpdatable(Boolean updatable) {
            this.updatable = updatable;
            return this;
        }

        public Strategy setDeliverable(Boolean deliverable) {
            this.deliverable = deliverable;
            return this;
        }

        public <T extends Serializable> Strategy setClazz(Class<T> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Strategy setClassName(String className) {
            this.className = className;
            return this;
        }
    }
}
