package com.example.home.gohoy.k8s_backend.controller.admin;

import com.example.home.gohoy.k8s_backend.POJO.PodInfo;
import com.example.home.gohoy.k8s_backend.dto.PodResourceDTO;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import com.example.home.gohoy.k8s_backend.utils.PodCURD;
import io.fabric8.kubernetes.api.model.*;
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

    @GetMapping("/getAllPods/{type}")
    public List<PodInfo> getPodByPage(@PathVariable("type") String type) {
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
//            podInfo.setStorage(new PodCURD(kubernetesClient).getPodStorage(pod));
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
        if (createOrUpdateConfigMap(configMapName, podResourceDTO)) {
            return new CommonResult<>().message("创建或修改容器默认资源成功").code(200);
        }
        return new CommonResult<>().message("创建失败，请联系管理员").code(500);
    }

    @PostMapping("/setVMDefaultResource/")
    @ApiResponse(description = "修改默认的contianer配置，将配置存储在configMap中")
    public CommonResult setVMDefaultResource(@RequestBody PodResourceDTO podResourceDTO) {
        String configMapName = "vm-config"; // 替换为您的ConfigMap名称
        if (createOrUpdateConfigMap(configMapName, podResourceDTO)) {
            return new CommonResult<>().message("创建或修改虚拟机默认资源成功").code(200);
        }
        return new CommonResult<>().message("创建失败，请联系管理员").code(500);
    }
    public boolean createOrUpdateConfigMap(String configMapName, PodResourceDTO podResourceDTO) {
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
    @GetMapping("/getDefaultConfig/{type}")
    public CommonResult getPodDefaultResource(@PathVariable("type") String type){
        if(type.equals("vm") || type.equals("ctr")){
            String configName = type + "-config";
            ConfigMap configMap = kubernetesClient.configMaps().inNamespace("default").withName(configName).get();
            if(configMap == null){
                return new CommonResult<String >().code(500).message(configName+"没有设置");
            }else {
                String cpu = configMap.getData().get("cpu");
                String memory = configMap.getData().get("memory");
                String port = configMap.getData().get("port");
                String timeOfLife = configMap.getData().get("timeOfLife");
                return new CommonResult<PodResourceDTO>().data(new PodResourceDTO(cpu,memory,timeOfLife,port)).message("获取"+configName+"默认配置成功").code(200);
            }
        }else {
            return new CommonResult<String >().code(500).message("没有"+type+"类型");
        }
    }


}
