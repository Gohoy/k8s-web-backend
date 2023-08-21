package com.example.home.gohoy.k8s_backend.service.user;

import com.example.home.gohoy.k8s_backend.entities.PodInfo;

import java.util.List;

public interface PodService {

    List<PodInfo> getPodsByUserName (String userName);
    //获取配置并且创建pod
    int getConfigAndCreatePod(String podName, String configName, String imageName);
}
