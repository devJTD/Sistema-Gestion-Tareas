package com.freedom.tareas.Config;

import com.freedom.tareas.Service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // DEPENDENCIAS INYECTADAS - Servicio de usuario y codificador de contraseñas
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // MANEJADOR DE ÉXITO DE AUTENTICACIÓN PERSONALIZADO - Redirige al rol adecuado
    // tras login
    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // FILTRO DE SEGURIDAD - Define reglas de acceso, login, logout y manejo de
    // errores
    @Bean
    @SuppressWarnings({ "deprecation", "removal" })
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // AUTORIZACIÓN DE RUTAS - Define qué roles acceden a qué rutas
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login/**", "/register/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/", "/profile", "/tasks/**", "/calendar/**", "/api/**", "/profile/**")
                        .hasAnyRole("USER", "ADMIN")
                        .anyRequest().denyAll())
                // CONFIGURACIÓN DE LOGIN - Página de login y redirección personalizada
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll())
                // RECORDAR AL USUARIO - Mantiene la sesión activa usando una cookie remember-me
                .rememberMe(remember -> remember
                        .key("claveSeguraRecordarSesion123")
                        .tokenValiditySeconds(86400 * 1)
                        .userDetailsService(userService))
                // CONFIGURACIÓN DE LOGOUT - Limpieza de sesión y redirección tras logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                // MANEJO DE ACCESO DENEGADO - Redirección cuando no se tiene permiso
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    // AUTENTICADOR PERSONALIZADO - Configura proveedor con servicio de usuario y
    // codificador
    @Bean
    @SuppressWarnings("deprecation")
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(this.passwordEncoder);
        return provider;
    }

    // HANDLER DE ACCESO DENEGADO - Redirige a la raíz si no se tiene autorización
    @Bean
    @SuppressWarnings("Convert2Lambda")
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                    AccessDeniedException accessDeniedException) throws IOException, ServletException {
                response.sendRedirect("/");
            }
        };
    }
}
