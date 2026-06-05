/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.StrUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.rce.models.AskDatasourceReq;
import com.openjiuwen.studio.agent.manager.rce.models.DatasourceExecuteRsp;
import com.openjiuwen.studio.agent.manager.rce.models.DatasourceStatusCheckRsp;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

/**
 * 数据源状态健康检查服务
 *
 */
@Slf4j
@Service
public class DatasourceStatusCheckService {
    /**
     * 数据源检查接口
     */
    private static final String URL_TEMPLATE = "%s/v1/datasource/%s/execute";

    /**
     * 数据源检查语句
     */
    private static final String CHECK_QUERY = "select 1";

    /**
     * 数据源执行响应体字段
     */
    private static final String OUTPUT_LIST = "output_list";

    private static final String ROW_NUM = "row_num";

    /**
     * Content-Type
     */
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * application/json
     */
    private static final String APPLICATION_JSON = "application/json";

    /**
     * MediaType
     */
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * 错误信息
     */
    private static final String CONNECTION_ERROR = "connection to datasource failed!";

    /**
     * 最大错误描述长度
     */
    private static final int MAX_ERROR_MESSAGE_SIZE = 2048;

    @Value("${feign.client.config.agentRuntime.url}")
    private String agentRuntimeEndpoint;

    @Value("${datasource.check.pool-size:15}")
    private int datasourceCheckPoolSize;

    @Value("${datasource.check.timeout:25}")
    private int datasourceCheckTimeOut;

    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    private OkHttpClientUtils okHttpClientUtils;

    private ExecutorService fixedThreadPool;

    @PostConstruct
    private void init() {
        fixedThreadPool = Executors.newFixedThreadPool(datasourceCheckPoolSize);
    }

    /**
     * 数据源检查接口
     *
     * @param datasourceId 数据源id
     * @return 数据源响应结果
     */
    public DatasourceStatusCheckRsp datasourceStatusCheck(String datasourceId) {
        // 数据源检查接口响应
        DatasourceStatusCheckRsp datasourceStatusCheckRsp =
            DatasourceStatusCheckRsp.builder().status(CommonConstant.FAILED).build();

        String authToken = RequestContextUtils.getRequestAuthToken();
        try (Response response = requestDatasource(datasourceId, buildDatasourceReq(), authToken)) {
            if (response.isSuccessful()) {
                datasourceStatusCheckRsp.setStatus(CommonConstant.SUCCESS);
            } else {
                if (response.body() != null) {
                    JSONObject body = JSONObject.parseObject(response.body().string());
                    datasourceStatusCheckRsp.setErrorMessage(
                        StrUtils.truncateText(body.getString(CommonConstant.ERROR_MSG), MAX_ERROR_MESSAGE_SIZE));
                } else {
                    datasourceStatusCheckRsp.setErrorMessage(
                        String.format("error code %s, datasource check response body is null", response.code()));
                }
            }
        } catch (AgentStudioException e) {
            log.error("Call datasource failed", e);
            datasourceStatusCheckRsp.setErrorMessage(i18nUtil.getMessage(e.getErrorCode(), e.getArgs()));
        } catch (Exception e) {
            log.error("Call datasource failed", e);
            datasourceStatusCheckRsp.setErrorMessage(CONNECTION_ERROR);
        }
        return datasourceStatusCheckRsp;
    }

    /**
     * 获取数据源表格列表
     * @param datasourceId 数据源ID
     * @return
     */
    public DatasourceExecuteRsp retrieveDatasourceBySql(String datasourceId, String query, List<String> conditions) {
        String authToken = RequestContextUtils.getRequestAuthToken();
        DatasourceExecuteRsp datasourceExecuteRsp = DatasourceExecuteRsp.builder().build();
        try (Response response = requestDatasource(datasourceId, buildDatasourceReq(query, conditions), authToken)) {
            if (response.isSuccessful()) {
                JSONObject body = JSONObject.parseObject(response.body().string());
                datasourceExecuteRsp.setOutputList(JSONArray.from(body.get(OUTPUT_LIST)));
                datasourceExecuteRsp.setRowNum(body.getInteger(ROW_NUM));
            } else {
                log.error("Call datasource failed, response code: {}, msg: {}", response.code(), response.body());
                throw new AgentStudioException(StudioError.DATASOURCE_CALL_FAILED);
            }
        } catch (AgentStudioException e) {
            log.error("Call datasource failed", e);
            throw new AgentStudioException(e.getErrorCode());
        } catch (Exception e) {
            log.error("Call datasource failed", e);
            throw new AgentStudioException(StudioError.DATASOURCE_CALL_FAILED);
        }
        return datasourceExecuteRsp;
    }

    /**
     * 构建健康检查请求
     *
     * @return 数据源健康检查请求
     */
    private AskDatasourceReq buildDatasourceReq() {
        return AskDatasourceReq.builder().query(CHECK_QUERY).shouldByCache(false).build();
    }

    /**
     * 构造自定义的SQL查询请求
     * @param sqlQuery 查询的SQL语句
     * @return 执行SQL的结果
     */
    private AskDatasourceReq buildDatasourceReq(String sqlQuery, List<String> conditions) {
        return AskDatasourceReq.builder().query(sqlQuery).shouldByCache(false).conditions(conditions).build();
    }

    private Response requestDatasource(String datasourceId, AskDatasourceReq body, String authToken) {
        RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(body), MEDIA_TYPE_JSON);
        Request.Builder builder = new Request.Builder();
        builder.addHeader(CONTENT_TYPE, APPLICATION_JSON);
        if (StringUtils.isNotEmpty(authToken)) {
            builder.addHeader(CommonConstant.X_AUTH_TOKEN, authToken);
        }
        String url = String.format(URL_TEMPLATE, agentRuntimeEndpoint, datasourceId);
        Request request = builder.url(url).post(requestBody).build();
        OkHttpClient httpClient = okHttpClientUtils.getHttpClient();

        // 调用模型
        Future<Response> future = null;
        try {
            future = fixedThreadPool.submit(() -> httpClient.newCall(request).execute());
            return future.get(datasourceCheckTimeOut, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("datasource response timeout!", e);
            future.cancel(true);
            throw new AgentStudioException(StudioError.DATASOURCE_CHECK_TIMEOUT);
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.DATASOURCE_CHECK_CONNECTION_FAILED);
        }
    }
}
