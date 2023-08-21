package com.example.home.gohoy.k8s_backend.config;

import io.fabric8.kubernetes.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {

    @Bean
    public KubernetesClient kubernetesClient() {
        Config config = new ConfigBuilder().withMasterUrl("https://10.168.59.90:6443").build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
