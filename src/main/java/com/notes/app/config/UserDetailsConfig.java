package com.notes.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserDetailsConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Standalone bean — no dependency on SecurityConfig.
     * JwtAuthenticationFilter can now inject UserDetailsService directly
     * without touching SecurityConfig, which breaks the cycle.
     *
     * Replace InMemoryUserDetailsManager with a database-backed
     * implementation when you add a users table.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
            User.builder()
                .username("alice")
                .password(encoder.encode("password"))
                .roles("USER")
                .build(),
            User.builder()
                .username("admin")
                .password(encoder.encode("admin"))
                .roles("USER", "ADMIN")
                .build()
        );
    }
}
