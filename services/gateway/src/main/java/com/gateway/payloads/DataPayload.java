package com.gateway.payloads;

public class DataPayload {
    private String key;
    private String value;

    // Getters and setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DataPayload{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
