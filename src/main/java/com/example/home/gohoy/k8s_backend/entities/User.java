package com.example.home.gohoy.k8s_backend.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@TableName("users")
@Table(name = "users", schema = "k8s")
public class User  extends UserDTO{
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "password")
    private String password;


}
