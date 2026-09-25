package ru.veduteam.vedu.auth.application;

import java.util.UUID;

public record RefreshTokenClaims(UUID userId, String tokenId) {
}
