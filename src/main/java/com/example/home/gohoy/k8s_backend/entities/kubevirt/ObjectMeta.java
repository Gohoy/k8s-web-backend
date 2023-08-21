package com.example.home.gohoy.k8s_backend.entities.kubevirt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ObjectMeta {
    private String namespace;
    private String name;
    private Map<String,String> labels;
}
