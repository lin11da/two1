package com.two.pojo;

import lombok.Data;

@Data
public class RootRedis {
    private String roottoken;
    private String accountid;

    public String getRoottoken() {
        return roottoken;
    }

    public void setRoottoken(String roottoken) {
        this.roottoken = roottoken;
    }

    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }
}
