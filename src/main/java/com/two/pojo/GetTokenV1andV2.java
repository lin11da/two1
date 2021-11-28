package com.two.pojo;

import lombok.Data;

@Data
public class GetTokenV1andV2 {
    private String status;
    private String totalRecord;
    private String data;

    public String getStatus() {
        return status;
    }

    public String getTotalRecord() {
        return totalRecord;
    }

    public String getData() {
        return data;
    }
}
