package com.example.home.gohoy.k8s_backend.utils;

import lombok.Data;

@Data
public  class CommonResult<T> {
    private int code;
    private T data;
    private String message;

    public CommonResult code(int code){
        this.code = code;
        return this;
    }
    public CommonResult data(T data){
        this.data = data;
        return this;
    }
    public CommonResult message(String message){
        this.message = message;
        return this;
    }
}
