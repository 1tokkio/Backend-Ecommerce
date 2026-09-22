package cl.duoc.pedidos360.notificaciones.controller;

import cl.duoc.pedidos360.notificaciones.service.CorreoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final CorreoService correo;

    public NotificacionController(CorreoService correo) {
        this.correo = correo;
    }

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "ms-notificaciones", "estado", "operativo");
    }

    /** Envia un correo de prueba sin pasar por RabbitMQ, para probar el SMTP desde Postman. */
    @PostMapping("/prueba")
    public ResponseEntity<Map<String, String>> enviarPrueba(@RequestBody EnvioPrueba peticion) {
        correo.enviarConfirmacion(peticion.destinatario(), 0L, 0);
        return ResponseEntity.ok(Map.of("mensaje", "Correo de prueba enviado a " + peticion.destinatario()));
    }

    public record EnvioPrueba(String destinatario) {
    }
}
