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

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @SuppressWarnings({ "deprecation", "removal" })
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // 1. Rutas públicas (accesibles por cualquier persona)
                // Incluye /login, /register, /loginAdmin (aunque ya no uses /loginAdmin.html, la ruta sigue siendo permitida por si acaso), y recursos estáticos.
                .requestMatchers("/login/**", "/register/**", "/loginAdmin/**", "/css/**", "/js/**", "/images/**").permitAll()

                // 2. Rutas específicas para ADMIN
                // Solo usuarios con rol ADMIN pueden acceder a /admin y cualquier subruta.
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")

                // 3. Rutas para USUARIOS COMUNES (Exclusivo para USER)
                // Estas rutas requieren autenticación y que el usuario tenga EXCLUSIVAMENTE el rol USER.
                // Los ADMINS NO podrán acceder a estas rutas.
                .requestMatchers("/", "/tasks/**", "/calendar/**", "/api/**").hasRole("USER") // CAMBIO AQUÍ: de hasAnyRole("USER", "ADMIN") a hasRole("USER")

                // 4. Cualquier otra solicitud que no haya sido explícitamente permitida o
                // restringida por rol, será DENIEDA.
                .anyRequest().denyAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(this.passwordEncoder);
        return provider;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                               AccessDeniedException accessDeniedException) throws IOException, ServletException {
                // Redirige a la página principal si el acceso es denegado (para usuarios autenticados sin el rol adecuado).
                // Considera si quieres redirigir a un error 403 o una página de acceso denegado específica.
                response.sendRedirect("/"); 
            }
        };
    }
}