package edu.uam.futboldb.user;

import edu.uam.futboldb.exception.ApiException;
import edu.uam.futboldb.user.UserDtos.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registrar(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflicto("Ya existe un usuario con ese email");
        }
        return userRepository.save(new User(request.name().trim(), email, passwordEncoder.encode(request.password())));
    }

    public User buscarPorEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
    }
}
