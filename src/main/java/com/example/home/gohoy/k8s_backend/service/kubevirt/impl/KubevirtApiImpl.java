package com.example.home.gohoy.k8s_backend.service.kubevirt.impl;

import com.example.home.gohoy.k8s_backend.entities.kubevirt.VirtualMachine;
import com.example.home.gohoy.k8s_backend.service.kubevirt.KubevirtApi;
import com.example.home.gohoy.k8s_backend.utils.KubevirtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

@Service
public class KubevirtApiImpl implements KubevirtApi {
    @Resource
    private KubevirtUtil kubevirtUtil;
    @Override
    public String get(String path) throws Exception {
        return kubevirtUtil.sendHttpRequest("GET", path, null, null);
    }

    @Override
    public String post(String path, RequestBody requestBody) throws Exception {
        return kubevirtUtil.sendHttpRequest("POST",path,null,requestBody);
    }

    @Override
    public String delete(String path) throws Exception {
        return kubevirtUtil.sendHttpRequest("DELETE",path,null,null);
    }


    @Override
    public String getVM(String username, String VMOccupied) throws JsonProcessingException {

        String VMName = username + "-vm-" + VMOccupied;
        VirtualMachine vm = new VirtualMachine();
        String requestBody = kubevirtUtil.getRequestBody();
        System.out.println(requestBody);

        return requestBody;
    }
}
