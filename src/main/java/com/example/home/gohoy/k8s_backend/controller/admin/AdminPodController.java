package com.example.home.gohoy.k8s_backend.controller.admin;

import com.example.home.gohoy.k8s_backend.POJO.PodInfo;
import com.example.home.gohoy.k8s_backend.dto.PodResourceDTO;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
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
