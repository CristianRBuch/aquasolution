package com.aquasolution.service;

import com.aquasolution.model.TokenRecuperacion;
import com.aquasolution.model.Usuario;
import com.aquasolution.repository.TokenRecuperacionRepository;
import com.aquasolution.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecuperacionService {

    private final TokenRecuperacionRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public RecuperacionService(TokenRecuperacionRepository tokenRepository,
                               UsuarioRepository usuarioRepository,
                               EmailService emailService,
                               PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean solicitarRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) return false;

        Usuario usuario = usuarioOpt.get();

        // Eliminar tokens anteriores del usuario
        tokenRepository.deleteByUsuarioId(usuario.getId());

        // Generar nuevo token
        String token = UUID.randomUUID().toString();
        TokenRecuperacion tokenRecuperacion = new TokenRecuperacion();
        tokenRecuperacion.setToken(token);
        tokenRecuperacion.setUsuario(usuario);
        tokenRecuperacion.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(tokenRecuperacion);

        // Enviar correo
        emailService.enviarRecuperacionContrasena(usuario.getEmail(), token);
        return true;
    }

    public Optional<TokenRecuperacion> validarToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.getUsado() && !t.isExpirado());
    }

    @Transactional
    public boolean resetearContrasena(String token, String nuevaContrasena) {
        Optional<TokenRecuperacion> tokenOpt = validarToken(token);
        if (tokenOpt.isEmpty()) return false;

        TokenRecuperacion tokenRecuperacion = tokenOpt.get();
        Usuario usuario = tokenRecuperacion.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);

        tokenRecuperacion.setUsado(true);
        tokenRepository.save(tokenRecuperacion);
        return true;
    }
}