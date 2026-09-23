package cl.duoc.pedidos360.notificaciones.service;

import cl.duoc.pedidos360.notificaciones.model.NotificacionEnviada;
import cl.duoc.pedidos360.notificaciones.repository.NotificacionEnviadaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    private final CorreoService correo;
    private final NotificacionEnviadaRepository repositorio;

    public NotificacionService(CorreoService correo, NotificacionEnviadaRepository repositorio) {
        this.correo = correo;
        this.repositorio = repositorio;
    }

    /** Envio de prueba, sin detalle de items porque no viene de una orden real. */
    public NotificacionEnviada enviar(String destinatario, Long ordenId, Integer total) {
        return enviar(destinatario, ordenId, total, List.of());
    }

    /** Envia el correo y deja registrado el intento, haya salido o no. */
    public NotificacionEnviada enviar(String destinatario, Long ordenId, Integer total, List<String> detalleItems) {
        boolean enviada = correo.enviarConfirmacion(destinatario, ordenId, total, detalleItems);
        return repositorio.save(new NotificacionEnviada(destinatario, ordenId, total, enviada));
    }

    public List<NotificacionEnviada> listar() {
        return repositorio.findAllByOrderByFechaEnvioDesc();
    }
}
