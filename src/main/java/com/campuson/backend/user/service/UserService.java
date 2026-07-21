package com.campuson.backend.user.service;

import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.global.jwt.JwtHandler;
import com.campuson.backend.global.jwt.JwtUserClaim;
import com.campuson.backend.token.entity.Token;
import com.campuson.backend.token.service.ConfirmationTokenService;
import com.campuson.backend.user.domain.User;
import com.campuson.backend.user.domain.UserRole;
import com.campuson.backend.user.dto.request.LoginRequest;
import com.campuson.backend.user.dto.request.SignupRequest;
import com.campuson.backend.user.dto.request.VerifyEmailRequest;
import com.campuson.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ALLOWED_EMAIL_DOMAIN = "@kumoh.ac.kr";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    //private final ConfirmationTokenService confirmationTokenService;
    //private final JavaMailSender mailSender;
    private final JwtHandler jwtHandler;

    @Transactional(rollbackFor = Exception.class)
    public void signup(SignupRequest request) {
        validateEmailDomain(request.email());

        /**
        Optional<User> existing = userRepository.findByEmail(request.email());
        if (existing.isPresent()) {
            if (existing.get().isEmailVerified()) {
                throw new BusinessException(ExceptionType.DUPLICATE_EMAIL); // 이미 인증까지 끝난 진짜 중복
            }
            resendVerification(existing.get()); // 인증 전 상태면 코드만 재발급
            return;
        }
        */

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ExceptionType.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ExceptionType.DUPLICATE_LOGIN_ID);
        }

        User user = User.builder()
                .email(request.email())
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(UserRole.USER)
                .build();
        user.markEmailVerified();
        userRepository.save(user);

        //resendVerification(user);
    }

    /**
    private void resendVerification(User user) {
        String code = confirmationTokenService.issue(user.getId());
        sendVerificationEmail(user.getEmail(), code);
    }
     */

    private void validateEmailDomain(String email) {
        if (!email.toLowerCase().endsWith(ALLOWED_EMAIL_DOMAIN)) {
            throw new BusinessException(ExceptionType.INVALID_EMAIL_DOMAIN);
        }
    }

    /**
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        confirmationTokenService.confirm(user.getId(), request.code());
        user.markEmailVerified();
    }
    */

    @Transactional
    public User login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ExceptionType.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ExceptionType.INVALID_LOGIN);
        }
        /**
        if (!user.isEmailVerified()) {
            throw new BusinessException(ExceptionType.EMAIL_NOT_VERIFIED);
        }
        */
        return user;
    }

    /**
    private void sendVerificationEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Campus On]이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n5분 이내에 입력해주세요.");
        mailSender.send(message);
    }
    */

    @Transactional(readOnly = true)
    public boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }
}
