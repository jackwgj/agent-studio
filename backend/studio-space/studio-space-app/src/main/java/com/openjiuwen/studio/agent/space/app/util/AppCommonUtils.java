package com.openjiuwen.studio.agent.space.app.util;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;

import com.openjiuwen.studio.agent.space.common.constant.HeaderConstant;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class AppCommonUtils {
    // 标准时间格式
    public static final String PATTERN_STANDARD = "yyyy-MM-dd HH:mm:ss";

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void validateDomainIdUserIdIsNotEmpty() {
        validateDomainIdIsNotEmpty();
        validateUserIdIsNotEmpty();
    }

    public static void validateDomainIdIsNotEmpty() {
        if (StringUtils.isBlank(AuthorizationContextHolder.domainId())) {
            log.error("tenant id is empty");
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_TENANT_ID_EMPTY_ERROR);
        }
    }

    public static void validateUserIdIsNotEmpty() {
        if (StringUtils.isBlank(AuthorizationContextHolder.userId())) {
            log.error("user id is empty");
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_USER_ID_EMPTY_ERROR);
        }
    }

    /**
     * 添加数据库实体类基础字段，有值时不去更新
     * @param object object
     */
    public static void setCommonCreateProperties(Object object) {
        try {
            Class<?> cls = object.getClass();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            setIfNull(cls, object, "CreatedDate", Timestamp.class, now);
            setIfNull(cls, object, "CreatedByUserId", String.class,
                Optional.ofNullable(AuthorizationContextHolder.userId()).orElse("SYSTEM"));
            setIfNull(cls, object, "LastUpdatedDate", Timestamp.class, now);
            setIfNull(cls, object, "LastUpdatedByUserId", String.class,
                Optional.ofNullable(AuthorizationContextHolder.userId()).orElse("SYSTEM"));
            setIfNull(cls, object, "DomainId", String.class,
                Optional.ofNullable(AuthorizationContextHolder.domainId()).orElse("SYSTEM"));
        } catch (Exception e) {
            log.error("WPS set creating common properties error. {}", e.getMessage());
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR, e.getMessage());
        }
    }

    private static <T> void setIfNull(Class<?> cls, Object object, String fieldName, Class<T> paramType, T newValue)
        throws Exception {
        Method getter = cls.getMethod("get" + fieldName);
        Object currentValue = getter.invoke(object);

        if (currentValue == null) {
            Method setter = cls.getMethod("set" + fieldName, paramType);
            setter.invoke(object, newValue);
        }
    }

    /**
     * 更新最后修改人，最后修改时间
     *
     * @param object 对象
     */
    public static void setCommonUpdateProperties(Object object) {
        try {
            Class<?> cls = object.getClass();
            Timestamp now = new Timestamp(new Date().getTime());

            Method setLastUpdatedDate = cls.getMethod("setLastUpdatedDate", Timestamp.class);
            setLastUpdatedDate.invoke(object, now);

            Method setLastUpdatedByUserId = cls.getMethod("setLastUpdatedByUserId", String.class);
            setLastUpdatedByUserId.invoke(object,
                Optional.ofNullable(AuthorizationContextHolder.userId()).orElse("SYSTEM"));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("WPS set updating common properties error. {}", e.getMessage());
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR, e.getMessage());
        }
    }

    public static Map<String, String> getCommonMapHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(HeaderConstant.HEADER_CONTENT_TYPE, "application/json;charset=UTF-8");
        headerMap.put(HeaderConstant.HEADER_DOMAIN_ID, AuthorizationContextHolder.domainId());
        headerMap.put(HeaderConstant.HEADER_USER_ID, AuthorizationContextHolder.userId());
        headerMap.put(HeaderConstant.HEADER_X_AUTH_TOKEN, AuthorizationContextHolder.iamToken());
        headerMap.put(ACCEPT, "application/json;charset=UTF-8");
        return headerMap;
    }

    /**
     * 时间戳转化为时间
     * @param mills 时间戳
     * @return 时间格式字符串
     */
    public static String getStandardDateTime(long mills) {
        return DateFormatUtils.format(mills, PATTERN_STANDARD, Locale.ENGLISH);
    }

    /**
     * 获取某一天的开始时间
     *
     * @return 耗秒数 long
     */
    public static long getDayStartTime(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MIN).toEpochSecond(OffsetDateTime.now().getOffset()) * 1000;
    }

    /**
     * 格式化文件名
     * @param filename filename
     * @return filename
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
