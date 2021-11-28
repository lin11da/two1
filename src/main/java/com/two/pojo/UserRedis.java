package com.two.pojo;

import lombok.Data;

@Data
public class UserRedis {
    private String userRedis;

    public String getUserRedis() {
        return userRedis;
    }

    public void setUserRedis(String userRedis) {
        this.userRedis = userRedis;
    }

}
