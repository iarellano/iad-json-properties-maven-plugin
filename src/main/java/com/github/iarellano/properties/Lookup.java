package com.github.iarellano.properties;

public class Lookup {

    private String jsonPath;

    private String propertyName;

    private boolean failFast = false;

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    @Override
    public String toString() {
        return "Lookup{" +
                "jsonPath='" + jsonPath + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", failFast=" + failFast +
                '}';
    }
}
