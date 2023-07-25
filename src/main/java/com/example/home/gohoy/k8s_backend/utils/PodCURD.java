package com.example.home.gohoy.k8s_backend.utils;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;

public class PodCURD {
    public static String getPodCpu(Pod pod) {
        Quantity cpu = pod.getSpec().getContainers().get(0).getResources().getLimits().get("cpu");
        return cpu != null ? cpu.getAmount() : "Not specified";
    }

    public static String getPodMemory(Pod pod) {
        Quantity memory = pod.getSpec().getContainers().get(0).getResources().getLimits().get("memory");
        return memory != null ? memory.getAmount() : "Not specified";
    }

    public static String getPodStorage(Pod pod) {
        Quantity storage = pod.getSpec().getContainers().get(0).getResources().getRequests().get("storage");
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
}
