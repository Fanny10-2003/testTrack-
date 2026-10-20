package com.TestTrackQA.testTrack.config;

import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Locale;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(IUsuarioRepository usuarioRepository) {
        return correo -> {
            Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                    .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

            String rol = usuario.getRol().trim().toUpperCase(Locale.ROOT);
            if (!rol.startsWith("ROLE_")) {
                rol = "ROLE_" + rol;
            }

            return User.withUsername(usuario.getCorreo())
                    .password(usuario.getContrasena())
                    .authorities(new SimpleGrantedAuthority(rol))
                    .build();
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider) {
        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/registro", "/error", "/auth/**",
                                "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("correo")
                        .passwordParameter("password")
                        .failureUrl("/login?error")
                        .defaultSuccessUrl("/proyectos", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}
