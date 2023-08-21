package com.example.home.gohoy.k8s_backend.controller.admin;

import com.example.home.gohoy.k8s_backend.dto.PodResourceDTO;
import com.example.home.gohoy.k8s_backend.entities.PodInfo;
import com.example.home.gohoy.k8s_backend.service.admin.AdminPodService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/admin/pod/")
public class AdminPodController {
    private final KubernetesClient kubernetesClient;
    @Resource
    private AdminPodService adminPodService ;

    public AdminPodController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @GetMapping("/getAllPods/{type}")
    public CommonResult  getPodByPage(@PathVariable("type") String type) {
        List<PodInfo> pods = adminPodService.getPodByPage(type, kubernetesClient);
        return new CommonResult<List<PodInfo>>().data(pods).message("获取"+type+"成功").code(200);
    }

    @PostMapping("/setCtrDefaultResource/")
    @ApiResponse(description = "修改默认的container配置，将配置存储在configMap中")
    public CommonResult setCtrDefaultResource(@RequestBody PodResourceDTO podResourceDTO) {
        String configMapName = "ctr-config"; // 替换为您的ConfigMap名称
        if (adminPodService.createOrUpdateConfigMap(configMapName, podResourceDTO,kubernetesClient)) {
            return new CommonResult<>().message("创建或修改容器默认资源成功").code(200);
        }
        return new CommonResult<>().message("创建失败，请联系管理员").code(500);
    }

    @PostMapping("/setVMDefaultResource/")
    @ApiResponse(description = "修改默认的container配置，将配置存储在configMap中")
    public CommonResult setVMDefaultResource(@RequestBody PodResourceDTO podResourceDTO) {
        String configMapName = "vm-config"; // 替换为您的ConfigMap名称
        if (adminPodService.createOrUpdateConfigMap(configMapName, podResourceDTO,kubernetesClient)) {
            return new CommonResult<>().message("创建或修改虚拟机默认资源成功").code(200);
        }
        return new CommonResult<>().message("创建失败，请联系管理员").code(500);
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
