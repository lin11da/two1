package com.two.untils;

import com.two.pojo.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class Userreturn<T> {
    private T data;

    private String code;
    private String message;
    private Boolean success;


    public Userreturn(Map map) {
        this.success = true;
        this.code = "200";
        this.message = "操作成功";
        this.data = (T) map;
    }

    public Userreturn(User user) {
        this.success = true;
        this.code = "200";
        this.message = "操作成功";
        this.data = (T) user;
    }


    public Userreturn() {
        this.success = true;
        this.code = "200";
        this.message = "操作成功";
        this.data = (T) "";
    }

    public Userreturn(String message) {
        this.success = false;
        this.code = "201";
        this.message = message;
        this.data = (T) "";
    }

    public Userreturn(int data) {
        this.success = false;
        this.code = "403";
        this.message = "权限不足";
        this.data = (T) "";
    }


    public Userreturn(T data) {
        this.success = true;
        this.code = "200";
        this.message = "操作成功";
        this.data = data;


    }

    public Userreturn(double mate) {
        this.success = false;
        this.code = "401";
        this.message = "账号登录信息已过期，请重新登录";
        this.data = null;
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }


}
