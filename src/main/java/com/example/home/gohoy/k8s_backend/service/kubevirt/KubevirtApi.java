package com.example.home.gohoy.k8s_backend.service.kubevirt;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.RequestBody;

public interface KubevirtApi {
    String get(String path) throws Exception;
    String post(String path, RequestBody requestBody) throws Exception;
    String delete(String path) throws Exception;
    String getVM(String username, String VMOccupied) throws JsonProcessingException;
}

