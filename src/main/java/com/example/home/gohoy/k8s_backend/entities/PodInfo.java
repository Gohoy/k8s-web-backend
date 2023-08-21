package com.example.home.gohoy.k8s_backend.entities;

import lombok.Data;

@Data
public class PodInfo {
    private String name;
    private String status;
    private String  cpu;
    private String memory;
    private String storage;
    private String namespace;
    private String ip;
    private String sshPort;

    private String ttl;
    // 可根据需要添加其他字段的getter和setter方法
}
