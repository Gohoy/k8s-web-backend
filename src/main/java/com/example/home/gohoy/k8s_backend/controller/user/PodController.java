package com.example.home.gohoy.k8s_backend.controller.user;

import com.example.home.gohoy.k8s_backend.POJO.PodInfo;
import com.example.home.gohoy.k8s_backend.controller.admin.AdminPodController;
import com.example.home.gohoy.k8s_backend.dto.PodDTO;
import com.example.home.gohoy.k8s_backend.dto.PodResourceDTO;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import com.example.home.gohoy.k8s_backend.utils.PodCURD;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.example.home.gohoy.k8s_backend.utils.PodCURD.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/pod/")
public class PodController {
    private final KubernetesClient kubernetesClient;
    @Autowired
    UserService userService;

    @Autowired
    AdminPodController adminPodController;

    public PodController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @GetMapping("/selectPodByUserName/{username}")
    public List<PodInfo> getPodsByUserName(@PathVariable("username") String username) {
        List<PodInfo> filteredPods = new ArrayList<>();
        PodList podList = kubernetesClient.pods().inNamespace("default").list();

        for (Pod pod : podList.getItems()) {
            String podName = getPodName(pod);

            // 判断是否是系统Pod（这里简单假设系统Pod的名称以"kube-"开头）
            if (podName.startsWith("kube-")) {
                continue; // 排除系统Pod
            }
            if (podName.startsWith(username)) {
                PodInfo podInfo = new PodInfo();
                podInfo.setName(podName);
                podInfo.setStatus(getStatus(pod));
                podInfo.setNamespace(getNameSpace(pod));
                podInfo.setCpu(getPodCpu(pod));
                podInfo.setMemory(getPodMemory(pod));
//                podInfo.setStorage(new PodCURD(kubernetesClient).getPodStorage(pod));
                podInfo.setIp(getIp(pod));
                podInfo.setSshPort(new PodCURD(kubernetesClient).getSshPort(pod));
                podInfo.setTtl(getTTL(pod));
                filteredPods.add(podInfo);
            }
        }

        return filteredPods;
    }

