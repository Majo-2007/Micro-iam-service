package co.sena.iam.adapter.out.persistence;

import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void save(User user) {
        UserJpaEntity entity = jpaRepository.findById(user.id()).orElseThrow();
        entity.setFailedAttempts(user.failedAttempts());
        entity.setLockedUntil(user.lockedUntil());
        entity.setLastLoginAt(user.lastLoginAt());
        jpaRepository.save(entity);
    }

    private User toDomain(UserJpaEntity e) {
        return new User(e.getId(), e.getEmail(), e.getPasswordHash(), e.getFirstName(), e.getLastName(),
                e.getActorType(), e.getActorId(), e.isActive(), e.getLastLoginAt(),
                e.getFailedAttempts(), e.getLockedUntil());
    }
}
