/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.serializer;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.util.Map;

@Component
public class YamlSerializer {

    private final Yaml yaml;

    public YamlSerializer() {
        // 加载器（反序列化）安全规则
        LoaderOptions loaderOptions = new LoaderOptions();
        // 安全构造器：仅允许基础类型反序列化
        SafeConstructor safeConstructor = new SafeConstructor(loaderOptions);
        this.yaml = new Yaml(safeConstructor);
    }

    public Map<String, Object> load(String s) {
        return yaml.load(s);
    }

    public Map<String, Object> load(InputStream io) {
        return yaml.load(io);
    }

    public String dump(Map<String, Object> data) {
        return yaml.dump(data);
    }
}
