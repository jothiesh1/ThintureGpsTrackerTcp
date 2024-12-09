package com.ThintureGpsTrackerTcp.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Disable CSRF protection
            .authorizeHttpRequests()
                .anyRequest().permitAll() // Allow all requests without authentication
            .and()
            .headers().frameOptions().disable(); // Allow access to H2 Console if needed
        return http.build();
    }
}
