package cl.duoc.pedidos360.productos.controller;

import cl.duoc.pedidos360.productos.dto.NuevoProducto;
import cl.duoc.pedidos360.productos.model.Producto;
import cl.duoc.pedidos360.productos.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "ms-productos", "estado", "operativo");
    }

    /** Sin id devuelve el catalogo completo; con id devuelve un solo producto. */
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Long id) {
        if (id == null) {
            return ResponseEntity.ok(servicio.listar());
        }
        return servicio.buscar(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "No existe el producto " + id)));
    }

    /** Alta de catalogo. Solo un Admin puede agregar productos nuevos. */
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> crear(@RequestBody NuevoProducto peticion) {
        if (peticion.nombre() == null || peticion.precio() == null || peticion.precio() < 0
                || peticion.stock() == null || peticion.stock() < 0) {
            return ResponseEntity.badRequest().body(Map.of("mensaje",
                    "Se requiere nombre, un precio y un stock validos"));
        }
        Producto producto = servicio.crear(peticion);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }
}
