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

    /** Envia el correo y deja registrado el intento, haya salido o no. */
    public NotificacionEnviada enviar(String destinatario, Long ordenId, Integer total) {
        boolean enviada = correo.enviarConfirmacion(destinatario, ordenId, total);
        return repositorio.save(new NotificacionEnviada(destinatario, ordenId, total, enviada));
    }

    public List<NotificacionEnviada> listar() {
        return repositorio.findAllByOrderByFechaEnvioDesc();
    }
}
