package com.aquasolution.config;

import com.aquasolution.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioService usuarioService;

    public SecurityConfig(@Lazy UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/registro", "/recuperar/**").permitAll()                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/tecnico/**").hasAnyAuthority("ADMIN", "TECNICO")
                        .requestMatchers("/tickets/**").hasAnyAuthority("ADMIN", "TECNICO", "CLIENTE")
                        .requestMatchers("/cotizaciones/**").hasAuthority("ADMIN")
                        .requestMatchers("/piscinas/**").hasAnyAuthority("ADMIN", "TECNICO")
                        .requestMatchers("/reportes/**").hasAnyAuthority("ADMIN", "TECNICO")
                        .requestMatchers("/historial/**").hasAnyAuthority("ADMIN", "TECNICO")
                        .requestMatchers("/productos/**").hasAuthority("ADMIN")
                        .requestMatchers("/dashboard").hasAnyAuthority("ADMIN", "TECNICO")
                        .requestMatchers("/cliente/**").hasAnyAuthority("ADMIN", "TECNICO", "CLIENTE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            String rol = authentication.getAuthorities()
                                    .iterator().next().getAuthority();
                            if (rol.equals("CLIENTE")) {
                                response.sendRedirect("/cliente/inicio");
                            } else {
                                response.sendRedirect("/dashboard");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}