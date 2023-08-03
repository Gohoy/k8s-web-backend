package com.example.home.gohoy.k8s_backend.dto;

import lombok.Data;

@Data
public class PodResourceDTO {
    public PodResourceDTO(String cpuValue, String memoryValue, String timeOfLife , String  port){
        this.cpuValue = cpuValue;
        this.memoryValue = memoryValue;
        this.timeOfLife = timeOfLife;
        this.port = port;
    }
    String cpuValue ;
    String memoryValue ;
    String timeOfLife ;
    String  port ;
}
