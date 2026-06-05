package com.openjiuwen.studio.agent.space.app.model.web;

public class UrlPattern {
    private String prefix = "";

    private String suffix = "";

    private boolean fullMatchModel;

    private String fullPath;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isFullMatchModel() {
        return fullMatchModel;
    }

    public void setFullMatchModel(boolean fullMatchModel) {
        this.fullMatchModel = fullMatchModel;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
}
