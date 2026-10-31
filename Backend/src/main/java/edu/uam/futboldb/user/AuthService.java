package edu.uam.futboldb.user;

import edu.uam.futboldb.exception.ApiException;
import edu.uam.futboldb.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(String email, String password) {
        // Mismo mensaje para email desconocido y contraseña errónea: no revela qué emails existen.
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> ApiException.noAutorizado("Credenciales inválidas"));
        return jwtService.generarToken(user.getEmail());
    }
}
