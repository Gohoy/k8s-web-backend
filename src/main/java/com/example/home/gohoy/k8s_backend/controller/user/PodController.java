package com.example.home.gohoy.k8s_backend.controller.user;

import com.example.home.gohoy.k8s_backend.dto.PodDTO;
import com.example.home.gohoy.k8s_backend.entities.PodInfo;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.user.PodService;
import com.example.home.gohoy.k8s_backend.service.user.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
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
        return new CommonResult<List<PodInfo>>().data(podService.getPodsByUserName(username)).code(200).message("获取" + username + "的pod成功");

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
        String folderPath = "/data/upload_pvc_commands";  // Linux中的文件夹路径
        String fileName = VMName + ".vm";
// 结合文件夹路径和文件名
        String filePath = folderPath + "/" + fileName;
        File file = new File(filePath);
        try {
            // 检查文件是否存在，如果存在则删除
            if (file.exists()) {
                file.delete();
            }

            // 创建新文件
            boolean fileCreated = file.createNewFile();
            if (fileCreated) {
                System.out.println("文件创建成功。");
            } else {
                System.out.println("文件创建失败。");
                return new CommonResult<PodDTO>().message("申请失败").code(500);
            }
        } catch (IOException e) {
            System.err.println("创建文件时出错：" + e.getMessage());
            return new CommonResult<PodDTO>().message("申请失败").code(500);
        }
//        String clusterIP = kubernetesClient.services().inNamespace("default").withName(VMName + "-service").get().getSpec().getClusterIP();
        PodDTO podDTO = new PodDTO();
//        podDTO.setSshPort(port);
//        podDTO.setSshIP(clusterIP);
        podDTO.setRootPassword("123456");
        user.setVmOccupied(user.getVmOccupied() + 1);
        userService.updateById(user);
        return new CommonResult<PodDTO>().data(podDTO).message("申请成功").code(200);
    }


    @PostMapping("/deletePod/{podName}")
    @ApiResponse(description = "删除指定名称的pod")
    public CommonResult deletePod(@PathVariable("podName") String podName) {

        kubernetesClient.pods().inNamespace("default").withName(podName).delete();

        int startIndex = podName.indexOf("-job-");
        if (startIndex == -1) {
            String[] parts = podName.split("-");
            podName = parts[2]+"-"+parts[3]+"-"+parts[4];
            String folderPath = "/data/upload_pvc_commands";  // Linux中的文件夹路径
            String fileName = podName + ".delete";
            String filePath = folderPath + "/" + fileName;
            File file = new File(filePath);
            try {
                // 检查文件是否存在，如果存在则删除
                if (file.exists()) {
                    file.delete();
                }
                // 创建新文件
                boolean fileCreated = file.createNewFile();
                if (fileCreated) {
                    System.out.println("文件创建成功。");
                } else {
                    System.out.println("文件创建失败。");
                    return new CommonResult<PodDTO>().message("删除失败").code(500);
                }
            } catch (IOException e) {
                System.err.println("创建文件时出错：" + e.getMessage());
                return new CommonResult<PodDTO>().message("删除失败").code(500);
            }


        } else {
            podName = podName.substring(0, startIndex);

            String pvcName = podName + "-pvc";
            String pvName = podName + "-pv";
            String serviceName = podName + "-service";
            String jobName = podName + "-job";
            kubernetesClient.services().inNamespace("default").withName(serviceName).delete();
            kubernetesClient.batch().v1().jobs().inNamespace("default").withName(jobName).delete();
            kubernetesClient.persistentVolumeClaims().inNamespace("default").withName(pvcName).delete();
            kubernetesClient.persistentVolumes().withName(pvName).delete();
        }
        return new CommonResult<>().code(200).message("删除pod" + podName + "成功");
    }

    @PostMapping("/deleteVM/{podName}")
    @ApiResponse(description = "删除指定名称的vm")
    public CommonResult deleteVM(@PathVariable("podName") String podName) {
        kubernetesClient.pods().inNamespace("default").withName(podName).delete();
        String pvcName = podName + "-pvc";
        String pvName = podName + "-pv";
        kubernetesClient.persistentVolumeClaims().inNamespace("default").withName(pvcName).delete();
        kubernetesClient.persistentVolumes().withName(pvName).delete();
        return new CommonResult<>().code(200).message("删除pod" + podName + "成功");
    }

    @PostMapping("/extendTime/{podName}")
    @ApiResponse(description = "延长指定名称的pod的使用时间")
    public CommonResult extendTime(@PathVariable("podName") String podName) throws ApiException {

        int startIndex = podName.indexOf("-job-");
        String pvcName = podName + "-pvc";
        podName = podName.substring(0, startIndex);
        String jobName = podName + "-job";
//        ConfigMap configMap = kubernetesClient.configMaps().inNamespace("default").withName("ctr-config").get();
//            String cpu = configMap.getData().get("cpu");
//            String memory = configMap.getData().get("memory");
//            String port = configMap.getData().get("port");

        JobList jobList = kubernetesClient.batch().v1().jobs().inNamespace("default").withLabelSelector("job-name=" + jobName).list();
        BatchV1Api batchV1Api = new BatchV1Api();
        String labelSelector = jobName;
        Job job = jobList.getItems().get(0);
        if (job == null) {
            return new CommonResult<>().code(200).message("延长pod使用时间" + podName + "成功");
        }
        Long timeOfLife = job.getSpec().getTemplate().getSpec().getActiveDeadlineSeconds();

        timeOfLife = timeOfLife + 7 * 24 * 60 * 60;
        job.getSpec().getTemplate().getSpec().setActiveDeadlineSeconds(timeOfLife);
        // 将Job提交到Kubernetes集群
        Job createdJob = kubernetesClient.resource(job).inNamespace("default").createOrReplace();
        return new CommonResult<>().code(200).message("延长pod使用时间" + podName + "成功");
    }
}
