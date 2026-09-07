package cl.duoc.pedidos360.pedidos.controller;

import cl.duoc.pedidos360.pedidos.dto.NuevoPedido;
import cl.duoc.pedidos360.pedidos.model.Pedido;
import cl.duoc.pedidos360.pedidos.service.PedidoService;
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
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "ms-pedidos", "estado", "operativo");
    }

    /** El pedido siempre se registra a nombre del dueño del token, no del cuerpo de la peticion. */
    @PostMapping
    public ResponseEntity<?> crear(@AuthenticationPrincipal Jwt token,
                                   @RequestBody NuevoPedido peticion) {
        if (peticion.items() == null || peticion.items().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El pedido no puede ir vacio"));
        }

        Pedido pedido = servicio.crear(
                token.getClaimAsString("oid"),
                correoDe(token),
                peticion.items());

        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<Pedido>> misPedidos(@AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(servicio.misPedidos(token.getClaimAsString("oid")));
    }

    /** Pedidos de todos los clientes. Requiere el rol Admin, si no responde 403. */
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<Pedido>> todos() {
        return ResponseEntity.ok(servicio.todos());
    }

    private String correoDe(Jwt token) {
        String correo = token.getClaimAsString("preferred_username");
        if (correo == null) {
            correo = token.getClaimAsString("email");
        }
        return correo == null ? "sin-correo" : correo;
    }
}
