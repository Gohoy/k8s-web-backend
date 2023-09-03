package com.example.home.gohoy.k8s_backend.service.user;

import com.example.home.gohoy.k8s_backend.entities.PodInfo;
import io.fabric8.kubernetes.api.model.batch.v1.Job;

import java.util.List;

public interface PodService {

    List<PodInfo> getPodsByUserName (String userName);
    //获取配置并且创建pod
    int getConfigAndCreatePod(String podName, String configName, String imageName);

    Job createJob(String podName,String cpu,String memory,Long timeOfLife, String imageName);
}
