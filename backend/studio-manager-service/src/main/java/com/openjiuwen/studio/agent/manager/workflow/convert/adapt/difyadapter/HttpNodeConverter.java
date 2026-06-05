/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.VariableEnum;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.enums.DifyAuthType;
import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.enums.DifyHttpExceptionType;
import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums.HttpBodyType;

import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums.SupportHttpMethodType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HttpNodeConverter extends AbstractSDSLNodeConverter {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{#([^.]+)\\.([^#]+)#}}");

    private static final String EXCEPTION_SUPPRESSION = "{\"body\":\"\",\"status_code\":0,\"headers\":\"\"}";

    private final CheckUrlService checkUrlService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${workflow.connector_config_host_blacklist:}")
    private String connectorConfigHostBlacklist;

    @Value("${front-page.block-nodes:}")
    private String blockNodes;

    public HttpNodeConverter(CheckUrlService checkUrlService) {
        this.checkUrlService = checkUrlService;
    }

    /**
     * 引用匹配数据结构
     */
    private static class ReferenceMatch {
        // 匹配的node
        String nodeId;

        // 匹配的变量
        String varName;

        // 匹配到的起始位置
        int start;

        // 匹配到的结束位置
        int end;
    }

    @Data
    private static class UrlInfo {

        private String processedUrl;

        private String scheme = "";

        private String endpoint = "";

        private String path = "";

        private String query = "";
    }

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.HTTP.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO,
        Map<String, WorkflowNodeVO> workflowNodeMap) {
        List<WorkflowFieldVO> inputParams = new ArrayList<>();
        // 判断 HTTP 节点是否放开
        if (StringUtils.isNotEmpty(blockNodes) && blockNodes.toLowerCase(Locale.ROOT).contains("http")) {
            log.warn("The http node is not supported.");
            workflowNodeVO.setType(NodeType.EMPTY.getType())
                .setConfigs(super.adaptConfigs(data))
                .setInputs(new ArrayList<>())
                .setOutputs(new ArrayList<>());
            return workflowNodeVO;
        }
        // 判断 method 是否为支持的 method, 否则返回空白节点
        String method = data.getOrDefault("method", "").toString();
        if (!SupportHttpMethodType.checkHttpMethod(method.toUpperCase(Locale.ROOT))) {
            log.warn("The method {} is not support.", method);
            data.put("method", "get");
            workflowNodeVO.setType(NodeType.EMPTY.getType())
                .setConfigs(adaptConfigs(data, workflowNodeMap, inputParams))
                .setInputs(adaptInputs(data, workflowNodeMap, inputParams))
                .setOutputs(adaptOutputs(data));
            return workflowNodeVO;
        }

        // 判断 url 是否为白名单
        String url = data.getOrDefault("url", "").toString();
        UrlInfo urlInfo = parseUrl(url, workflowNodeMap);
        try {
            checkUrlService.checkSwaggerUrlValid(urlInfo.scheme + "://" + urlInfo.endpoint,
                connectorConfigHostBlacklist);
        } catch (Exception e) {
            log.warn("The url {} is not in the whitelist, please check it.", url);
            data.put("url", "");
            workflowNodeVO.setType(NodeType.EMPTY.getType())
                .setConfigs(adaptConfigs(data, workflowNodeMap, inputParams))
                .setInputs(adaptInputs(data, workflowNodeMap, inputParams))
                .setOutputs(adaptOutputs(data));
            return workflowNodeVO;
        }

        // 开始适配
        workflowNodeVO.setType(NodeType.HTTP.getType());
        workflowNodeVO.setConfigs(adaptConfigs(data, workflowNodeMap, inputParams));
        // input一定要排在configs后面，等处理configs时生成输入参数
        workflowNodeVO.setInputs(adaptInputs(data, workflowNodeMap, inputParams));
        workflowNodeVO.setOutputs(adaptOutputs(data));
        return workflowNodeVO;
    }

    /**
     * 转换 Inputs
     * Headers -> Headers（请求头）
     * Params -> Variables(输入参数）
     * @param data Http 节点的 data
     * @param workflowNodeMap 已经解析好的节点（key:节点名字，value：节点）
     * @return 支持的 Http Inputs 字段
     */
    public List<WorkflowFieldVO> adaptInputs(Map<String, Object> data, Map<String, WorkflowNodeVO> workflowNodeMap,
        List<WorkflowFieldVO> inputParams) {
        List<WorkflowFieldVO> inputs = new ArrayList<>();
        inputs.add(adaptHeaders(data, workflowNodeMap));
        inputs.addAll(inputParams);
        inputParams.clear();
        inputs.add(adaptQuery(data, workflowNodeMap));
        return inputs;
    }

    /**
     * 转换 Outputs
     * Dify DSL 中 Http 节点没有定义 Outputs，用 Agents Arts 的 Http 节点默认的 Outputs 代替
     * @param data Http 节点的 data
     * @return 支持的 Http Outputs 字段
     */
    @Override
    public List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data) {
        List<WorkflowFieldVO> outputs = new ArrayList<>();
        outputs.add(createOutputWorkflowFieldVO(CommonConstant.DIFY.BODY,
            VariableEnum.PARAGRAPH.getDslType(), "请求响应体"));
        outputs.add(createOutputWorkflowFieldVO(CommonConstant.DIFY.STATUS_CODE,
            VariableEnum.INTEGER.getDslType(), "请求状态码"));
        outputs.add(createOutputWorkflowFieldVO(CommonConstant.DIFY.HEADERS,
            VariableEnum.PARAGRAPH.getDslType(), "请求响应头"));
        outputs.add(createOutputWorkflowFieldVO(CommonConstant.DIFY.ERROR_MESSAGE,
            VariableEnum.PARAGRAPH.getDslType(), "错误消息"));
        return outputs;
    }

    /**
     * 转换 Configs
     * @param data Http 节点的 data
     * @return 支持的 Http Configs 字段
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> adaptConfigs(Map<String, Object> data, Map<String, WorkflowNodeVO> workflowNodeMap,
        List<WorkflowFieldVO> inputParams) {
        Map<String, Object> configs = super.adaptConfigs(data);
        // 转换 url
        String url = data.getOrDefault("url", "").toString();
        adaptUrl(url, configs, workflowNodeMap);

        // 转换 method
        configs.put(CommonConstant.DIFY.METHOD,
            SupportHttpMethodType.fromValue(data.getOrDefault("method", "").toString()));

        // 转换body
        Map<String, Object> httpBody = (Map<String, Object>) data.getOrDefault("body", new HashMap<>());
        List<WorkflowFieldVO> bodyParams = adaptBody(data, httpBody, configs, workflowNodeMap);
        if (!bodyParams.isEmpty()) {
            inputParams.addAll(bodyParams);
        }

        // 转换异常处理
        adaptException(data, configs);

        // 转换 API 鉴权方式
        adaptAuth(data, configs);

        return configs;
    }

    /**
     * 处理 headers 的转换
     * @param data Http 节点的 data
     * @param workflowNodeMap 已经解析好的节点（key:节点名字，value：节点）
     * @return 支持的 Headers 字段
     */
    private WorkflowFieldVO adaptHeaders(Map<String, Object> data, Map<String, WorkflowNodeVO> workflowNodeMap) {
        String rawHeaders = String.valueOf(data.getOrDefault("headers", ""));
        if ("null".equals(rawHeaders) || "".equals(rawHeaders)) {
            return adaptEmptyHeaders();
        }

        WorkflowFieldVO headersField = new WorkflowFieldVO();
        headersField.setName(CommonConstant.DIFY.HEADERS);
        headersField.setType(VariableEnum.JSON_OBJECT.getDslType());
        headersField.setSource(WorkflowFieldVO.SourceEnum.PRE_DEFINED);
        headersField.setRequired(true);

        WorkflowFieldVOValue headersValue = new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.NESTED)
                                                .setHint("")
                                                .setDefault("");
        headersField.setValue(headersValue);

        List<WorkflowFieldVO> schemaList = new ArrayList<>();
        String[] headers = rawHeaders.split("\\n");

        for (String header : headers) {
            if (header.isEmpty()) {
                continue;
            }
            String[] headerKeyValue = header.split(":");
            String rawKey = headerKeyValue[0];
            String rawValue = headerKeyValue[1];

            WorkflowFieldVO headerItem = new WorkflowFieldVO();
            headerItem.setName(rawKey);
            headerItem.setType(VariableEnum.PARAGRAPH.getDslType());
            headerItem.setRequired(true);
            headerItem.setSource(WorkflowFieldVO.SourceEnum.PRE_DEFINED);

            // 处理 key
            String content = replaceReferenceWithFixedValue(rawKey, workflowNodeMap);
            headerItem.setName(content);

            // 处理 Value
            headerItem.setValue(transformReferenceToSupportedFormat(rawValue, workflowNodeMap));
            schemaList.add(headerItem);
        }

        // 将解析出的所有 header 放入 schema
        headersField.setSchema(schemaList);
        return headersField;
    }

    /**
     * 生成一个 empty headers（不转换任何信息）
     * @return 返回空白 headers
     */
    private WorkflowFieldVO adaptEmptyHeaders() {
        WorkflowFieldVO headersField = new WorkflowFieldVO();
        // 1. 构造基础信息
        headersField.setName(CommonConstant.DIFY.HEADERS);
        headersField.setType(VariableEnum.JSON_OBJECT.getDslType());
        headersField.setDescription("http节点Header配置参数");
        headersField.setRequired(true);
        headersField.setSource(WorkflowFieldVO.SourceEnum.PRE_DEFINED);
        headersField.setReflection(false);

        // 2. 构造 Value 结构
        WorkflowFieldVOValue value = new WorkflowFieldVOValue();
        value.setType(WorkflowFieldVOValue.TypeEnum.NESTED);
        value.setHint("");
        value.setDefault("");
        headersField.setValue(value);

        // 3. 构造 Schema
        List<WorkflowFieldVO> schemaList = new ArrayList<>();
        headersField.setSchema(schemaList);

        return headersField;
    }

    /**
     * 处理 Query 的转换 （query 的来源包括从 url 中获取，和从 params 字段获取。dify dsl 中的 params 相当于query 的作用）
     * @param data Http 节点的 data
     * @param workflowNodeMap 已经解析好的节点（key:节点名字，value：节点）
     * @return 支持的 Query 字段
     */
    private WorkflowFieldVO adaptQuery(Map<String, Object> data, Map<String,
        WorkflowNodeVO> workflowNodeMap) {
        // 1. 从 url 中获取query
        String url = data.getOrDefault("url", "").toString();
        String query;
        List<WorkflowFieldVO> processedQueryList = new ArrayList<>();

        if (StringUtils.isNotBlank(url)) {
            // 获取 Query 部分
            int questionMarkIndex = url.indexOf('?');
            if (questionMarkIndex != -1) {
                query = url.substring(questionMarkIndex + 1);

                // 把query进行拆分
                Map<String, String> queryMap = new HashMap<>();
                String[] queryArr = query.split("&");
                for (String queryItem : queryArr) {
                    String[] kv = queryItem.split("=");
                    String queryItemKey = kv[0];
                    String queryItemValue = kv[1];
                    queryMap.put(queryItemKey, queryItemValue);
                }

                // 转换每个 query item
                if (!queryMap.isEmpty()) {
                    for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                        WorkflowFieldVO processedQuery = new WorkflowFieldVO().setDescription("")
                            .setRequired(true)
                            .setSource(WorkflowFieldVO.SourceEnum.USER)
                            .setReflection(false)
                            .setType(VariableEnum.PARAGRAPH.getDslType());

                        // 处理 key
                        processedQuery.setName(replaceReferenceWithFixedValue(entry.getKey(), workflowNodeMap));

                        // 处理 Value
                        processedQuery.setValue(transformReferenceToSupportedFormat(entry.getValue(), workflowNodeMap));

                        processedQueryList.add(processedQuery);
                    }
                }
            }
        }
        // 2. 从 params 中获取 query (dify 中 params 对应的query）
        String rawParams = String.valueOf(data.getOrDefault("params", ""));
        String[] rawParamsList = rawParams.split("\\n");

        for (String rawParam : rawParamsList) {
            WorkflowFieldVO processedParam = new WorkflowFieldVO().setDescription("")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setReflection(false)
                .setType(VariableEnum.PARAGRAPH.getDslType());

            if (rawParam.isEmpty()) {
                continue;
            }
            String[] kv = rawParam.split(":");
            String rawKey = kv[0];
            String rawValue = kv[1];

            // 处理 key
            processedParam.setName(replaceReferenceWithFixedValue(rawKey, workflowNodeMap));

            // 处理 Value
            processedParam.setValue(transformReferenceToSupportedFormat(rawValue, workflowNodeMap));

            processedQueryList.add(processedParam);
        }

        WorkflowFieldVOValue queryValue = new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.NESTED)
            .setHint("")
            .setDefault("");

        return new WorkflowFieldVO().setName(CommonConstant.DIFY.QUERY)
            .setType(VariableEnum.JSON_OBJECT.getDslType())
            .setDescription("http节点请求参数")
            .setRequired(true)
            .setSchema(new ArrayList<>())
            .setSource(WorkflowFieldVO.SourceEnum.PRE_DEFINED)
            .setReflection(false)
            .setSchema(processedQueryList)
            .setValue(queryValue);
    }

    /**
     * 处理 url 的转换
     * @param url 原始url
     * @param configs Http 节点的 configs
     * @param workflowNodeMap 已经解析好的节点（key:节点名字，value：节点）
     */
    private void adaptUrl(String url, Map<String, Object>configs, Map<String, WorkflowNodeVO> workflowNodeMap) {
        UrlInfo urlInfo = parseUrl(url, workflowNodeMap);
        if (StringUtils.isNotEmpty(urlInfo.scheme) && StringUtils.isNotEmpty(urlInfo.endpoint)) {
            configs.put(CommonConstant.DIFY.ENDPOINT, urlInfo.scheme + "://" + urlInfo.endpoint);
        } else {
            configs.put(CommonConstant.DIFY.ENDPOINT, "");
        }
        configs.put(CommonConstant.DIFY.PATH, urlInfo.path);
    }


    /**
     * 转换 body（只转换 None 和 Json 形式）
     * 处理方式：
     *  1. None -> None
     *  2. Json -> Json
     *  3. 其他形式 -> None
     * @param httpBody  Dify DSL 中 Http 节点的 body
     * @param configs Http 节点的 configs
     */
    @SuppressWarnings("unchecked")
    private List<WorkflowFieldVO> adaptBody(Map<String, Object> data, Map<String, Object> httpBody, Map<String, Object> configs,
        Map<String, WorkflowNodeVO> workflowNodeMap) {
        // 转换类型：如果是none 或者 json， 保留原来的类型；否则，转换成 none
        String type = httpBody.getOrDefault("type", "").toString();
        HttpBodyType bodyType = HttpBodyType.fromValue(type);

        String requestBodyValue = "";

        if (bodyType == HttpBodyType.NONE) {
            configs.put(CommonConstant.DIFY.REQUEST_TYPE, HttpBodyType.NONE.name());
        } else {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) httpBody.getOrDefault("data",
                new ArrayList<>());
            configs.put(CommonConstant.DIFY.REQUEST_TYPE, HttpBodyType.JSON.name());
            if (!dataList.isEmpty()) {
                Map<String, Object> bodyData = dataList.get(0);
                String bodyValue = bodyData.getOrDefault("value", "").toString();

                // 把引用转换成固定值，比如{{_xxx_yyy}},同时把引用放进输入参数里面
                Map<String, Set<List<String>>> idNameMap = new HashMap<>();
                requestBodyValue = extractAndReplace(bodyValue, idNameMap);
                configs.put(CommonConstant.DIFY.REQUEST_BODY, requestBodyValue);
                return convertToFieldVOs(idNameMap, workflowNodeMap);
            }
        }
        configs.put(CommonConstant.DIFY.REQUEST_BODY, requestBodyValue);
        return new ArrayList<>();
    }

    /**
     * 转换异常处理（只支持 None / Default-Value，其他的不支持）
     * @param data Http 节点的 data
     * @param configs Http 节点的 configs
     */
    @SuppressWarnings("unchecked")
    public void adaptException(Map<String, Object> data, Map<String, Object> configs) {
        // 转化异常处理类型
        String errorStrategy = data.getOrDefault("error_strategy", "").toString();
        DifyHttpExceptionType strategyType = DifyHttpExceptionType.fromValue(errorStrategy);

        // 构建异常处理 body
        String exceptionSuppression = EXCEPTION_SUPPRESSION;
        if (strategyType.isEnable()) {
            Object defaultValObj = data.getOrDefault("default_value", new ArrayList<>());
            if (defaultValObj instanceof List) {
                List<Map<String, Object>> defaultList = (List<Map<String, Object>>) defaultValObj;
                exceptionSuppression = buildSuppressionJson(defaultList);
            }
        }

        // 更新到configs
        configs.put(CommonConstant.DIFY.EXCEPTION_ENABLE, strategyType.isEnable());
        configs.put(CommonConstant.DIFY.EXCEPTION_SUPPRESSION, exceptionSuppression);
    }

    /**
     * 转换鉴权信息
     * 处理方式：
     *  1. None -> None
     *  2. ApiKey Basic -> ApiKey: Headers key = Authorization , value = xxx
     *  3. ApiKey Bearer -> ApiKey: Headers key = Authorization, value = Bearer xxx
     *  4. ApiKey Custom -> Apikey: Headers key = xxx, value = yyy
     * @param data Http 节点的 data
     * @param configs Http 节点的 configs
     */
    @SuppressWarnings("unchecked")
    private void adaptAuth(Map<String, Object> data, Map<String, Object> configs) {
        Map<String, Object> rawAuthInfo = (Map<String, Object>) data.getOrDefault("authorization", new HashMap<>());

        // 没有 API 鉴权
        if (rawAuthInfo.isEmpty() || DifyAuthType.fromValue(
            rawAuthInfo.getOrDefault("type", "").toString()) == DifyAuthType.NO_AUTH) {
            configs.put(CommonConstant.DIFY.AUTH_INFO, new HashMap<>());
            return;
        }

        // 有 API 鉴权，但没有配置
        Map<String, Object> config = (Map<String, Object>) rawAuthInfo.getOrDefault("config", new HashMap<>());
        if (config == null || config.isEmpty()) {
            configs.put(CommonConstant.DIFY.AUTH_INFO, new HashMap<>());
            return;
        }

        // 有 API 鉴权，用对应的模式（basic/custom/bearer）转换鉴权信息
        String difyAuthTypeStr = String.valueOf(config.getOrDefault("type", ""));
        DifyAuthType authType = DifyAuthType.fromValue(difyAuthTypeStr);

        Map<String, String> keyPair = new HashMap<>();
        authType.fillKeyPair(keyPair, config);

        Map<String, Object> processedAuthInfo = new HashMap<>();
        processedAuthInfo.put("auth_keys", Collections.singletonList(keyPair));
        processedAuthInfo.put("domain", "HEADERS");
        processedAuthInfo.put("scope", "SERVICE");

        configs.put(CommonConstant.DIFY.AUTH_INFO, processedAuthInfo);
    }

    /**
     * 转换异常处理-默认值，将 body，status-code，headers 拼接成 Json
     * @param defaultList Dify DSL 中 Http 节点的异常处理字段及其对应的值
     * @return 返回拼接好的Json
     */
    private String buildSuppressionJson(List<Map<String, Object>> defaultList) {
        // 初始化
        ObjectNode suppressionNode = mapper.createObjectNode();
        suppressionNode.put(CommonConstant.DIFY.BODY, "");
        suppressionNode.put(CommonConstant.DIFY.STATUS_CODE, 0);
        suppressionNode.put(CommonConstant.DIFY.HEADERS, "");

        // 把 dify 设置的 exception suppression 替换 suppressionNode 初始化的值
        for (Map<String, Object> item : defaultList) {
            String key = String.valueOf(item.get("key"));
            Object value = item.get("value");
            if (value == null) {
                continue;
            }

            switch (key) {
                case CommonConstant.DIFY.BODY:
                    suppressionNode.put(CommonConstant.DIFY.BODY, String.valueOf(value));
                    break;
                case CommonConstant.DIFY.STATUS_CODE:
                    suppressionNode.put(CommonConstant.DIFY.STATUS_CODE, Integer.parseInt(value.toString()));
                    break;
                case CommonConstant.DIFY.HEADERS:
                    String headerStr = String.valueOf(value);
                    try {
                        JsonNode headerNode = mapper.readTree(headerStr);
                        if (headerNode.isObject() && !headerNode.isEmpty()) {
                            suppressionNode.set(CommonConstant.DIFY.HEADERS, headerNode);
                        } else {
                            suppressionNode.put(CommonConstant.DIFY.HEADERS, "");
                        }
                    } catch (Exception e) {
                        suppressionNode.put(CommonConstant.DIFY.HEADERS, headerStr);
                    }
                    break;
                default:
                    break;
            }
        }
        return suppressionNode.toString();
    }


    /**
     * 构建 Output WorkflowFieldVO
     * @param name output WorkflowFieldVO 的名字
     * @param type output WorkflowFieldVO 的类型
     * @param description output WorkflowFieldVO 的描述
     * @return 填入对应数据后的 WorkflowFieldVO
     */
    private WorkflowFieldVO createOutputWorkflowFieldVO(String name, String type, String description) {
        WorkflowFieldVO field = new WorkflowFieldVO().setName(name)
                                    .setType(type)
                                    .setDescription(description)
                                    .setRequired(true)
                                    .setSource(WorkflowFieldVO.SourceEnum.SYSTEM)
                                    .setReflection(false);

        // 构造内部的 value 结构
        WorkflowFieldVOValue valueMap = new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                                            .setHint("");

        // error_message 的结构稍有不同，增加 content 字段
        if (CommonConstant.DIFY.ERROR_MESSAGE.equals(name)) {
            valueMap.setContent("");
        } else {
            valueMap.setDefault("");
        }

        field.setValue(valueMap);
        return field;
    }


    /**
     * 处理 agent arts 不支持引用，但是 dify 支持引用的场景
     * 处理方式：
     *  1.固定值 -> 固定值
     *  2.引用（如果可以获取到值） -> 固定值
     *  3.引用（如果获取不到值）-> {{xxx.yyy}}
     * @param rawValue 原始 String
     * @param workflowNodeMap 已经解析好的节点（key:节点名字，value：节点）
     * @return 把引用全部替换成固定值的 String
     */
    private String replaceReferenceWithFixedValue(String rawValue, Map<String, WorkflowNodeVO> workflowNodeMap) {
        if (StringUtils.isBlank(rawValue)) {
            return "";
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(rawValue);

        // 没有引用
        if (!matcher.find()) {
            return rawValue;
        }

        // 使用了引用
        matcher.reset();
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            sb.append(rawValue, lastEnd, matcher.start());

            String nodeId = matcher.group(1);
            String varName = matcher.group(2);

            String resolvedValue = getResolvedReferenceValue(nodeId, varName, workflowNodeMap);
            sb.append(resolvedValue);

            lastEnd = matcher.end();
        }
        // 添加剩余文本
        sb.append(rawValue.substring(lastEnd));

        return sb.toString();
    }

    /**
     * 把引用转换成固定的值，包含ENV_VARIABLE，SYS_VARIABLE，CONVERSATION_VARIABLE和其他引用
     * 如果没有固定的值，则用 {{sys.project_id}} 这种形式代替
     * @param nodeId 节点 id
     * @param varName 引用变量名
     * @param workflowNodeMap 已经解析好的节点（key:节点名字，value：节点）
     * @return 把引用变成固定值后的 String
     */
    private String getResolvedReferenceValue(String nodeId, String varName, Map<String, WorkflowNodeVO> workflowNodeMap) {
        if (CommonConstant.DIFY.ENV_VARIABLE.equalsIgnoreCase(nodeId)) {
            return getResolvedEnvReferenceValue(varName, workflowNodeMap).toString();
        } else if (CommonConstant.DIFY.SYS_VARIABLE.equalsIgnoreCase(nodeId)) {
            return getResolvedSysReferenceValue(varName, workflowNodeMap);
        } else if (CommonConstant.DIFY.CONVERSATION_VARIABLE.equalsIgnoreCase(nodeId)) {
            return getResolvedConversationReferenceValue(varName, workflowNodeMap);
        }

        // 处理普通节点变量
        return "{{node_" + nodeId + "." + varName + "}}";
    }

    /**
     * 把系统变量引用转换成固定的值 （如果没有固定的值，则用 {{sys.project_id}} 这种形式代替
     * @param varName 系统变量引用的key
     * @param workflowNodeMap 已经解析好的节点（key:节点名字，value：节点）
     * @return 转换好的固定值
     */
    @SuppressWarnings("unchecked")
    private String getResolvedSysReferenceValue(String varName, Map<String, WorkflowNodeVO> workflowNodeMap) {
        WorkflowNodeVO startNode = workflowNodeMap.get(CommonConstant.DIFY.SYS_VARIABLE);
        if (startNode != null && startNode.getOutputs() != null) {
            for (WorkflowFieldVO output : startNode.getOutputs()) {
                if (CommonConstant.DIFY.SYS_VARIABLE.equals(output.getName()) && output.getSchema() != null) {
                    List<WorkflowFieldVO> sysVars = (List<WorkflowFieldVO>) output.getSchema();

                    for (WorkflowFieldVO sysVar : sysVars) {
                        if (sysVar.getName().equals(varName) && sysVar.getValue() != null) {
                            Object content = sysVar.getValue().getContent();
                            return content.toString();
                        }
                    }
                    break;
                }
            }
        }
        return "{{" + CommonConstant.DIFY.SYS_VARIABLE + "." + varName + "}}";
    }

    /**
     * 把会话变量引用转换成固定的值 （如果没有固定的值，则用 {{memory.project_id}} 这种形式代替）
     * 由于 agent arts 没有会话变量，因此会话变量是放在记忆变量那边，因此引用是通过 memory.xxx 来引用
     * @param varName 会话变量引用的key
     * @param workflowNodeMap 已经解析好的节点（key:节点名字，value：节点）
     * @return 转换好的固定值
     */
    private String getResolvedConversationReferenceValue(String varName, Map<String, WorkflowNodeVO> workflowNodeMap) {
        WorkflowNodeVO conversationNode = workflowNodeMap.get(CommonConstant.DIFY.CONVERSATION_VARIABLE);
        if (conversationNode != null && conversationNode.getOutputs() != null) {
            for (WorkflowFieldVO output : conversationNode.getOutputs()) {
                if (output.getName().equals(varName) && output.getValue() != null) {
                    return output.getValue().getDefault().toString();
                }
            }
        }
        return "{{memory." + varName + "}}";
    }

    /**
     * 处理 agent arts支持引用，dify 支持引用的场景
     * dify 支持多个引用，且支持引用和固定值混搭；agent arts 只能最多一个引用，且引用和固定值不能混搭
     * 处理方式：
     *  1. 固定值 -> 固定值
     *  2. 单个引用 -> 单个引用
     *  3. 多个引用 -> 转换成 {{xxx}}{{yyy}} 的形式返回
     */
    private WorkflowFieldVOValue transformReferenceToSupportedFormat(String rawValue,
        Map<String, WorkflowNodeVO> workflowNodeMap) {
        if (StringUtils.isBlank(rawValue)) {
            return createLiteralValue("");
        }

        List<ReferenceMatch> matches = findAllReferenceMatches(rawValue);

        // 只有固定值
        if (matches.isEmpty()) {
            return createLiteralValue(rawValue);
        }

        // 只有一个引用
        if (matches.size() == 1 && isPureReference(rawValue, matches.get(0))) {
            ReferenceMatch match = matches.get(0);

            // 如果是环境变量，换成固定的值
            // 如果是其他变量，先检查有没有这个变量。如果有，则用引用；如果没有，返回{{xxx}}的固定值
            if (CommonConstant.DIFY.ENV_VARIABLE.equalsIgnoreCase(match.nodeId)) {
                Object envValue = getResolvedEnvReferenceValue(match.varName, workflowNodeMap);
                return createLiteralValue(envValue);

            } else if (CommonConstant.DIFY.SYS_VARIABLE.equalsIgnoreCase(match.nodeId) ||
                CommonConstant.DIFY.CONVERSATION_VARIABLE.equalsIgnoreCase(match.nodeId)) {
                WorkflowNodeVO workflowNodeVO = workflowNodeMap.get(match.nodeId);
                List<String> refInfo = new ArrayList<>();
                refInfo.add(match.nodeId);
                refInfo.add(match.varName);
                return adaptWorkflowFieldVoValue(refInfo, workflowNodeMap, workflowNodeVO);
            }

            // 如果是其他变量，换成引用
            WorkflowNodeVO refNode = workflowNodeMap.get(match.nodeId);
            List<String> refInfo = new ArrayList<>();
            refInfo.add(match.nodeId);
            refInfo.add(match.varName);
            return adaptWorkflowFieldVoValue(refInfo, workflowNodeMap, refNode);
        }

        // 混合(固定值+引用）或多引用：agents 不支持混合引用，强制转为固定值 (Literal)
        // 替换环境变量为真实值，替换其他变量为 {{xxx}} 占位符
        String processedContent = replaceReferenceWithFixedValue(rawValue, workflowNodeMap);
        return createLiteralValue(processedContent);
    }

    /**
     * 创建 Literal 形式的 Value，用于 Inputs 中
     */
    private WorkflowFieldVOValue createLiteralValue(Object content) {
        return new WorkflowFieldVOValue()
            .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
            .setContent(content)
            .setHint("");
    }

    /**
     * 搜索出所有引用，每个引用用一个 ReferenceMatch 记录，最终返回一个 List
     */
    private List<ReferenceMatch> findAllReferenceMatches(String rawValue) {
        List<ReferenceMatch> matches = new ArrayList<>();
        if (StringUtils.isBlank(rawValue)) {
            return matches;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(rawValue);
        while (matcher.find()) {
            ReferenceMatch match = new ReferenceMatch();
            match.nodeId = matcher.group(1);
            match.varName = matcher.group(2);
            match.start = matcher.start();
            match.end = matcher.end();
            matches.add(match);
        }
        return matches;
    }

    /**
     * 判断是否是单个引用，且不含其他固定值
     */
    private boolean isPureReference(String raw, ReferenceMatch referenceMatch) {
        return raw.length() == (referenceMatch.end - referenceMatch.start);
    }

    /**
     * 解析 URL 结构，拆分 scheme, endpoint 和 path
     */
    private UrlInfo parseUrl(String url, Map<String, WorkflowNodeVO> workflowNodeMap) {
        // 1. 变量替换
        String processedUrl = replaceReferenceWithFixedValue(url, workflowNodeMap);
        String urlWithoutQuery = processedUrl;

        UrlInfo info = new UrlInfo();
        info.setProcessedUrl(processedUrl);

        if (StringUtils.isNotBlank(processedUrl)) {
            // 2. 剥离 Query 参数（不参与 endpoint/path 的解析）
            int questionMarkIndex = processedUrl.indexOf('?');
            if (questionMarkIndex != -1) {
                urlWithoutQuery = processedUrl.substring(0, questionMarkIndex);
                info.setQuery(processedUrl.substring(questionMarkIndex + 1));
            }

            // 3. 解析协议、域名和路径
            if (urlWithoutQuery.contains("://")) {
                int protocolEnd = urlWithoutQuery.indexOf("://");
                info.setScheme(urlWithoutQuery.substring(0, protocolEnd));

                String rest = urlWithoutQuery.substring(protocolEnd + 3);
                int firstSlash = rest.indexOf('/');

                if (firstSlash == -1) {
                    info.setEndpoint(rest);
                    info.setPath("");
                } else {
                    info.setEndpoint(rest.substring(0, firstSlash));
                    info.setPath(rest.substring(firstSlash));
                }
            } else {
                // 处理没有协议头的情况
                info.setPath(urlWithoutQuery);
            }
        }
        return info;
    }
}
