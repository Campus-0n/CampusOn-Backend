package com.campuson.backend.global.config.security;

import com.campuson.backend.global.jwt.JwtHandler;
import com.campuson.backend.global.jwt.JwtProperties;
import com.campuson.backend.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class JwtConfig {

    private final RefreshTokenRepository refreshTokenRepository;

    @Bean
    public JwtHandler jwtHandler(JwtProperties jwtProperties) {
        return new JwtHandler(jwtProperties, refreshTokenRepository);
    }
}
