package ru.veduteam.vedu.auth.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.veduteam.vedu.auth.application.dto.LoginRequest;
import ru.veduteam.vedu.auth.application.dto.RegisterRequest;
import ru.veduteam.vedu.auth.application.dto.TokenPair;
import ru.veduteam.vedu.auth.application.services.AuthService;
import ru.veduteam.vedu.auth.domain.errors.InvalidCredentialsException;
import ru.veduteam.vedu.auth.domain.errors.InvalidRefreshTokenException;
import ru.veduteam.vedu.auth.domain.errors.UsernameTakenException;
import ru.veduteam.vedu.auth.infrastructure.http.AuthController;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;

  private static final String VALID_REGISTRATION = """
      {"username": "bob", "email": "bob@example.com", "password": "password123"}
      """;

  @Test
  void registerReturnsTokensWith201() throws Exception {
    when(authService.register(any(RegisterRequest.class))).thenReturn(new TokenPair("access", "refresh"));

    mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTRATION))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken").value("access"))
        .andExpect(jsonPath("$.refreshToken").value("refresh"));
  }

  @Test
  void registerRejectsInvalidInputWithoutCallingService() throws Exception {
    String invalid = """
        {"username": "b", "email": "not-an-email", "password": "short"}
        """;

    mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(invalid))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(authService);
  }

  @Test
  void registerRejectsPasswordOver72Bytes() throws Exception {
    String longPassword = """
        {"username": "bob", "email": "bob@example.com", "password": "%s"}
        """.formatted("я".repeat(40));

    mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(longPassword))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(authService);
  }

  @Test
  void registerWithTakenUsernameReturns409() throws Exception {
    when(authService.register(any(RegisterRequest.class))).thenThrow(new UsernameTakenException());

    mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTRATION))
        .andExpect(status().isConflict());
  }

  @Test
  void loginWithWrongCredentialsReturns401() throws Exception {
    when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

    mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"username": "bob", "password": "wrong-password"}
            """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshWithInvalidTokenReturns401() throws Exception {
    when(authService.refresh(any())).thenThrow(new InvalidRefreshTokenException());

    mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"refreshToken": "stale"}
            """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshIsNotAvailableViaGet() throws Exception {
    mockMvc.perform(get("/api/auth/refresh")).andExpect(status().isMethodNotAllowed());
  }

  @Test
  void logoutReturns204() throws Exception {
    mockMvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"refreshToken": "any"}
            """))
        .andExpect(status().isNoContent());
  }
}
