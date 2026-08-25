package co.sena.iam.application.usecase;

import co.sena.iam.application.port.in.GetCurrentUserUseCase;
import co.sena.iam.application.port.out.UserRepository;
import co.sena.iam.domain.exception.InvalidCredentialsException;
import co.sena.iam.domain.model.User;

import java.util.UUID;

public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User me(UUID userId) {
        return userRepository.findById(userId).orElseThrow(InvalidCredentialsException::new);
    }
}
