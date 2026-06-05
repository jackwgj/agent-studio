/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.contrlller;

import com.openjiuwen.studio.common.service.model.skuinst.LicenseInst;
import com.openjiuwen.studio.common.service.model.skuinst.LicenseReportReq;
import com.openjiuwen.studio.common.service.model.skuinst.ResourceInst;

import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * License查询、消费相关接口
 */
@RequestMapping("/v1/common-intern/license")
public interface ResourceInstInternApi {

    @GetMapping("/query-resource-insts")
    @ApiOperation("查询租户订购的所有有效SKU")
    ResponseEntity<List<ResourceInst>> queryResourceInsts(@RequestHeader(value = "x-auth-token", required = false) String token,
                                                          @RequestHeader(value = "domain-id", required = false) String domainId);

    @GetMapping("/query")
    @ApiOperation("查询租户订购的所有有效SKU")
    ResponseEntity<List<LicenseInst>> queryLicense(@Valid @Size(max = 64) @RequestParam(value = "resource_id", required = false) String resId,
                                                   @Valid @Size(max = 64) @RequestParam(value = "attr_code", required = false) String attrCode,
                                                   @Valid @Size(max = 64) @RequestParam(value = "sku_code", required = false) String skuCode,
                                                   @RequestHeader(value = "x-auth-token", required = false) String token,
                                                   @RequestHeader(value = "domain-id", required = false) String domainId);

    @PostMapping("/report")
    @ApiOperation("license消费接口")
    ResponseEntity<String> report(@RequestBody @Valid LicenseReportReq req,
                                  @RequestHeader(value = "x-auth-token", required = false) String token,
                                  @RequestHeader(value = "domain-id", required = false) String domainId);
}
