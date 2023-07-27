package com.example.home.gohoy.k8s_backend.dto;

import lombok.Data;

@Data
public class PodResourceDTO {
    String cpuValue ;
    String memoryValue ;
    String storageValue ;
}
