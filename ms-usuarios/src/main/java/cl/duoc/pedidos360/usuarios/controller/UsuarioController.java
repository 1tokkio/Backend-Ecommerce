package cl.duoc.pedidos360.usuarios.controller;

import cl.duoc.pedidos360.usuarios.model.Usuario;
import cl.duoc.pedidos360.usuarios.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    /** Ruta abierta. Sirve para comprobar que el servicio responde sin token. */
    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "ms-usuarios", "estado", "operativo");
    }

    /** Perfil del usuario del token. Devuelve tambien los claims que usamos para autorizar. */
    @GetMapping("/perfil")
    public ResponseEntity<Map<String, Object>> perfil(@AuthenticationPrincipal Jwt token,
                                                       Authentication authentication) {
        // El rol ya lo resolvio SecurityConfig, no se vuelve a leer el claim aca.
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_Admin"));
        Usuario usuario = servicio.sincronizar(token, esAdmin);

        // LinkedHashMap y no Map.of: los claims pueden llegar nulos.
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", token.getIssuer() == null ? null : token.getIssuer().toString());
        claims.put("aud", token.getAudience());
        claims.put("roles", token.getClaimAsStringList("roles"));
        claims.put("cognito:groups", token.getClaimAsStringList("cognito:groups"));
        claims.put("exp", token.getExpiresAt());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", usuario.getId());
        respuesta.put("nombre", usuario.getNombre());
        respuesta.put("correo", usuario.getCorreo());
        respuesta.put("rol", usuario.getRol());
        respuesta.put("fechaRegistro", usuario.getFechaRegistro());
        respuesta.put("claims", claims);

        return ResponseEntity.ok(respuesta);
    }

    /** Listado completo. Un token sin el rol Admin recibe 403 aunque sea valido. */
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(servicio.listarTodos());
    }
}
