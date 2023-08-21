package com.example.home.gohoy.k8s_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import okhttp3.Headers;
import okhttp3.RequestBody;

@Data
public class Request {
    private  String path;
    private String method;
    @JsonIgnore
    private Headers header;
    @JsonIgnore
    private RequestBody requestBody;
}
