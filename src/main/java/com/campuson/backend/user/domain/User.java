package com.campuson.backend.user.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password; // 반드시 암호화된 값만 저장

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean emailVerified;


    @Builder
    public User(String email, String loginId, String password, String name, UserRole role) {
        this.email = email;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.role = role;
        this.emailVerified = false;
    }

    public void markEmailVerified() {
        this.emailVerified = true;
    }

}
