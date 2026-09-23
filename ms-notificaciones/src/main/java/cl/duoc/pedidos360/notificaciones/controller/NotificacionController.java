package cl.duoc.pedidos360.notificaciones.controller;

import cl.duoc.pedidos360.notificaciones.model.NotificacionEnviada;
import cl.duoc.pedidos360.notificaciones.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final NotificacionService servicio;

    public NotificacionController(NotificacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "ms-notificaciones", "estado", "operativo");
    }

    /** Envia un correo de prueba sin pasar por RabbitMQ, para probar el SMTP desde Postman. */
    @PostMapping("/enviar")
    public ResponseEntity<NotificacionEnviada> enviar(@RequestBody EnvioNotificacion peticion) {
        NotificacionEnviada resultado = servicio.enviar(peticion.destinatario(), null, 0);
        return ResponseEntity.ok(resultado);
    }

    /** Historial de notificaciones enviadas. Solo lo revisa un Admin. */
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<NotificacionEnviada>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }

    public record EnvioNotificacion(String destinatario) {
    }
}
