package ru.veduteam.vedu.user.application;

import org.springframework.stereotype.Service;

@Service
public class UserService {
  public UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }
}
