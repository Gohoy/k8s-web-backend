package com.example.home.gohoy.k8s_backend.entities.kubevirt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Devices {
    private boolean autoattachGraphicsDevice;
    private ArrayList<Disk> disks;
    private ArrayList<Interface> interfaces;
 }
