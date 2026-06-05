package com.openjiuwen.studio.agent.space.common.model;

public enum StreamRequestSupportType {
    POST("POST"),
    put("put");

    private String requestType;

    StreamRequestSupportType(String type) {
        this.requestType = type;
    }

    public String getSupportType() {
        return this.requestType;
    }
}
