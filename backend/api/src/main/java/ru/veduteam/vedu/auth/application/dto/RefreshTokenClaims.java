package ru.veduteam.vedu.auth.application.dto;

import java.util.UUID;

public record RefreshTokenClaims(UUID userId, String tokenId) {
}
