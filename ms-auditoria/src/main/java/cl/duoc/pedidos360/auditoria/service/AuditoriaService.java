package cl.duoc.pedidos360.auditoria.service;

import cl.duoc.pedidos360.auditoria.model.EventoAuditoria;
import cl.duoc.pedidos360.auditoria.repository.EventoAuditoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriaService {

    private final EventoAuditoriaRepository repositorio;

    public AuditoriaService(EventoAuditoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void registrar(String routingKey, String contenido) {
        repositorio.save(new EventoAuditoria(routingKey, contenido));
    }

    public List<EventoAuditoria> listar() {
        return repositorio.findAllByOrderByFechaRegistroDesc();
    }
}
