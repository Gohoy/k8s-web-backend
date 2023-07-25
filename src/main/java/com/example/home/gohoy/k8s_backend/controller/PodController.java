package com.example.home.gohoy.k8s_backend.controller;

import com.example.home.gohoy.k8s_backend.POJO.PodInfo;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/k8s/")
public class PodController {

    private final KubernetesClient kubernetesClient;

    public PodController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @GetMapping("/pods")
    public List<Map<String, String>> getRunningPods() {
        List<Map<String, String>> podInfoList = new ArrayList<>();
        List<Pod> pods = kubernetesClient.pods().inAnyNamespace().list().getItems();

        for (Pod pod : pods) {
            if (isPodRunning(pod)) {
                Map<String, String> podInfo = new HashMap<>();
                podInfo.put("name", pod.getMetadata().getName());
                podInfo.put("namespace", pod.getMetadata().getNamespace());
                podInfo.put("cpu", getPodCpu(pod));
                podInfo.put("memory", getPodMemory(pod));
                podInfo.put("storage", getPodStorage(pod));
                podInfoList.add(podInfo);
            }
        }

        return podInfoList;
    }

    private boolean isPodRunning(Pod pod) {
        return "Running".equals(pod.getStatus().getPhase());
    }

    private String getPodCpu(Pod pod) {
        Quantity cpu = pod.getSpec().getContainers().get(0).getResources().getLimits().get("cpu");
        return cpu != null ? cpu.getAmount() : "Not specified";
    }

    private String getPodMemory(Pod pod) {
        Quantity memory = pod.getSpec().getContainers().get(0).getResources().getLimits().get("memory");
        return memory != null ? memory.getAmount() : "Not specified";
    }

    private String getPodStorage(Pod pod) {
        Quantity storage = pod.getSpec().getContainers().get(0).getResources().getRequests().get("storage");
        return storage != null ? storage.getAmount() : "Not specified";
    }

    @GetMapping("/pod/")
    public List<PodInfo> getFilteredPodsByPrefix(@RequestParam(name = "namespace") String namespace,
                                                 @RequestParam(name = "podPrefix") String podPrefix) {
        List<PodInfo> filteredPods = new ArrayList<>();
        PodList podList = kubernetesClient.pods().inNamespace(namespace).list();

        for (Pod pod : podList.getItems()) {
            String podName = pod.getMetadata().getName();

            // 判断是否是系统Pod（这里简单假设系统Pod的名称以"kube-"开头）
            if (podName.startsWith("kube-")) {
                continue; // 排除系统Pod
            }

            if (podName.startsWith(podPrefix)) {
                PodInfo podInfo = new PodInfo();
                podInfo.setName(podName);
                podInfo.setPhase(pod.getStatus().getPhase());
                podInfo.setNamespace(pod.getMetadata().getNamespace());
                podInfo.setCpu(getPodCpu(pod));
                podInfo.setMemory(getPodMemory(pod));
                podInfo.setStorage(getPodStorage(pod));

                filteredPods.add(podInfo);
            }
        }

        return filteredPods;
    }

}
