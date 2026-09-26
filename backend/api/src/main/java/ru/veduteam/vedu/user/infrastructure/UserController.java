package ru.veduteam.vedu.user.infrastructure;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.veduteam.vedu.user.application.UserService;

@RestController
@RequestMapping("/api/user")
public final class UserController {
  public final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }
}