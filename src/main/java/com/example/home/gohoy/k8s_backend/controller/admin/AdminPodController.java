package com.example.home.gohoy.k8s_backend.controller.admin;

import com.example.home.gohoy.k8s_backend.POJO.PodInfo;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.example.home.gohoy.k8s_backend.utils.PodCURD.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/admin/pod/")
public class AdminPodController {
    private final KubernetesClient kubernetesClient;

    public AdminPodController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }
    @GetMapping("/allPods")
    public List<PodInfo> getRunningPods() {
        List<PodInfo> podInfoList = new ArrayList<>();
        List<Pod> pods = kubernetesClient.pods().inAnyNamespace().list().getItems();
        for (Pod pod : pods) {
            PodInfo podInfo = new PodInfo();
            podInfo.setName(getPodName(pod));
            podInfo.setStatus(getStatus(pod));
            podInfo.setNamespace(getNameSpace(pod));
            podInfo.setCpu(getPodCpu(pod));
            podInfo.setMemory(getPodMemory(pod));
            podInfo.setStorage(getPodStorage(pod));
            podInfoList.add(podInfo);
        }
        return podInfoList;
    }


}
