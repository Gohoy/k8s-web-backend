package com.example.home.gohoy.k8s_backend.utils;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Map;

public class PodCURD {

    private final KubernetesClient kubernetesClient;


    public PodCURD(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public static String getPodCpu(Pod pod) {
        Quantity cpu = pod.getSpec().getContainers().get(0).getResources().getLimits().get("cpu");
        return cpu != null ? cpu.getAmount() : "Not specified";
    }

    public static String getPodMemory(Pod pod) {
        Quantity memory = pod.getSpec().getContainers().get(0).getResources().getLimits().get("memory");
        return memory != null ? memory.getAmount() : "Not specified";
    }

    public  String getPodStorage(Pod pod) {
        String podName = pod.getMetadata().getName();
        int startIndex = podName.indexOf("-job-");
        // 提取子字符串
         podName = podName.substring(0, startIndex);
        String pvcName = podName+"-pvc";
        PersistentVolumeClaim pvc = kubernetesClient.persistentVolumeClaims().inNamespace("default").withName(pvcName).get();
        Quantity storage = pvc.getSpec().getResources().getRequests().get("storage");
        return storage != null ? storage.getAmount() : "Not specified";
    }

    public static String getPodName(Pod pod){
        return pod.getMetadata().getName();
    }
    public static String getNameSpace(Pod pod){
        return pod.getMetadata().getNamespace();
    }
    public static String getStatus(Pod pod){
        return pod.getStatus().getPhase();
    }
    public static String getIp(Pod pod){
        return pod.getStatus().getHostIP();
    }
    public String getSshPort(Pod pod){
        String podName = pod.getMetadata().getName();
        int startIndex = podName.indexOf("-job-");
        // 提取子字符串
        podName = podName.substring(0, startIndex);
        String serviceName = podName+"-service";
        Service service = kubernetesClient.services().inNamespace("default").withName(serviceName).get();
        return service.getSpec().getPorts().get(0).getPort().toString();
    }
    public static String getTTL(Pod pod){
        Map<String, String> annotations = pod.getMetadata().getAnnotations();
        if (annotations != null && annotations.containsKey("ttlFinishedAt")) {
            String ttlFinishedAt = annotations.get("ttlFinishedAt");
            return ttlFinishedAt;
        }
        return "Not specified";
    }
}
