package com.example.home.gohoy.k8s_backend.entities.kubevirt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class VirtualMachineInstanceSpec {
    private DomainSpec domain;
    private ArrayList<Network> networks;
    private ArrayList<Volume> volumes;
}
