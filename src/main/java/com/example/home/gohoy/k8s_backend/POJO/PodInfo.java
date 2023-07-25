package com.example.home.gohoy.k8s_backend.POJO;

import lombok.Data;

@Data
public class PodInfo {
    private String name;
    private String phase;
    private String  cpu;
    private String memory;
    private String storage;
    private String namespace;

    // 可根据需要添加其他字段的getter和setter方法
}
