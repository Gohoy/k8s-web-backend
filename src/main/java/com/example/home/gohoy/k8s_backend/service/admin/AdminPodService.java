package com.example.home.gohoy.k8s_backend.service.admin;

import com.example.home.gohoy.k8s_backend.dto.PodResourceDTO;
import com.example.home.gohoy.k8s_backend.entities.PodInfo;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;

public interface AdminPodService {
    //分页查询ctr或vm信息
    public List<PodInfo> getPodByPage(String type, KubernetesClient kubernetesClient);

    //
    public boolean createOrUpdateConfigMap(String configMapName, PodResourceDTO podResourceDTO, KubernetesClient kubernetesClient);

}
