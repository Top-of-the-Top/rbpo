package ru.veduteam.vedu.auth.infrastructure.errors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ru.veduteam.vedu.auth.domain.errors.EmailTakenException;
import ru.veduteam.vedu.auth.domain.errors.InvalidCredentialsException;
import ru.veduteam.vedu.auth.domain.errors.InvalidRefreshTokenException;
import ru.veduteam.vedu.auth.domain.errors.UsernameTakenException;
import ru.veduteam.vedu.auth.infrastructure.http.AuthController;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {
  @ExceptionHandler({ UsernameTakenException.class, EmailTakenException.class })
  public ProblemDetail handleTaken(RuntimeException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleConcurrentRegistration() {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Username or email is already taken");
  }

  @ExceptionHandler({ InvalidCredentialsException.class, InvalidRefreshTokenException.class })
  public ProblemDetail handleUnauthorized(RuntimeException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
  }
}
