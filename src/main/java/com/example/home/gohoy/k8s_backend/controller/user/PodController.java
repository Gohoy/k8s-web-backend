package com.example.home.gohoy.k8s_backend.controller.user;

import com.example.home.gohoy.k8s_backend.dto.PodDTO;
import com.example.home.gohoy.k8s_backend.entities.PodInfo;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.user.PodService;
import com.example.home.gohoy.k8s_backend.service.user.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/pod/")
public class PodController {
    private final KubernetesClient kubernetesClient;
    @Resource
    UserService userService;
    @Resource
    PodService podService;

    public PodController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @GetMapping("/selectPodByUserName/{username}")
    public CommonResult getPodsByUserName(@PathVariable("username") String username) {
       return new CommonResult<List<PodInfo>>().data( podService.getPodsByUserName(username)).code(200).message("获取"+username+"的pod成功");

    }

    @PostMapping("createCtr/{username}")
    @ApiResponse(description = "用户通过此接口来创建一个container，并且返回该pod 的ip和默认root的密码")
    public CommonResult createCtr(@PathVariable("username") String username) {
        User user = userService.getUserByName(username);
        if (user.getCtrMax() - user.getCtrOccupied() <= 0) {
            return new CommonResult<>().message("当前用户可用容器已达上限").code(500);
        }
        String ctrName = username + "-ctr-" + userService.getUserByName(username).getCtrOccupied().toString();

        int port = podService.getConfigAndCreatePod(ctrName, "ctr-config", "nginx");
        if (port == -1) {
            return new CommonResult<PodDTO>().message("申请失败，请联系管理员").code(500);
        }
        String clusterIP = kubernetesClient.services().inNamespace("default").withName(ctrName + "-service").get().getSpec().getClusterIP();
        PodDTO podDTO = new PodDTO();
        podDTO.setSshPort(port);
        podDTO.setSshIP(clusterIP);
        podDTO.setRootPassword("123456");
        user.setCtrOccupied(user.getCtrOccupied() + 1);
        userService.updateById(user);
        return new CommonResult<PodDTO>().data(podDTO).message("申请成功").code(200);
    }

    @PostMapping("createVM/{username}")
    @ApiResponse(description = "用户通过此接口来创建一个VM，并且返回该pod 的ip和默认root的密码")
    public CommonResult createVM(@PathVariable("username") String username) {
        User user = userService.getUserByName(username);
        if (user.getVmMax() - user.getVmOccupied() <= 0) {
            return new CommonResult<>().message("当前用户可用虚拟机已达上限").code(500);
        }
        String VMName = username + "-vm-" + userService.getUserByName(username).getVmOccupied().toString();

        int port = podService.getConfigAndCreatePod(VMName, "vm-config", "nginx");
        if (port == -1) {
            return new CommonResult<PodDTO>().message("申请失败，请联系管理员").code(500);
        }
        String clusterIP = kubernetesClient.services().inNamespace("default").withName(VMName + "-service").get().getSpec().getClusterIP();
        PodDTO podDTO = new PodDTO();
        podDTO.setSshPort(port);
        podDTO.setSshIP(clusterIP);
        podDTO.setRootPassword("123456");
        user.setVmOccupied(user.getVmOccupied() + 1);
        userService.updateById(user);
        return new CommonResult<PodDTO>().data(podDTO).message("申请成功").code(200);
    }


    @PostMapping("/deletePod/{podName}")
    @ApiResponse(description = "关闭指定名称的pod")
    public CommonResult deletePod(@PathVariable("podName") String podName) {
        int startIndex = podName.indexOf("-job-");
        kubernetesClient.pods().inNamespace("default").withName(podName).delete();
        podName = podName.substring(0, startIndex);
        String serviceName = podName+"-service";
        String jobName = podName+ "-job";
        kubernetesClient.services().inNamespace("default").withName(serviceName).delete();
        kubernetesClient.batch().v1().jobs().inNamespace("default").withName(jobName).delete();
        return new CommonResult<>().code(200).message("删除pod"+podName+"成功");
    }
}
