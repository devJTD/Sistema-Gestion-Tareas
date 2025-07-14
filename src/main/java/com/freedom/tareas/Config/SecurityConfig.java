package com.freedom.tareas.Config;

import com.freedom.tareas.Service.UserService;
import com.freedom.tareas.Security.JwtRequestFilter;
import com.freedom.tareas.Security.JwtAuthenticationEntryPoint;
import com.freedom.tareas.Security.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

import java.io.IOException;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Sección de Dependencias
    // Inyecta los servicios y filtros necesarios para la configuración de seguridad.
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public SecurityConfig(UserService userService,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        System.out.println("LOG: SecurityConfig inicializado.");
    }

    // Sección de Beans de Seguridad
    // Crea una instancia de JwtRequestFilter como un Bean.
    @Bean
    public JwtRequestFilter jwtRequestFilterBean(UserService userService, JwtUtil jwtUtil) {
        System.out.println("LOG: Creando bean JwtRequestFilter.");
        return new JwtRequestFilter(userService, jwtUtil);
    }

    // Crea una instancia de JwtAuthenticationEntryPoint como un Bean.
    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        System.out.println("LOG: Creando bean JwtAuthenticationEntryPoint.");
        return new JwtAuthenticationEntryPoint();
    }

    // Sección para deshabilitar el registro global del JwtRequestFilter
    @Bean
    public FilterRegistrationBean<JwtRequestFilter> jwtFilterRegistration(JwtRequestFilter jwtRequestFilterBean) {
        System.out.println("LOG: Deshabilitando el registro global de JwtRequestFilter.");
        FilterRegistrationBean<JwtRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtRequestFilterBean);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    // Sección de RequestMatcher para APIs JWT
    private RequestMatcher apiJwtRequestMatcher() {
        System.out.println("LOG: Definiendo RequestMatcher para APIs JWT.");
        @SuppressWarnings({ "removal", "deprecation" })
        AntPathRequestMatcher apiBaseMatcher = new AntPathRequestMatcher("/api/**");
        @SuppressWarnings({ "removal", "deprecation" })
        AntPathRequestMatcher authenticateMatcher = new AntPathRequestMatcher("/api/authenticate");
        return new AndRequestMatcher(apiBaseMatcher, new NegatedRequestMatcher(authenticateMatcher));
    }

    // Sección de Cadena de Filtros para APIs (JWT)
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("\n************************************************************************************");
        System.out.println("DEBUG JWT: Configurando SecurityFilterChain para APIs (Orden 1).");
        http
                .securityMatcher(apiJwtRequestMatcher())
                .csrf(csrf -> {
                    csrf.disable();
                    System.out.println("DEBUG JWT: CSRF deshabilitado para APIs JWT (sin estado).");
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    System.out.println("DEBUG JWT: Gestión de sesión establecida a STATELESS para APIs JWT.");
                })
                .exceptionHandling(exceptions -> {
                    exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint());
                    System.out.println("DEBUG JWT: AuthenticationEntryPoint configurado para APIs JWT.");
                })
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated())
                .addFilterBefore(jwtRequestFilterBean(userService, jwtUtil), UsernamePasswordAuthenticationFilter.class);
        System.out.println("DEBUG JWT: JwtRequestFilter añadido antes de UsernamePasswordAuthenticationFilter para APIs.");
        System.out.println("************************************************************************************\n");
        return http.build();
    }

    // Sección de Cadena de Filtros para la Aplicación Web (Sesión)
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("LOG: Configurando SecurityFilterChain para la aplicación web (Orden 2).");
        http
                .csrf(csrf -> {
                    csrf.disable();
                    System.out.println("LOG: CSRF deshabilitado para la aplicación web.");
                })
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login/**", "/register/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/authenticate").permitAll()
                        .requestMatchers("/admin", "/admin/**", "/admin/api/**").hasRole("ADMIN")
                        // Aquí se añadió "/archived/**"
                        .requestMatchers("/", "/profile", "/tasks/**", "/calendar/**", "/profile/**", "/trash/**", "/archived/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().denyAll())
                .formLogin(form -> {
                    form.loginPage("/login")
                            .loginProcessingUrl("/login")
                            .failureUrl("/login?error")
                            .permitAll();
                    System.out.println("LOG: Formulario de login configurado.");
                })
                .logout(logout -> {
                    logout.logoutUrl("/logout")
                            .logoutSuccessUrl("/login?logout")
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll();
                    System.out.println("LOG: Funcionalidad de cierre de sesión configurada.");
                })
                .exceptionHandling(exceptions -> {
                    exceptions.accessDeniedHandler(accessDeniedHandler())
                            .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
                    System.out.println("LOG: Manejo de excepciones (acceso denegado y punto de entrada) configurado.");
                });

        return http.build();
    }

    // Sección de Proveedor de Autenticación
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        System.out.println("LOG: Creando bean DaoAuthenticationProvider.");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(this.passwordEncoder);
        return provider;
    }

    // Sección de AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        System.out.println("LOG: Exponiendo bean AuthenticationManager.");
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Sección de Manejador de Acceso Denegado
    @Bean
    @SuppressWarnings("Convert2Lambda")
    public AccessDeniedHandler accessDeniedHandler() {
        System.out.println("LOG: Creando bean AccessDeniedHandler.");
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                               AccessDeniedException accessDeniedException) throws IOException, ServletException {
                System.out.println("LOG: Acceso denegado para la ruta: " + request.getRequestURI());
                response.sendRedirect("/");
            }
        };
    }
}