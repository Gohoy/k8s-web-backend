package com.example.home.gohoy.k8s_backend.service.user.impl;

import com.example.home.gohoy.k8s_backend.dto.PodResourceDTO;
import com.example.home.gohoy.k8s_backend.entities.PodInfo;
import com.example.home.gohoy.k8s_backend.service.admin.impl.AdminPodServiceImpl;
import com.example.home.gohoy.k8s_backend.service.user.PodService;
import com.example.home.gohoy.k8s_backend.utils.PodCURD;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.example.home.gohoy.k8s_backend.utils.PodCURD.*;
@org.springframework.stereotype.Service
public class PodServiceImpl implements PodService {

    private final KubernetesClient kubernetesClient;

    public  PodServiceImpl(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }
    @Resource
    AdminPodServiceImpl adminPodService;

    @Override
    public List<PodInfo> getPodsByUserName(String userName) {
        List<PodInfo> filteredPods = new ArrayList<>();
        PodList podList = kubernetesClient.pods().inNamespace("default").list();

        for (Pod pod : podList.getItems()) {
            String podName = getPodName(pod);

            // 判断是否是系统Pod（这里简单假设系统Pod的名称以"kube-"开头）
            if (podName.startsWith("kube-")) {
                continue; // 排除系统Pod
            }
            if (podName.startsWith(userName)) {
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

    @Override
    public int getConfigAndCreatePod(String podName, String configName, String imageName) {
        ConfigMap configMap = kubernetesClient.configMaps().inNamespace("default").withName(configName).get();
        String jobName = podName + "-job";
        String serviceName = podName + "-service";
        if (configMap == null) {
            String port ;
            if (configName.equals("vm-config")) {
                port = "50000";
            } else {
                port = "30000";
            }
            adminPodService.createOrUpdateConfigMap(configName, new PodResourceDTO("100m", "200Mi", "100000", port),kubernetesClient);
        }
        configMap = kubernetesClient.configMaps().inNamespace("default").withName(configName).get();
        if (configMap != null) {
            String cpu = configMap.getData().get("cpu");
            String memory = configMap.getData().get("memory");
            String port = configMap.getData().get("port");
            long timeOfLife = Long.parseLong(configMap.getData().get("timeOfLife"));
//创建pv
            PersistentVolume persistentVolume = new PersistentVolumeBuilder()
                    .withNewMetadata()
                    .withName(podName + "-pv")
                    .endMetadata()
                    .withNewSpec()
                    .withCapacity(Collections.singletonMap("storage", new Quantity("1Gi")))
                    .withAccessModes(Collections.singletonList("ReadWriteOnce"))
                    .withPersistentVolumeReclaimPolicy("Retain")
                    .withStorageClassName("local-storage") // Replace with your desired storage class name for local storage
                    .withHostPath(new HostPathVolumeSourceBuilder()
                            .withPath("/data/pv/") // Replace with the local directory path on each node
                            .build())
                    .withNodeAffinity(new VolumeNodeAffinityBuilder()
                            .withRequired(new NodeSelectorBuilder()
                                    .withNodeSelectorTerms(new NodeSelectorTermBuilder()
                                            .withMatchExpressions(new NodeSelectorRequirementBuilder()
                                                    .withKey("kubernetes.io/hostname") // Replace with your node label key
                                                    .withOperator("In")
                                                    .withValues("worker", "worker1", "master") // Replace with your node label value
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .endSpec()
                    .build();

            kubernetesClient.resource(persistentVolume).inNamespace("default").createOrReplace();

            // 检查是否已存在同名的 PVC
            PersistentVolumeClaim createdPVC = kubernetesClient.persistentVolumeClaims().inNamespace("default").withName(podName + "-pvc").get();

            if (createdPVC != null) {
                System.out.println("Existing PVC found. Binding to the existing PVC.");
            } else {
                // 创建新的 PVC
                PersistentVolumeClaim persistentVolumeClaim = new PersistentVolumeClaimBuilder()
                        .withNewMetadata()
                        .withName(podName + "-pvc")
                        .endMetadata()
                        .withNewSpec()
                        .withStorageClassName("local-storage")
                        .withAccessModes(Collections.singletonList("ReadWriteOnce"))
                        .withNewResources()
                        .withRequests(Collections.singletonMap("storage", new Quantity("1Gi")))
                        .endResources()
                        .endSpec()
                        .build();

                createdPVC = kubernetesClient.resource(persistentVolumeClaim).inNamespace("default").createOrReplace();
                System.out.println("New PVC created and bound.");
            }


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

            io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
                    .withNewMetadata()
                    .withName(serviceName) // 确保Service名称与Job名称相同
                    .endMetadata()
                    .withSpec(serviceSpec)
                    .build();


            io.fabric8.kubernetes.api.model.Service createdService = kubernetesClient.resource(service).inNamespace("default").createOrReplace();
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
                    .addNewVolumeMount() // 添加VolumeMount用于挂载PVC
                    .withName(podName + "-pvc") // 使用PVC的名称
                    .withMountPath("/data/pv/") // 设置挂载的路径，确保与容器应用程序需要访问的路径相匹配
                    .endVolumeMount()
                    .endContainer()
                    .addNewVolume() // 添加Volume用于绑定PVC
                    .withName(podName + "-pvc") // 使用PVC的名称
                    .withPersistentVolumeClaim(new PersistentVolumeClaimVolumeSourceBuilder()
                            .withClaimName(createdPVC.getMetadata().getName())
                            .build())
                    .endVolume()
                    .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();

            job.getSpec().getTemplate().getSpec().setActiveDeadlineSeconds(timeOfLife);
            // 将Job提交到Kubernetes集群
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
}
