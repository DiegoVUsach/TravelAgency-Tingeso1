package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String keycloakId;

    @Column(unique = true, nullable = false)
    private String email;


    private String fullName;
    private String phone;
    private String identityDocument; //maybe add unique later
    private String nationality;

    @Column(nullable = false)
    private boolean active = true;

}
