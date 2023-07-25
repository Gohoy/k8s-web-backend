package com.example.home.gohoy.k8s_backend.controller.user;

import com.example.home.gohoy.k8s_backend.POJO.PodInfo;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.example.home.gohoy.k8s_backend.utils.PodCURD.*;

@RestController
@CrossOrigin
@RequestMapping("/pod/")
public class PodController {
    private final KubernetesClient kubernetesClient;

    public PodController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @GetMapping("/selectPodByPrefix")
    public List<PodInfo> getFilteredPodsByPrefix(@RequestParam(name = "namespace") String namespace,
                                                 @RequestParam(name = "podPrefix") String podPrefix) {
        List<PodInfo> filteredPods = new ArrayList<>();
        PodList podList = kubernetesClient.pods().inNamespace(namespace).list();

        for (Pod pod : podList.getItems()) {
            String podName = getPodName(pod);

            // 判断是否是系统Pod（这里简单假设系统Pod的名称以"kube-"开头）
            if (podName.startsWith("kube-")) {
                continue; // 排除系统Pod
            }
            if (podName.startsWith(podPrefix)) {
                PodInfo podInfo = new PodInfo();
                podInfo.setName(podName);
                podInfo.setStatus(getStatus(pod));
                podInfo.setNamespace(getNameSpace(pod));
                podInfo.setCpu(getPodCpu(pod));
                podInfo.setMemory(getPodMemory(pod));
                podInfo.setStorage(getPodStorage(pod));

                filteredPods.add(podInfo);
            }
        }

        return filteredPods;
    }

}
