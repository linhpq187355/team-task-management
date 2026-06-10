package com.g5.teamtaskmanagement.service.impl;

import com.g5.teamtaskmanagement.entity.RefreshToken;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.repository.RefreshTokenRepository;
import com.g5.teamtaskmanagement.service.RefreshTokenService;
import com.g5.teamtaskmanagement.service.RefreshTokenIssueResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenMs;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-token-ms:2592000000}") long refreshTokenMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenMs = refreshTokenMs;
    }

    @Override
    public RefreshTokenIssueResult createRefreshToken(User user) {
        String raw = UUID.randomUUID().toString() + "." + UUID.randomUUID().toString();
        String hash = sha256(raw);
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hash);
        rt.setExpiresAt(LocalDateTime.now().plusNanos(refreshTokenMs * 1000000L));
        RefreshToken saved = refreshTokenRepository.save(rt);
        return new RefreshTokenIssueResult(raw, saved.getId());
    }

    @Override
    public Optional<RefreshToken> findByRawToken(String raw) {
        String hash = sha256(raw);
        return refreshTokenRepository.findByTokenHash(hash);
    }

    @Override
    public void revoke(String raw) {
        String hash = sha256(raw);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
            rt.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(rt);
        });
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
