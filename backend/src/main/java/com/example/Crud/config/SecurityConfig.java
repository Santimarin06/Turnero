package com.example.Crud.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public org.springframework.security.web.context.SecurityContextRepository securityContextRepository() {
        return new org.springframework.security.web.context.HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:4201"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("POST", "/api/auth/login").permitAll()
                        .requestMatchers("POST", "/api/cliente").permitAll()
                        .requestMatchers("GET", "/api/cliente/documento/**").permitAll()
                        .requestMatchers("POST", "/api/turno/crear/**").permitAll()
                        
                        .requestMatchers("GET", "/api/turno/caja/pendientes").hasAnyAuthority("ROLE_CAJERO", "ROLE_ADMIN")
                        .requestMatchers("GET", "/api/turno/caja/siguiente").hasAnyAuthority("ROLE_CAJERO", "ROLE_ADMIN")
                        .requestMatchers("GET", "/api/turno/caja/actual").hasAnyAuthority("ROLE_CAJERO", "ROLE_ADMIN")
                        
                        .requestMatchers("GET", "/api/turno/asesoria/pendientes").hasAnyAuthority("ROLE_ASESOR", "ROLE_ADMIN")
                        .requestMatchers("GET", "/api/turno/asesoria/siguiente").hasAnyAuthority("ROLE_ASESOR", "ROLE_ADMIN")
                        .requestMatchers("GET", "/api/turno/asesoria/actual").hasAnyAuthority("ROLE_ASESOR", "ROLE_ADMIN")
                        
                        .requestMatchers("PUT", "/api/turno/iniciar-atencion/**").hasAnyAuthority("ROLE_CAJERO", "ROLE_ASESOR", "ROLE_ADMIN")
                        .requestMatchers("PUT", "/api/turno/finalizar-atencion/**").hasAnyAuthority("ROLE_CAJERO", "ROLE_ASESOR", "ROLE_ADMIN")
                        .requestMatchers("PUT", "/api/turno/cancelar/**").hasAnyAuthority("ROLE_CAJERO", "ROLE_ASESOR", "ROLE_ADMIN")
                        .requestMatchers("GET", "/api/turno/{id}").hasAnyAuthority("ROLE_CAJERO", "ROLE_ASESOR", "ROLE_ADMIN")
                        .requestMatchers("GET", "/api/turno/estado/**").hasAnyAuthority("ROLE_CAJERO", "ROLE_ASESOR", "ROLE_ADMIN")
                        .requestMatchers("POST", "/api/turno/actualizar-aging").hasAnyAuthority("ROLE_CAJERO", "ROLE_ASESOR", "ROLE_ADMIN")
                        
                        .requestMatchers("/api/usuario/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("GET", "/api/cliente/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("PUT", "/api/cliente/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("DELETE", "/api/cliente/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("GET", "/api/turno/obtener-todos").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("PUT", "/api/turno/pasar-primero/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("DELETE", "/api/turno/**").hasAuthority("ROLE_ADMIN")
                        
                        .requestMatchers("POST", "/api/auth/logout").authenticated()
                        .requestMatchers("GET", "/api/auth/me").authenticated()
                        
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
