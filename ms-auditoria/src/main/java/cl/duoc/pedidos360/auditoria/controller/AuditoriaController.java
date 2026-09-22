package cl.duoc.pedidos360.auditoria.controller;

import cl.duoc.pedidos360.auditoria.model.EventoAuditoria;
import cl.duoc.pedidos360.auditoria.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {

    private final AuditoriaService servicio;

    public AuditoriaController(AuditoriaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "ms-auditoria", "estado", "operativo");
    }

    /** Los eventos de todo el sistema solo los revisa un Admin. */
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<EventoAuditoria>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }
}
