package com.example.home.gohoy.k8s_backend.controller.admin;

import com.example.home.gohoy.k8s_backend.POJO.PodInfo;
import com.example.home.gohoy.k8s_backend.dto.PodResourceDTO;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import com.example.home.gohoy.k8s_backend.utils.PodCURD;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.home.gohoy.k8s_backend.utils.PodCURD.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/admin/pod/")
public class AdminPodController {
    private final KubernetesClient kubernetesClient;

    public AdminPodController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @GetMapping("/getAllPods")
    public List<PodInfo> getPodByPage() {
        List<PodInfo> filteredPods = new ArrayList<>();
        PodList podList = kubernetesClient.pods().inAnyNamespace().list();

        for (Pod pod : podList.getItems()) {
            String podName = getPodName(pod);

            // 判断是否是系统Pod（这里简单假设系统Pod的名称以"kube-"开头）
            if (podName.startsWith("kube-") || podName.startsWith("calico-") || podName.startsWith("coredns-")||podName.startsWith("etcd-")||podName.startsWith("tigera-")) {
                continue; // 排除系统Pod
            }
            PodInfo podInfo = new PodInfo();
            podInfo.setName(podName);
            podInfo.setStatus(getStatus(pod));
            podInfo.setNamespace(getNameSpace(pod));
            podInfo.setCpu(getPodCpu(pod));
            podInfo.setMemory(getPodMemory(pod));
            podInfo.setStorage(new PodCURD(kubernetesClient).getPodStorage(pod));
            podInfo.setIp(getIp(pod));
            podInfo.setSshPort(new PodCURD(kubernetesClient).getSshPort(pod));
            podInfo.setTtl(getTTL(pod));
            filteredPods.add(podInfo);
        }
        return filteredPods;
    }

    @PostMapping("/setCtrDefaultResource/")
    @ApiResponse(description = "修改默认的contianer配置，将配置存储在configMap中")
    public CommonResult setCtrDefaultResource(@RequestBody PodResourceDTO podResourceDTO) {
        String configMapName = "ctr-config"; // 替换为您的ConfigMap名称
        if (createOrUpdateConfigMap(configMapName, podResourceDTO,"30000")) {
            return new CommonResult<>().message("创建成功").code(200);
        }
        return new CommonResult<>().message("创建失败，请联系管理员").code(500);
    }

    @PostMapping("/setVMDefaultResource/")
    @ApiResponse(description = "修改默认的contianer配置，将配置存储在configMap中")
    public CommonResult setVMDefaultResource(@RequestBody PodResourceDTO podResourceDTO) {
        String configMapName = "vm-config"; // 替换为您的ConfigMap名称
        if (createOrUpdateConfigMap(configMapName, podResourceDTO,"50000")) {
            return new CommonResult<>().message("创建成功").code(200);
        }
        return new CommonResult<>().message("创建失败，请联系管理员").code(500);
    }
    public boolean createOrUpdateConfigMap(String configMapName, PodResourceDTO podResourceDTO,String startPort) {
        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new ObjectMeta());
        configMap.getMetadata().setName(configMapName);
        ConfigMap existMap = kubernetesClient.configMaps().inNamespace("config").withName(configMapName).get();
        String portValue;
        if(existMap != null){
             portValue = existMap.getData().get("port");
        }else{
            portValue = startPort;
        }


        configMap.setData(Map.of("cpu", podResourceDTO.getCpuValue(), "memory", podResourceDTO.getMemoryValue(), "storage", podResourceDTO.getStorageValue() , "port", portValue));
        ConfigMap map = kubernetesClient.resource(configMap).inNamespace("config").createOrReplace();
        return map != null;
    }


}
