package com.zvonok.service;

import com.zvonok.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Service for generating unique invite codes for servers.
 * Сервис для генерации уникальных кодов приглашения для серверов.
 * 
 * Использует репозиторий напрямую для проверки уникальности кода,
 * чтобы избежать циклической зависимости с ServerService.
 */
@Service
@RequiredArgsConstructor
public class InviteCodeService {

    private final ServerRepository serverRepository;
    private static final String CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int CODE_LENGTH = 15;
    private final SecureRandom random = new SecureRandom();

    public String generateUniqueInviteCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (serverRepository.existsByInvitedCode(code));

        return code;
    }

    public String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}
