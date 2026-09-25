package ru.veduteam.vedu.user.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.veduteam.vedu.user.domain.User;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByUsername(String username);

  boolean existsByEncryptedEmail(String encryptedEmail);

  Optional<User> findByUsername(String username);
}