    @PostMapping("createCtr/{username}")
    @ApiResponse(description = "用户通过此接口来创建一个container，并且返回该pod 的ip和默认root的密码")
    public CommonResult createCtr(@PathVariable("username") String username) {
        User user = userService.getUserByName(username);
        if (user.getCtrMax() - user.getCtrOccupied() <= 0) {
            return new CommonResult<>().message("当前用户可用容器已达上限").code(500);
        }
        String ctrName = username + "-ctr-" + userService.getUserByName(username).getCtrOccupied().toString();

        //TODO 创建一个container
        int port = getConfigAndCreatePod(ctrName, "ctr-config", "nginx");
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
//        TODO 创建一台虚拟机
        int port = getConfigAndCreatePod(VMName, "vm-config", "nginx");
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

    public int getConfigAndCreatePod(String podName, String configName, String imageName) {
        ConfigMap configMap = kubernetesClient.configMaps().inNamespace("default").withName(configName).get();
        String jobName = podName + "-job";
        String serviceName = podName + "-service";
        if (configMap == null) {
            String port = "0";
            if (configName.equals("vm-config")) {
                port = "50000";
            } else {
                port = "30000";
            }
            adminPodController.createOrUpdateConfigMap(configName, new PodResourceDTO("100m", "200Mi", "100000", port));
        }
        configMap = kubernetesClient.configMaps().inNamespace("default").withName(configName).get();
        if (configMap != null) {
            String cpu = configMap.getData().get("cpu");
            String memory = configMap.getData().get("memory");
            String port = configMap.getData().get("port");
            long timeOfLife = Long.parseLong(configMap.getData().get("timeOfLife"));
////创建pv
//            PersistentVolume persistentVolume = new PersistentVolumeBuilder()
//                    .withNewMetadata()
//                    .withName(podName + "-pv")
//                    .endMetadata()
//                    .withNewSpec()
//                    .withCapacity(Collections.singletonMap("storage", new Quantity(storage)))
//                    .withAccessModes(Collections.singletonList("ReadWriteOnce"))
//                    .withPersistentVolumeReclaimPolicy("Retain")
//                    .withStorageClassName("local-storage") // Replace with your desired storage class name for local storage
//                    .withHostPath(new HostPathVolumeSourceBuilder()
//                            .withPath("/data/pv/") // Replace with the local directory path on each node
//                            .build())
//                    .withNodeAffinity(new VolumeNodeAffinityBuilder()
//                            .withRequired(new NodeSelectorBuilder()
//                                    .withNodeSelectorTerms(new NodeSelectorTermBuilder()
//                                            .withMatchExpressions(new NodeSelectorRequirementBuilder()
//                                                    .withKey("kubernetes.io/hostname") // Replace with your node label key
//                                                    .withOperator("In")
//                                                    .withValues("worker", "worker1", "master") // Replace with your node label value
//                                                    .build())
//                                            .build())
//                                    .build())
//                            .build())
//                    .endSpec()
//                    .build();
//
//            PersistentVolume createdPV = kubernetesClient.resource(persistentVolume).inNamespace("default").createOrReplace();
//
//            // 检查是否已存在同名的 PVC
//            PersistentVolumeClaim createdPVC = kubernetesClient.persistentVolumeClaims().inNamespace("default").withName(podName + "-pvc").get();
//
//            if (createdPVC != null) {
//                System.out.println("Existing PVC found. Binding to the existing PVC.");
//            } else {
//                // 创建新的 PVC
//                PersistentVolumeClaim persistentVolumeClaim = new PersistentVolumeClaimBuilder()
//                        .withNewMetadata()
//                        .withName(podName + "-pvc")
//                        .endMetadata()
//                        .withNewSpec()
//                        .withStorageClassName("local-storage")
//                        .withAccessModes(Collections.singletonList("ReadWriteOnce"))
//                        .withNewResources()
//                        .withRequests(Collections.singletonMap("storage", new Quantity(storage)))
//                        .endResources()
//                        .endSpec()
//                        .build();
//
//                createdPVC = kubernetesClient.resource(persistentVolumeClaim).inNamespace("default").createOrReplace();
//                System.out.println("New PVC created and bound.");
//            }


            // 创建Service
            ServicePort servicePort = new ServicePort();
            servicePort.setName("ssh");
            servicePort.setProtocol("TCP");
            servicePort.setPort(Integer.valueOf(port)); // 设置为0以实现自动获取端口
            servicePort.setTargetPort(new IntOrString(22)); // 映射pod的22端口

            ServiceSpec serviceSpec = new ServiceSpec();
            serviceSpec.setSelector(Map.of("job-name", jobName)); // 与Job的标签匹配
            serviceSpec.setType("ClusterIP"); // 可根据需求选择Service类型
            serviceSpec.setPorts(Collections.singletonList(servicePort));

            Service service = new ServiceBuilder()
                    .withNewMetadata()
                    .withName(serviceName) // 确保Service名称与Job名称相同
                    .endMetadata()
                    .withSpec(serviceSpec)
                    .build();


            Service createdService = kubernetesClient.resource(service).inNamespace("default").createOrReplace();
            if(createdService == null){
                return -1;
            }
            System.out.println("Service created successfully. NodePort: " + port);
            port = String.valueOf(Integer.parseInt(port) + 1);
            configMap.getData().put("port", port);
            kubernetesClient.resource(configMap).inNamespace("default").createOrReplace();
            // 使用ConfigMap中的关键项来创建Job
            Job job = new JobBuilder()
                    .withNewMetadata()
                    .withName(jobName)
                    .endMetadata()
                    .withNewSpec()
                    .withCompletions(1)
                    .withParallelism(1)
                    .withBackoffLimit(0)
                    .withTtlSecondsAfterFinished(Math.toIntExact(60L)) //默认一个周
                    .withNewTemplate()
                    .withNewMetadata()
                    .addToLabels("job-name", jobName) // 添加标签用于Service的匹配
                    .endMetadata()
                    .withNewSpec()
                    .withRestartPolicy("Never")
                    .addNewContainer()
                    .withName(podName)
                    .withImage(imageName)
                    .withNewResources()
                    .addToLimits("cpu", Quantity.parse(cpu))
                    .addToLimits("memory", Quantity.parse(memory))
                    .addToRequests("cpu", Quantity.parse(cpu))
                    .addToRequests("memory", Quantity.parse(memory))
                    .endResources()
//                    .addNewVolumeMount() // 添加VolumeMount用于挂载PVC
//                    .withName(podName + "-pvc") // 使用PVC的名称
//                    .withMountPath("/data/pv/") // 设置挂载的路径，确保与容器应用程序需要访问的路径相匹配
//                    .endVolumeMount()
                    .endContainer()
//                    .addNewVolume() // 添加Volume用于绑定PVC
//                    .withName(podName + "-pvc") // 使用PVC的名称
//                    .withPersistentVolumeClaim(new PersistentVolumeClaimVolumeSourceBuilder()
//                            .withClaimName(createdPVC.getMetadata().getName())
//                            .build())
//                    .endVolume()
                    .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();

            job.getSpec().getTemplate().getSpec().setActiveDeadlineSeconds(timeOfLife);
            // 将Job提交到Kubernetes集群
//                Job createdJob = kubernetesClient.batch().jobs().createOrReplace(job);
            Job createdJob = kubernetesClient.resource(job).inNamespace("default").createOrReplace();
            if (createdJob != null) {
                return Integer.parseInt(port);
            }
        } else {
            System.out.println("configMap is null");
            return -1;
        }

        return -1; // 返回-1表示创建失败
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
