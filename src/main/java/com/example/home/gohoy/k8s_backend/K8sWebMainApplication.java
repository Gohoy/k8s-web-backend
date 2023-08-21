package com.example.home.gohoy.k8s_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.home.gohoy.k8s_backend.dao")
public class K8sWebMainApplication {
	public static void main(String[] args) {
		SpringApplication.run(K8sWebMainApplication.class, args);
	}

}
