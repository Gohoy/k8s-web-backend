package com.example.home.gohoy.k8s_backend.dto;

import lombok.Data;

@Data
public class PodDTO {
    private String sshIP;
    private int sshPort;
    private String rootPassword;
}
