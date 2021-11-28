package com.two.pojo;

import lombok.Data;

import java.util.List;

@Data
public class QueryDeviceData {
    private String status;
    private String totalRecord;
    private List<Querydata> data;

    public String getStatus() {
        return status;
    }

    public String getTotalRecord() {
        return totalRecord;
    }

    public List<Querydata> getData() {
        return data;
    }
}
