package com.gateway.payloads;

public class DataPayload {
    public String location;
    public String name;
    public String id;
    public String date;

    @Override
    public String toString() {
        return "DataPayload {" +
                "location='" + location + "'" +
                ", name='" + name + "'" +
                ", id='" + id + "'" +
                ", id='" + id + "'" +
                '}';
    }
}
