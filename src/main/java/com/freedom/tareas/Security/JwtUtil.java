package com.freedom.tareas.Security;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    // Inyecta la clave secreta JWT desde las propiedades de la aplicación.
    @Value("${jwt.secret}")
    private String secretString;

    // Inyecta el tiempo de expiración del JWT desde las propiedades de la aplicación.
    @Value("${jwt.expiration}")
    private long jwtExpirationTime;

    // Almacena la clave secreta decodificada para la firma de JWT.
    private SecretKey SECRET_KEY;

    // Inicializa la clave secreta después de que Spring inyecte las propiedades.
    @PostConstruct
    public void init() {
        if (secretString == null || secretString.isEmpty()) {
            System.err.println("DEBUG JWT ERROR: La clave secreta JWT no puede ser nula o vacía. Verifica application.properties.");
            throw new IllegalArgumentException("JWT secret key must not be null or empty. Check application.properties.");
        }
        this.SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
        System.out.println("LOG: JwtUtil inicializado con clave secreta.");
    }

    // Sección de Extracción de Claims
    // Extrae el nombre de usuario del token JWT.
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrae la fecha de expiración del token JWT.
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrae los roles del token JWT y los convierte a GrantedAuthority.
    public Collection<? extends GrantedAuthority> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null || !claims.containsKey("roles")) {
            System.out.println("DEBUG JWT: No se encontraron roles en el token o claims nulos.");
            return List.of();
        }
        Object rolesObj = claims.get("roles");
        List<String> roles;
        if (rolesObj instanceof List<?> list) { 
            roles = list.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            roles = List.of();
        }
        System.out.println("DEBUG JWT: Roles extraídos del token: " + roles);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // Extrae un claim específico del token JWT usando una función resolutora.
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    // Extrae todos los claims del token JWT, manejando posibles excepciones.
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException e) {
            System.out.println("\n************************************************************************************");
            System.out.println("DEBUG JWT ERROR: Firma JWT inválida: " + e.getMessage());
            System.out.println("************************************************************************************\n");
        } catch (MalformedJwtException e) {
            System.out.println("\n************************************************************************************");
            System.out.println("DEBUG JWT ERROR: Token JWT malformado: " + e.getMessage());
            System.out.println("************************************************************************************\n");
        } catch (ExpiredJwtException e) {
            System.out.println("\n************************************************************************************");
            System.out.println("DEBUG JWT ERROR: Token JWT expirado: " + e.getMessage());
            System.out.println("************************************************************************************\n");
        } catch (IllegalArgumentException e) {
            System.out.println("\n************************************************************************************");
            System.out.println("DEBUG JWT ERROR: Cadena JWT vacía o nula: " + e.getMessage());
            System.out.println("************************************************************************************\n");
        } catch (RuntimeException e) {
            System.out.println("\n************************************************************************************");
            System.out.println("DEBUG JWT ERROR: Error al parsear el token JWT: " + e.getMessage());
            System.out.println("************************************************************************************\n");
        }
        return null;
    }

    // Sección de Validación de Expiración
    // Verifica si el token JWT ha expirado.
    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean expired = expiration != null && expiration.before(new Date());
        if (expired) {
            System.out.println("DEBUG JWT: Token expirado. Fecha de expiración: " + expiration);
        } else {
            System.out.println("DEBUG JWT: Token no expirado. Fecha de expiración: " + expiration);
        }
        return expired;
    }

    // Sección de Generación de Token
    // Genera un token JWT para un usuario, incluyendo sus roles en los claims.
    public String generateToken(UserDetails userDetails) {
        System.out.println("DEBUG JWT: Iniciando generación de token para usuario: " + userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);
        String token = createToken(claims, userDetails.getUsername());
        System.out.println("DEBUG JWT: Token generado con éxito para: " + userDetails.getUsername());
        return token;
    }

    // Crea el token JWT con los claims, sujeto, fecha de emisión y expiración.
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Sección de Validación de Token
    // Valida si el token JWT es válido para un usuario y no ha expirado.
    public Boolean validateToken(String token, UserDetails userDetails) {
        System.out.println("DEBUG JWT: Iniciando validación de token para usuario: " + userDetails.getUsername());
        final String username = extractUsername(token);
        boolean isValid = (username != null && username.equals(userDetails.getUsername()) &&
                           !isTokenExpired(token));
        if (isValid) {
            System.out.println("DEBUG JWT: Token validado exitosamente para usuario: " + username);
        } else {
            System.out.println("DEBUG JWT: Fallo en la validación del token para usuario: " + username);
        }
        return isValid;
    }
}
