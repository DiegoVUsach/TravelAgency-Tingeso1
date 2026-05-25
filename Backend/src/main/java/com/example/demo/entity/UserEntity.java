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

    @Column(unique = true)
    private String email;

    // phone-number tba

}
