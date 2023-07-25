package com.example.home.gohoy.k8s_backend.entities;

import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Data
@Entity
@Table(name = "users", schema = "k8s")
public class User extends UserDTO {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "username")
    private String username;
    @Basic
    @Column(name = "password")
    private String password;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return id == that.id && isAdmin == that.isAdmin && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(ctrOccupied, that.ctrOccupied) && Objects.equals(ctrName, that.ctrName) && Objects.equals(ctrMax, that.ctrMax) && Objects.equals(vmOccupied, that.vmOccupied) && Objects.equals(vmName, that.vmName) && Objects.equals(vmMax, that.vmMax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, ctrOccupied, ctrName, ctrMax, vmOccupied, vmName, vmMax, isAdmin);
    }
}
