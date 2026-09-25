package ru.veduteam.vedu.auth.application;

import java.util.UUID;

public record UserDetails(UUID id, String username) {
}
