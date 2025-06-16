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
import org.springframework.beans.factory.annotation.Autowired; // Necesario para inyectar CustomAuthenticationSuccessHandler

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired // Inyecta tu handler personalizado
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
                // 1. Rutas públicas (accesibles por cualquier persona, logueada o no)
                // Esto incluye el login principal, registro, y ahora también la página de login de admin.
                // Los **/** al final de la ruta permiten cualquier subruta, por ejemplo /login/cualquierecosa
                .requestMatchers("/login/**", "/register/**",  "/css/**", "/js/**", "/images/**").permitAll()

                // 2. Rutas específicas para ADMIN
                // Todas las rutas bajo /admin solo son accesibles por usuarios con rol ADMIN
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")

                // 3. Rutas para USUARIOS COMUNES (y también por ADMINs, ya que un ADMIN suele tener también permisos de USER)
                // Estas rutas requieren autenticación y que el usuario tenga el rol USER o ADMIN
                .requestMatchers("/", "/profile","/tasks/**", "/calendar/**", "/api/**", "/profile/**").hasAnyRole("USER", "ADMIN")

                // 4. Cualquier otra solicitud que no haya sido explícitamente permitida o
                // restringida por rol, será DENIEDA.
                // Si un usuario no autenticado intenta acceder a una ruta protegida,
                // será redirigido automáticamente a la página de login (definida en formLogin).
                // Si un usuario autenticado pero sin los roles necesarios intenta acceder,
                // se activará el accessDeniedHandler.
                .anyRequest().denyAll()
            )
            .formLogin(form -> form
                .loginPage("/login")                  // La página de login que se mostrará por defecto si el acceso es denegado
                .loginProcessingUrl("/login")         // La URL a la que el formulario de login principal envía las credenciales (POST)
                .successHandler(customAuthenticationSuccessHandler) // Utiliza tu CustomAuthenticationSuccessHandler para manejar la redirección post-login
                .failureUrl("/login?error")           // URL a la que redirigir en caso de login fallido
                .permitAll()                          // Permitir acceso a la página de login y su URL de procesamiento
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST")) // La URL para cerrar sesión (método POST)
                .logoutSuccessUrl("/login?logout")    // URL a la que redirigir después de cerrar sesión
                .invalidateHttpSession(true)          // Invalidar la sesión HTTP
                .deleteCookies("JSESSIONID")          // Eliminar la cookie de sesión
                .permitAll()                          // Permitir acceso a la URL de logout
            )
            .exceptionHandling(exceptions -> exceptions
                // Este handler se activa cuando un usuario AUTENTICADO intenta acceder a una página SIN PERMISO.
                // (No se activa para usuarios no autenticados que intentan acceder a una página protegida; para esos, Spring redirige a loginPage).
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
                // Redirige al usuario a la página principal si el acceso es denegado
                // Esto es para usuarios autenticados sin el rol adecuado.
                response.sendRedirect("/");
            }
        };
    }
}