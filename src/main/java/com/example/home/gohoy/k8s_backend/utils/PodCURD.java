package com.example.home.gohoy.k8s_backend.utils;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
        if(startIndex == -1){
            return null;
        }
        // 提取子字符串
        podName = podName.substring(0, startIndex);
        String serviceName = podName+"-service";
        Service service = kubernetesClient.services().inNamespace("default").withName(serviceName).get();
        if(service != null){
            return service.getSpec().getPorts().get(0).getPort().toString();

        }else {
            return null;
        }
    }
    public static String getTTL(Pod pod){
        PodStatus status = pod.getStatus();
       if(pod.getStatus().getPhase().equals("Failed")){
           return "Expired";
       }
        if (status != null) {
            Instant startTime = Instant.parse(status.getStartTime());
            Instant now = Instant.now();
            Long ttlSeconds = pod.getSpec().getActiveDeadlineSeconds();
            if(ttlSeconds == null){
                return "Not specified";
            }
            Instant expirationTime = startTime.plusSeconds(ttlSeconds);
            System.out.println(pod.getMetadata().getName()+": " + expirationTime);
            System.out.println(pod.getMetadata().getName()+": "+ now);
            long remainingTimeSeconds = ChronoUnit.SECONDS.between(now, expirationTime);
            System.out.println(pod.getMetadata().getName()+": "+ remainingTimeSeconds);
            if (remainingTimeSeconds >= 0) {
                return remainingTimeSeconds + " seconds";
            } else {
                return "Expired";
            }
        }
        return "Not specified";
    }
}
