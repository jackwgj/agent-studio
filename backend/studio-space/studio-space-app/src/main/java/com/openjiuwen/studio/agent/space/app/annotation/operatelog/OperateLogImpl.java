/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.annotation.operatelog;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.space.app.util.MaskStringUtil;
import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.model.BaseResp;
import com.openjiuwen.studio.agent.space.common.utils.JacksonUtils;
import com.openjiuwen.studio.agent.space.dao.application.OperLogMapper;
import com.openjiuwen.studio.agent.space.dao.entity.OperLogEntity;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 操作日志注解逻辑
 */
@Aspect
@Slf4j
@Component
public class OperateLogImpl {
    @Resource
    private OperLogMapper operLogMapper;

    private static final int RESPONSE_MSG_MAX_SIZE = 255;

    @Pointcut(value = "@annotation(operateLog)")
    public void pointCut(OperateLog operateLog) {
    }

    @Around(value = "pointCut(operateLog)", argNames = "joinPoint,operateLog")
    public Object methodAround(ProceedingJoinPoint joinPoint, OperateLog operateLog) {
        OperLogEntity vo = new OperLogEntity();
        // 配置日志基本信息
        setLogBaseInfo(vo, operateLog);

        // 配置切面信息
        setLogAspectInfo(vo, joinPoint, operateLog);
        try {
            Object response = joinPoint.proceed();
            // Result 配置执行结果
            setLogResult(vo, response);

            saveLog(vo);
            return response;
        } catch (AgentSpaceException e) {
            log.error("Operator Log error on method. msg:{}", e.getMessage());

            vo.setResponseCode(String.valueOf(e.getErrorCode()));
            String errorMsg = e.getMessage();
            vo.setResponseMsg(
                StringUtils.isNotBlank(errorMsg) && errorMsg.length() > RESPONSE_MSG_MAX_SIZE ? errorMsg.substring(0,
                    RESPONSE_MSG_MAX_SIZE) : errorMsg);
            vo.setSuccessFlag(0);
            saveLog(vo);
            throw e;
        } catch (Throwable throwable) {
            log.error("Operator Log error on method", throwable);
            vo.setResponseCode("500");
            String errorMsg = throwable.getMessage();
            vo.setResponseMsg(
                StringUtils.isNotBlank(errorMsg) && errorMsg.length() > RESPONSE_MSG_MAX_SIZE ? errorMsg.substring(0,
                    RESPONSE_MSG_MAX_SIZE) : errorMsg);
            vo.setSuccessFlag(0);
            saveLog(vo);
            throw new AgentSpaceException(throwable.getMessage());
        }
    }

    private void setLogBaseInfo(OperLogEntity vo, OperateLog operateLog) {
        // when
        vo.setOperTime(LocalDateTime.now());
        // who
        vo.setOperUser(AuthorizationContextHolder.userId());
        vo.setDomainId(AuthorizationContextHolder.domainId());
        vo.setOperIp(AuthorizationContextHolder.clientIp());
        // what
        vo.setObjType(operateLog.objectType());
        // event
        vo.setOperType(operateLog.operateType());
    }

    private void setLogAspectInfo(OperLogEntity vo, ProceedingJoinPoint joinPoint, OperateLog operateLog) {
        // 记录入参
        vo.setObjIdField(operateLog.idField());
        vo.setObjIdValue(getFiledValeByNameFromRequest(vo.getObjIdField(), joinPoint));
        vo.setContent(getParamContent(joinPoint));
    }

