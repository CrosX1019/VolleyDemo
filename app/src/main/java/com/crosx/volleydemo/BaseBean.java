package com.crosx.volleydemo;

/**
 * Created by CrosX on 2017/10/12.
 */

public class BaseBean<T> {

    private int status;

    private String desc;

    private T data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
