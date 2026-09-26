package ru.veduteam.vedu.auth.application.dto;

import java.util.UUID;

public record UserDetails(UUID id, String username) {
}
