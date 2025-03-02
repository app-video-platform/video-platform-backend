package com.myproject.video.video_platform.configs;

import com.myproject.video.video_platform.service.security.CsrfFilter;
import com.myproject.video.video_platform.service.security.JwtAuthenticationFilter;
import com.myproject.video.video_platform.service.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
/**
 * Main Security Configuration using SecurityFilterChain (no more WebSecurityConfigurerAdapter).
 */
@Configuration
@EnableMethodSecurity // Enables @PreAuthorize etc.
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CsrfFilter csrfFilter;

    /**
     * Primary Security Filter Chain definition.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // We'll handle CSRF manually via CsrfFilter
                .cors(Customizer.withDefaults())
                // Handle auth errors
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // We don't need sessions since we're using JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Define URL authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Registration, login, verify, etc.
                        .anyRequest().authenticated()            // Everything else requires login
                )
                .addFilterBefore(jwtProvider.jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(csrfFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provide a PasswordEncoder bean (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure CORS globally via a CorsConfigurationSource bean.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Replace with the actual domain of your frontend
        configuration.setAllowedOrigins(List.of("https://serious-debauchery.netlify.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS config to all endpoints
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Expose AuthenticationManager as a bean if you need it elsewhere
     * (e.g., for manual authentication or custom login endpoints).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
