package com.wzp.entity;

import java.io.Serializable;

/**
 * 返回封装结果
 */
public class Result implements Serializable {
    private boolean success ;//封装结果
    private String message;//封装结果信息

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Result() {
    }
}
