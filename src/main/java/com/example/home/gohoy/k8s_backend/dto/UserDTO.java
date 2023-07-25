package com.example.home.gohoy.k8s_backend.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;


@Data
@Table(name = "users", schema = "k8s")
@TableName("users")
public class UserDTO {

    @Basic
    @Column(name = "username")
    private String username;

    @Basic
    @Column(name = "ctr_occupied")
    private Integer ctrOccupied;
    @Basic
    @Column(name = "ctr_name")
    private String ctrName;
    @Basic
    @Column(name = "ctr_max")
    private Integer ctrMax;
    @Basic
    @Column(name = "vm_occupied")
    private Integer vmOccupied;
    @Basic
    @Column(name = "vm_name")
    private String vmName;
    @Basic
    @Column(name = "vm_max")
    private Integer vmMax;
    @Basic
    @Column(name = "is_admin")
    private byte isAdmin;

    @Basic
    @Column(name = "token")
    private String token;
    @Basic
    @Column(name = "last_login")
    private Timestamp lastLogin;
}
