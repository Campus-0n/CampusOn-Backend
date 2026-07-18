package com.campuson.backend.global.jwt;

import com.campuson.backend.user.domain.User;
import com.campuson.backend.user.domain.UserRole;

public record  JwtUserClaim(
        Long userId,
        UserRole role
) {
    public static JwtUserClaim create(User user) {
        return new JwtUserClaim(user.getId(), user.getRole());
    }
    public static JwtUserClaim create(Long userId, UserRole role) {
        return new JwtUserClaim(userId, role);
    }
}
