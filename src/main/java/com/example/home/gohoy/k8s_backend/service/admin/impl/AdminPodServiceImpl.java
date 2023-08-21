package com.example.home.gohoy.k8s_backend.service.admin.impl;

import com.example.home.gohoy.k8s_backend.dto.PodResourceDTO;
import com.example.home.gohoy.k8s_backend.entities.PodInfo;
import com.example.home.gohoy.k8s_backend.service.admin.AdminPodService;
import com.example.home.gohoy.k8s_backend.utils.PodCURD;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.home.gohoy.k8s_backend.utils.PodCURD.*;

@Service
public class AdminPodServiceImpl implements AdminPodService {
    private final KubernetesClient kubernetesClient;

    public AdminPodServiceImpl(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public List<PodInfo> getPodByPage(String type, KubernetesClient kubernetesClient) {
        List<PodInfo> filteredPods = new ArrayList<>();
        PodList podList = kubernetesClient.pods().inAnyNamespace().list();

        for (Pod pod : podList.getItems()) {
            String podName = getPodName(pod);
            // 判断是否是系统Pod（这里简单假设系统Pod的名称以"kube-"开头）
            if (podName.startsWith("kube-") || podName.startsWith("calico-") || podName.startsWith("coredns-")||podName.startsWith("etcd-")||podName.startsWith("tigera-")) {
                continue; // 排除系统Pod
            }
            int startIndex = podName.indexOf("-") + 1;
            int endIndex = podName.indexOf("-", startIndex);
            String type1 = podName.substring(startIndex, endIndex);
            if(!type1.equals(type)){
                continue;
            }
            PodInfo podInfo = new PodInfo();
            podInfo.setName(podName);
            podInfo.setStatus(getStatus(pod));
            podInfo.setNamespace(getNameSpace(pod));
            podInfo.setCpu(getPodCpu(pod));
            podInfo.setMemory(getPodMemory(pod));
            podInfo.setIp(getIp(pod));
            podInfo.setSshPort(new PodCURD(kubernetesClient).getSshPort(pod));
            podInfo.setTtl(getTTL(pod));
            filteredPods.add(podInfo);
        }
        return filteredPods;


    }

    @Override
    public boolean createOrUpdateConfigMap(String configMapName, PodResourceDTO podResourceDTO, KubernetesClient kubernetesClient) {
        String startPort = podResourceDTO.getPort();
        Namespace config =    kubernetesClient.namespaces().withName("default").get();
        if(config == null){
            config = new Namespace();
            ObjectMeta metadata = new ObjectMeta();
            metadata.setName("default");
            config.setMetadata(metadata);
            kubernetesClient.resource(config).createOrReplace();
        }
        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new ObjectMeta());
        configMap.getMetadata().setName(configMapName);
        ConfigMap existMap = kubernetesClient.configMaps().inNamespace("default").withName(configMapName).get();
        String portValue;
        if(existMap != null){
            portValue = existMap.getData().get("port");
        }else{
            portValue = startPort;
        }
        configMap.setData(Map.of("cpu", podResourceDTO.getCpuValue(), "memory", podResourceDTO.getMemoryValue(), "timeOfLife",podResourceDTO.getTimeOfLife() , "port", portValue));
        ConfigMap map = kubernetesClient.resource(configMap).inNamespace("default").createOrReplace();
        return map != null;
    }
}
