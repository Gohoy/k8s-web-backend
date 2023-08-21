package com.example.home.gohoy.k8s_backend.entities.kubevirt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Network {
    private String name;
    private PodNetwork pod;
}
