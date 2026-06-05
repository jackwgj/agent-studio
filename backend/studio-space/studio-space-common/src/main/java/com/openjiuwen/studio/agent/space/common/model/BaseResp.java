package com.openjiuwen.studio.agent.space.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.springframework.http.HttpStatus;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResp<T> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    public static <T> BaseResp<T> success(T data) {
        return BaseResp.<T>builder().data(data).code(String.valueOf(HttpStatus.OK.value())).build();
    }

    public static <T> BaseResp<T> fail(String code, String message) {
        return BaseResp.<T>builder().code(code).message(message).build();
    }

    public static <T> BaseResp<T> fail(AgentSpaceErrorCodes errorCodes, T data) {
        BaseResp<T> rsp = new BaseResp<>();
        rsp.setCode(errorCodes.errorCode());
        rsp.setMessage(errorCodes.errorMsg());
        rsp.setData(data);
        return rsp;
    }
}