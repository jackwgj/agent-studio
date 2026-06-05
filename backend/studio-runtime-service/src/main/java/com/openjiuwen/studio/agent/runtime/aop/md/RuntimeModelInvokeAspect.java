/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.aop.md;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.runtime.annotation.SecurityCheck;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.common.dto.run.ChatMessage;
import com.openjiuwen.studio.agent.common.dto.md.ModelInvokeBase;
import com.openjiuwen.studio.agent.runtime.entity.security.ContentIdentifyReq;
import com.openjiuwen.studio.agent.runtime.enums.IdentifyType;
import com.openjiuwen.studio.agent.runtime.exception.OpenAiHttpException;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelApiLogService;
import com.openjiuwen.studio.agent.runtime.entity.security.Message;
import com.openjiuwen.studio.agent.runtime.service.security.SecurityService;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;
import com.openjiuwen.studio.agent.runtime.utils.ModelInvokeExceptionUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unchecked")
@Component
@Aspect
public class RuntimeModelInvokeAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeModelInvokeAspect.class);

    @Value("${model.request.securityCheck:true}")
    private boolean securityCheckEnable;

    @Value(
            "${model.request.security.outputEn:Sorry, this question does not meet the safety and compliance requirements and cannot be answered at this time.}")
    private String errorMsgEn;

    @Value("${model.request.security.outputZh:抱歉，该问题不符合安全合规要求，暂时无法回答}")
    private String errorMsgZh;

    @Autowired
    private RuntimeModelApiLogService apiLogService;

    @Autowired
    private SecurityService securityService;

    @Pointcut("execution(* com.openjiuwen.studio.agent.runtime.controller.RuntimeModelServiceController.*(..))")
    public void pointcut() {
    }

    private Object modelInvokeProceed(ProceedingJoinPoint joinPoint, AtomicBoolean needSaveLog) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (OpenAiHttpException e) {
            needSaveLog.set(true);
            throw ModelInvokeExceptionUtil.commonHttpExceptionHandler(e.getStatusCode().value(),
                    e.getErrorResponse() == null ? "" : e.getErrorResponse());
        }
    }

    @Around("pointcut()")
    public Object handler(ProceedingJoinPoint joinPoint) throws Throwable {
        ModelApiLog apiLog = ModelApiLogThreadLocal.getApiLog();
        AtomicBoolean needSaveLog = new AtomicBoolean(false);
        try {
            String ownerDomainId = RequestContextUtils.getHeaders().get("x-owner-domain-id");
            apiLog.setOwnerProjectId(RequestContextUtils.getHeaders().get("x-owner-project-id"));
            apiLog.setDomainId(RequestContextUtils.getRequestUserDomainId());
            apiLog.setOwnerDomainId(StringUtils.isEmpty(ownerDomainId) ? apiLog.getDomainId() : ownerDomainId);
            apiLog.setUserName(RequestContextUtils.getRequestUserName());
            apiLog.setUserId(RequestContextUtils.getRequestUserId());

            extractModelId(apiLog, joinPoint);

            if (!requestSecurityCheck(apiLog, joinPoint)) {
                needSaveLog.set(true);
                String msg = LanguageUtils.isChinese() ? errorMsgZh : errorMsgEn;
                throw new AgentStudioException(StudioError.MD_MSG_CONTAIN_SENSITIVE, msg);
            }

            Object result = modelInvokeProceed(joinPoint, needSaveLog);
            responseSecurityCheck(apiLog, apiLog.getModelId(), result);
            return result;
        } catch (Throwable e) {
            if (e instanceof AgentStudioException) {
                apiLog.setApiStatus(((AgentStudioException) e).getErrorCode().getHttpStatus().value());
            } else {
                LOGGER.error("Model invoke exception.", e);
                apiLog.setApiStatus(500);
            }

            needSaveLog.set(true);
            throw e;
        } finally {
            try {
                if (needSaveLog.get() || !Boolean.TRUE.equals(apiLog.getStream())) {
                    apiLog.setDuration(System.currentTimeMillis() - apiLog.getStartTime());
                    LOGGER.info("Invoke model service. model:{} service:{} status:{} cost:{} ms", apiLog.getModelName(),
                            apiLog.getServiceKey(), apiLog.getApiStatus(), apiLog.getDuration());
                    apiLogService.saveModelApiLog(apiLog);
                }

                ModelApiLogThreadLocal.clear();
            } catch (Exception e) {
                LOGGER.warn("Record model invoke log exception. {}", e.getMessage());
            }
        }
    }

    private void contentSecurityCheckParams(ModelApiLog apiLog, Object headers) {
        // 免费试用的模型，必须要经过风控校验
        if (apiLog.isFreeModel()) {
            apiLog.setRequestVerify(true);
            apiLog.setResponseVerify(true);
            return;
        }
        if (headers == null) {
            LOGGER.error("Request headers is null.");
            return;
        }
        if (headers instanceof Map<?, ?> headersMap) {
            boolean isRequestVerify = "true".equalsIgnoreCase((String) headersMap.get("safety-barrier"));
            apiLog.setRequestVerify(isRequestVerify);
            apiLog.setResponseVerify(isRequestVerify);
        }
    }

    private void extractModelId(ModelApiLog apiLog, ProceedingJoinPoint joinPoint) {
        Object body = joinPoint.getArgs().length > 3 ? joinPoint.getArgs()[3] : null;
        if (body == null) {
            return;
        }

        if (body instanceof ModelInvokeBase request) {
            apiLog.setModelId(request.getModel());
        }
    }

    private boolean requestSecurityCheck(ModelApiLog apiLog, ProceedingJoinPoint joinPoint) {
        if (!securityCheckEnable) {
            return true;
        }
        Object body = joinPoint.getArgs().length > 3 ? joinPoint.getArgs()[3] : null;
        if (body == null) {
            LOGGER.warn("Request body is null.");
            return true;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getAnnotation(SecurityCheck.class) == null) {
            LOGGER.warn("Not found SecurityCheck annotation.");
            return true;
        }

        contentSecurityCheckParams(apiLog, joinPoint.getArgs()[2]);
        if (!apiLog.isRequestVerify()) {
            return true;
        }

        if (body instanceof ChatCompletionRequest request) {
            apiLog.setStream(request.getStream());
            List<ContentIdentifyReq> reqs = getIdentifyRequests(apiLog.getDomainId(), request.getModel(),
                    request.getMessages());

            if (apiLog.isFreeModel()) {
                apiLog.setInput(getRequestContent(reqs));
            }
        } else {
            LOGGER.warn("Not chat completion. Not support security check.");
        }
        return true;
    }

    private String getRequestContent(List<ContentIdentifyReq> reqs) {
        StringBuilder builder = new StringBuilder();
        for (ContentIdentifyReq req : reqs) {
            Optional.ofNullable(req.getMessage())
                .map(Message::getText)
                .ifPresent(text -> builder.append(text).append("\n"));
        }
        return builder.toString();
    }

    private List<ContentIdentifyReq> getIdentifyRequests(String domainId, String model, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        List<ContentIdentifyReq> list = new ArrayList<>(messages.size());
        for (ChatMessage message : messages) {
            Object content = message.getContent();
            list.addAll(createContentIdentify(domainId, model, content));
        }
        return list;
    }

    private List<ContentIdentifyReq> createTextContentIdentify(String domainId, String model, String content) {
        if (content.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new ContentIdentifyReq(domainId, model, content));
    }

    private List<ContentIdentifyReq> createContentIdentify(String domainId, String model, Object content) {
        if (content instanceof String) {
            return createTextContentIdentify(domainId, model, (String) content);
        } else if (content instanceof List<?> list) {
            List<ContentIdentifyReq> requests = new ArrayList<>(list.size());
            for (Object obj : list) {
                if (obj instanceof String) {
                    requests.addAll(createTextContentIdentify(domainId, model, (String) obj));
                } else if (obj instanceof Map<?, ?> map) {
                    String type = (String) map.get("type");
                    switch (type) {
                        case "text": {
                            String text = (String) map.get("text");
                            requests.addAll(createTextContentIdentify(domainId, model, text));
                            break;
                        }
                        case "image_url": {
                            Map<String, String> imageUrl = (Map<String, String>) map.get("image_url");
                            requests.add(
                                    new ContentIdentifyReq(domainId, model, IdentifyType.IMAGE, imageUrl.get("url")));
                            break;
                        }
                        case "video": {
                            List<String> video = (List<String>) map.get("video");
                            for (String url : video) {
                                requests.add(new ContentIdentifyReq(domainId, model, IdentifyType.IMAGE, url));
                            }
                            break;
                        }
                        default: {
                            LOGGER.warn("Message content type is not support. type:{}", type);
                        }
                    }
                } else {
                    LOGGER.warn("message content item type is not support. type:{}",
                            obj == null ? null : obj.getClass().getName());
                }
            }
            return requests;
        } else {
            LOGGER.warn("message content type is not support. type:{}",
                    content == null ? null : content.getClass().getName());
            return Collections.emptyList();
        }
    }

    private void responseSecurityCheck(ModelApiLog apiLog, String modelId, Object response) {
        if (!securityCheckEnable || !apiLog.isResponseVerify()) {
            return;
        }
        if (response instanceof SseEmitter) {
            return;
        }
        if (response instanceof JSONObject map) {
            Object choices = map.get("choices");
            if (!(choices instanceof JSONArray choicesList)) {
                LOGGER.error("Response miss choices filed.");
                return;
            }

            if (choicesList.isEmpty()) {
                return;
            }
            if (!(choicesList.get(0) instanceof JSONObject choicesItem)) {
                return;
            }
            JSONObject message = (JSONObject) choicesItem.get("message");
            String content = message.getString("content");
            if (content == null || content.isEmpty()) {
                return;
            }

            if (apiLog.isFreeModel()) {
                apiLog.setOutput(content);
            }
        }
    }

    Object createSensitiveRsp(ModelApiLog apiLog) {
        long time = System.currentTimeMillis() / 1000;
        String msg = LanguageUtils.isChinese() ? errorMsgZh : errorMsgEn;
        String id = UUID.randomUUID().toString().replace("-", "");

        if (Boolean.TRUE.equals(apiLog.getStream())) {
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send(String.format(Locale.ROOT,
                        "{\"id\":\"%s\",\"object\":\"chat.completion.chunk\",\"created\":%d,\"model\":\"%s\","
                                + "\"choices\":[{\"index\":0,\"delta\":{\"content\":\"%s\"}}],\"usage\":"
                                + "{\"prompt_tokens\":0,\"total_tokens\":0,\"completion_tokens\":0}}", id, time,
                        apiLog.getModelId(), msg));
                emitter.send(String.format(Locale.ROOT,
                        "{\"id\":\"%s\",\"object\":\"chat.completion.chunk\",\"created\":%d,\"model\":\"%s\","
                                + "\"choices\":[{\"index\":0,\"delta\":{\"content\":\"\"},\"finish_reason\":\"stop\"}],"
                                + "\"usage\":{\"prompt_tokens\":0,\"total_tokens\":0,\"completion_tokens\":0}}", id, time,
                        apiLog.getModelId()));
                emitter.send(String.format(Locale.ROOT,
                        "{\"id\":\"%s\",\"object\":\"chat.completion.chunk\",\"created\":%d,\"model\":\"%s\","
                                + "\"choices\":[],\"usage\":{\"prompt_tokens\":0,\"total_tokens\":0,\"completion_tokens\":0}}",
                        id, time, apiLog.getModelId()));
                emitter.send(" [DONE]");
            } catch (Exception e) {
                LOGGER.error("Fail send stream event.", e);
            } finally {
                emitter.complete();
            }
            return emitter;
        } else {
            String content = String.format(Locale.ROOT,
                    "{\"id\":\"chat-%s\",\"object\":\"chat.completion\",\"created\":%d,"
                            + "\"model\":\"%s\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"%s\"},"
                            + "\"logprobs\":null,\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":0,\"total_tokens\":0,"
                            + "\"completion_tokens\":0}}", id, time, apiLog.getModelId(), msg);
            return JSONObject.parseObject(content);
        }
    }
}
