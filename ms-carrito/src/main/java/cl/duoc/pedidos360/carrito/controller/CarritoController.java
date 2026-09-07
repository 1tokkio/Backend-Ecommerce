package cl.duoc.pedidos360.carrito.controller;

import cl.duoc.pedidos360.carrito.model.ItemCarrito;
import cl.duoc.pedidos360.carrito.model.Producto;
import cl.duoc.pedidos360.carrito.service.CarritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {

    private final CarritoService servicio;

    public CarritoController(CarritoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "ms-carrito", "estado", "operativo");
    }

    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> catalogo() {
        return ResponseEntity.ok(servicio.catalogo());
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> verCarrito(@AuthenticationPrincipal Jwt token) {
        List<ItemCarrito> items = servicio.verCarrito(oidDe(token));

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("items", items);
        respuesta.put("cantidadItems", items.size());
        respuesta.put("total", items.stream().mapToInt(ItemCarrito::getSubtotal).sum());
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/items")
    public ResponseEntity<?> agregar(@AuthenticationPrincipal Jwt token,
                                     @RequestBody AgregarItem peticion) {
        if (peticion.productoId() == null || peticion.cantidad() == null || peticion.cantidad() < 1) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Se requiere productoId y una cantidad mayor a cero"));
        }
        try {
            ItemCarrito item = servicio.agregar(oidDe(token), peticion.productoId(), peticion.cantidad());
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> quitar(@AuthenticationPrincipal Jwt token, @PathVariable Long itemId) {
        try {
            servicio.quitar(oidDe(token), itemId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciar(@AuthenticationPrincipal Jwt token) {
        servicio.vaciar(oidDe(token));
        return ResponseEntity.noContent().build();
    }

    private String oidDe(Jwt token) {
        return token.getClaimAsString("oid");
    }

    public record AgregarItem(Long productoId, Integer cantidad) {
    }
}
