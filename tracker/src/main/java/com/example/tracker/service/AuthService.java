package com.example.tracker.service;

import com.example.tracker.dto.AuthResponse;
import com.example.tracker.dto.LoginRequest;
import com.example.tracker.dto.RegisterRequest;
import com.example.tracker.exception.EmailAlreadyUsedException;
import com.example.tracker.model.Role;
import com.example.tracker.model.User;
import com.example.tracker.repository.UserRepository;
import com.example.tracker.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException("Email is already registered");
        }

        User user = new User(email, passwordEncoder.encode(request.password()), Role.USER);
        userRepository.save(user);

        return new AuthResponse(
            jwtService.generateToken(email),
            email,
            jwtService.getExpirationSeconds()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        // Throws BadCredentialsException for an unknown email or a wrong
        // password; the handler maps both to the same 401 so the response
        // cannot be used to discover which emails are registered.
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.password()));

        return new AuthResponse(
            jwtService.generateToken(email),
            email,
            jwtService.getExpirationSeconds()
        );
    }
}
