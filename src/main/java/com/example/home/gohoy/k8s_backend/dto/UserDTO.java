package com.example.home.gohoy.k8s_backend.dto;

import lombok.Data;

@Data
public class UserDTO {
    private int id;
    private String userName;
    private int containerOccupied;
    private String containerName;
    private int containerMaxCount;
    private int VMOccupied;
    private String VMName;
    private int VMCount;
    private boolean isAdmin;
}