    private String getParamContent(ProceedingJoinPoint joinPoint) {
        List<Object> paramValues = Arrays.asList(joinPoint.getArgs());
        List<String> paramNames = Arrays.asList(((CodeSignature) joinPoint.getSignature()).getParameterNames());
        JSONObject contentObject = new JSONObject();

        if (CollectionUtils.isEmpty(paramValues)) {
            return contentObject.toString();
        }

        if (paramValues.size() == 1 && paramValues.get(0) instanceof List) {
            return JacksonUtils.transGsonToJSONString(paramValues.get(0));
        }

        for (int i = 0; i < paramValues.size(); i++) {
            Object paramValue = paramValues.get(i);
            if (paramValue == null) {
                continue;
            }
            if (paramValue instanceof String || paramValue instanceof Long || paramValue instanceof Integer) {
                String paramName = String.valueOf(paramNames.get(i));
                contentObject.put(paramName, MaskStringUtil.maskValue(paramName,
                    String.valueOf(paramValues.get(paramNames.indexOf(paramName)))));
            } else {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(paramValue));
                    for (String key : jsonObject.keySet()) {
                        if (!contentObject.containsKey(key)) {
                            contentObject.put(key, MaskStringUtil.maskValue(key, jsonObject.getString(key)));
                        }
                    }
                } catch (Exception e) {
                    log.info("getParamContent, parse failed.");
                }
            }
        }

        return contentObject.toString();
    }

    private String getFiledValeByNameFromRequest(String filedName, ProceedingJoinPoint joinPoint) {
        if (ContextConstants.AGENT_SPACE_DOMAIN_ID.equals(filedName)) {
            return AuthorizationContextHolder.domainId();
        }

        String value = "";
        if (StringUtils.isBlank(filedName)) {
            return value;
        }
        List<Object> paramValues = Arrays.asList(joinPoint.getArgs());
        List<String> paramNames = Arrays.asList(((CodeSignature) joinPoint.getSignature()).getParameterNames());

        // 从入参中获取对象ID标识值
        if (paramNames.contains(filedName)) {
            return String.valueOf(paramValues.get(paramNames.indexOf(filedName)));
        } else {
            // body参数解析
            for (Object paramValue : paramValues) {
                if (paramValue instanceof String) {
                    continue;
                }
                try {
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(paramValue));
                    if (jsonObject.containsKey(filedName)) {
                        value = jsonObject.getString(filedName);
                        break;
                    }
                } catch (Exception e) {
                    log.info("getFiledValeByNameFromRequest, parse failed.");
                }
            }
        }

        return value;
    }

    private void setLogResult(OperLogEntity vo, Object response) {
        // 记录结果
        if (null == response) {
            vo.setSuccessFlag(0);
            vo.setResponseMsg("response is null!");
            return;
        }
        if (response instanceof BaseResp<?> baseResponse) {
            vo.setResponseCode(baseResponse.getCode());
            String errorMsg = baseResponse.getMessage();
            vo.setResponseMsg(
                StringUtils.isNotBlank(errorMsg) && errorMsg.length() > RESPONSE_MSG_MAX_SIZE ? errorMsg.substring(0,
                    RESPONSE_MSG_MAX_SIZE) : errorMsg);
            // 从反参中获取对象ID标识
            if (StringUtils.isBlank(vo.getObjIdValue())) {
                vo.setObjIdValue(getFiledValeByNameFromBaseResponse(vo.getObjIdField(), baseResponse.getData()));
            }
        } else if (response instanceof ResponseEntity<?> responseEntity) {
            vo.setResponseCode(String.valueOf(responseEntity.getStatusCode().value()));
            String errorMsg = responseEntity.getStatusCode().toString();
            vo.setResponseMsg(
                StringUtils.isNotBlank(errorMsg) && errorMsg.length() > RESPONSE_MSG_MAX_SIZE ? errorMsg.substring(0,
                    RESPONSE_MSG_MAX_SIZE) : errorMsg);
            if (StringUtils.isBlank(vo.getObjIdValue())) {
                vo.setObjIdValue(
                    getFiledValeByNameFromBaseResponse(vo.getObjIdField(), ((ResponseEntity<?>) response).getBody()));
            }
        } else if (response instanceof SseEmitter) {
            vo.setResponseCode("200");
            vo.setSuccessFlag(1);
        } else {
            log.error("operateLogImpl  The response type cannot be resolved.");
            vo.setSuccessFlag(1);
        }
        if (vo.getSuccessFlag() == null && StringUtils.isNotBlank(vo.getResponseCode())) {
            vo.setSuccessFlag(vo.getResponseCode().equals(String.valueOf(HttpStatus.OK.value())) ? 1 : 0);
        }
    }

    private String getFiledValeByNameFromBaseResponse(String filedName, Object data) {
        if (StringUtils.isBlank(filedName)) {
            return "";
        }

        if (data == null) {
            return "";
        } else if (data instanceof String) {
            return (String) data;
        } else if (data instanceof Long || data instanceof Integer) {
            return String.valueOf(data);
        } else if (data instanceof List) {
            return data.toString();
        } else {
            // 复杂对象解析
            try {
                return JSON.parseObject(JSON.toJSONString(data)).getString(filedName);
            } catch (Exception ex) {
                log.error("parse response failed! {}", ex.getMessage());
            }
        }

        return "";
    }

    private void saveLog(OperLogEntity vo) {
        try {
            operLogMapper.insert(vo);
        } catch (Exception ex) {
            log.error("record operate log failed! {}", ex.getMessage());
        }
    }
}
