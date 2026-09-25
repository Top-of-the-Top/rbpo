package ru.veduteam.vedu.auth.application;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
  public final OneTimeCodeRepository repository;

  public AuthService(OneTimeCodeRepository repository) {
    this.repository = repository;
  }
}
