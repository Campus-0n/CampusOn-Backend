package com.campuson.backend.token.service;

import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.token.entity.ConfirmationToken;
import com.campuson.backend.token.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenService {

    private static final long CODE_EXPIRE_SECONDS = 300L; // 5분

    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Transactional
    public String issue(Long userId) {
        confirmationTokenRepository.deleteByUserId(userId);
        String code = createCode();
        confirmationTokenRepository.save(
                ConfirmationToken.builder()
                        .userId(userId)
                        .code(code)
                        .times(CODE_EXPIRE_SECONDS)
                        .build()
        );
        return code;
    }

    @Transactional
    public void confirm(Long userId, String code) {
        ConfirmationToken saved = confirmationTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.CONFIRMATION_TOKEN_NOT_EXIST));

        if (saved.isExpired()) {
            confirmationTokenRepository.deleteByUserId(userId);
            throw new BusinessException(ExceptionType.CONFIRMATION_TOKEN_EXPIRED);
        }
        if (!saved.getCode().equals(code)) {
            throw new BusinessException(ExceptionType.CONFIRMATION_TOKEN_NOT_MATCHED);
        }
        confirmationTokenRepository.deleteByUserId(userId);
    }

    private String createCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
