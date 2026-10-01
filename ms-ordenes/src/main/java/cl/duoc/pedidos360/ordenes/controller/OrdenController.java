package cl.duoc.pedidos360.ordenes.controller;

import cl.duoc.pedidos360.ordenes.dto.NuevaOrden;
import cl.duoc.pedidos360.ordenes.model.Orden;
import cl.duoc.pedidos360.ordenes.service.OrdenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ordenes")
public class OrdenController {

    private final OrdenService servicio;

    public OrdenController(OrdenService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "ms-ordenes", "estado", "operativo");
    }

    /** La orden siempre se registra a nombre del dueño del token, no del cuerpo de la peticion. */
    @PostMapping
    public ResponseEntity<?> crear(@AuthenticationPrincipal Jwt token,
                                   @RequestBody NuevaOrden peticion) {
        if (peticion.items() == null || peticion.items().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La orden no puede ir vacia"));
        }

        Orden orden = servicio.crear(
                identificadorDe(token),
                correoDe(token),
                peticion.items());

        return ResponseEntity.status(HttpStatus.CREATED).body(orden);
    }

    @GetMapping("/mis-ordenes")
    public ResponseEntity<List<Orden>> misOrdenes(@AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(servicio.misOrdenes(identificadorDe(token)));
    }

    /** Ordenes de todos los clientes. Requiere el rol Admin, si no responde 403. */
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<Orden>> todos() {
        return ResponseEntity.ok(servicio.todos());
    }

    /** El identificador estable es "oid" en Azure y "sub" en Cognito. */
    private String identificadorDe(Jwt token) {
        String issuer = token.getIssuer() == null ? "" : token.getIssuer().toString();
        return issuer.contains("cognito-idp") ? token.getSubject() : token.getClaimAsString("oid");
    }

    private String correoDe(Jwt token) {
        String correo = token.getClaimAsString("preferred_username");
        if (correo == null) {
            correo = token.getClaimAsString("email");
        }
        return correo == null ? "sin-correo" : correo;
    }
}
