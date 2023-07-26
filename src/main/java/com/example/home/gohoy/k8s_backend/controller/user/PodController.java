package com.example.home.gohoy.k8s_backend.controller.user;

import com.example.home.gohoy.k8s_backend.POJO.PodInfo;
import com.example.home.gohoy.k8s_backend.dto.PodDTO;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.example.home.gohoy.k8s_backend.utils.PodCURD.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/pod/")
public class PodController {
    @Resource
    UserService userService;
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
    @PostMapping("createCtr/{username}")
    @ApiResponse(description = "用户通过此接口来创建一个container，并且返回该pod 的ip和默认root的密码")
    public CommonResult createCtr(@PathVariable("username") String username){
        User user = userService.getUserByName(username);
        if(user.getCtrMax() - user.getCtrOccupied() <= 0){
            return new CommonResult<>().message("当前用户可用容器已达上限").code(200);
        }
        //TODO 创建一个container
        PodDTO podDTO = new PodDTO();
        return new CommonResult<PodDTO>().data(podDTO).message("申请成功").code(200);
    }

    @PostMapping("createVM/{username}")
    @ApiResponse(description = "用户通过此接口来创建一个VM，并且返回该pod 的ip和默认root的密码")
    public CommonResult createVM(@PathVariable("username") String username){
        User user = userService.getUserByName(username);
        if(user.getVmMax() - user.getVmOccupied() <=0){
            return new CommonResult<>().message("当前用户可用虚拟机已达上限").code(200);
        }
//        TODO 创建一台虚拟机
        PodDTO podDTO = new PodDTO();
        return new CommonResult<PodDTO>().data(podDTO).message("申请成功").code(200);
    }

}
