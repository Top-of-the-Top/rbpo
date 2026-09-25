package ru.veduteam.vedu.auth.application.dto;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9_.-]+$") String username,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 8, max = 72) String password) {

  @JsonIgnore
  @AssertTrue(message = "password must be at most 72 bytes in UTF-8")
  public boolean isPasswordWithinByteLimit() {
    return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
  }
}
