package com.aquasolution.service;

import com.aquasolution.model.Usuario;
import com.aquasolution.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRol().name()))
        );
    }

    public Usuario guardar(Usuario usuario, String usuarioActual, String rolActual) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario guardado = usuarioRepository.save(usuario);
        auditoriaService.registrar(usuarioActual, rolActual, "CREATE", "Usuarios",
                "Se creó el usuario: " + guardado.getUsername()
                        + " - Rol: " + guardado.getRol().name());
        return guardado;
    }

    public Usuario guardar(Usuario usuario) {
        return guardar(usuario, "Sistema", "SISTEMA");
    }

    public Usuario actualizarSinPassword(Usuario usuario, String usuarioActual, String rolActual) {
        Usuario actualizado = usuarioRepository.save(usuario);
        auditoriaService.registrar(usuarioActual, rolActual, "UPDATE", "Usuarios",
                "Se actualizaron datos del usuario: " + actualizado.getUsername());
        return actualizado;
    }

    public Usuario actualizarSinPassword(Usuario usuario) {
        return actualizarSinPassword(usuario, "Sistema", "SISTEMA");
    }

    public void cambiarPassword(Long id, String nuevaPassword, String usuarioActual, String rolActual) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        auditoriaService.registrar(usuarioActual, rolActual, "UPDATE", "Usuarios",
                "Se cambió la contraseña del usuario: " + usuario.getUsername());
    }

    public void cambiarPassword(Long id, String nuevaPassword) {
        cambiarPassword(id, nuevaPassword, "Sistema", "SISTEMA");
    }

    public void eliminar(Long id, String usuarioActual, String rolActual) {
        usuarioRepository.findById(id).ifPresent(u ->
                auditoriaService.registrar(usuarioActual, rolActual, "DELETE", "Usuarios",
                        "Se eliminó el usuario: " + u.getUsername() + " - Rol: " + u.getRol().name())
        );
        usuarioRepository.deleteById(id);
    }

    public void eliminar(Long id) {
        eliminar(id, "Sistema", "SISTEMA");
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> obtenerPorRol(Usuario.Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Optional<Usuario> obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
}