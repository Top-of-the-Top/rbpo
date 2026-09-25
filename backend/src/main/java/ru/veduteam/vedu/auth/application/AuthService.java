package ru.veduteam.vedu.auth.application;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  public final OneTimeCodeRepository repository;

  public AuthService(@Lazy OneTimeCodeRepository repository) {
    this.repository = repository;
  }
}
