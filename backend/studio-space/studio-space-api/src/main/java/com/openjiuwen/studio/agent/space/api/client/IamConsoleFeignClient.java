package com.openjiuwen.studio.agent.space.api.client;

import com.openjiuwen.studio.agent.space.common.feignclient.FeignConfig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Validated
@FeignClient(name = "IamConsoleFeignClient", url = "${iam.cas.endpoint}", configuration = {FeignConfig.class})
public interface IamConsoleFeignClient {
    @RequestMapping(value = "/v3/auth/tokens?nocatalog=false", method = RequestMethod.GET)
    ResponseEntity<String> ValidateToken(@RequestHeader Map<String, String> header);
}
