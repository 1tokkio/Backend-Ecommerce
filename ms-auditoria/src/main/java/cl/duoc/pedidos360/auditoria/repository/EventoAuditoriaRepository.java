package cl.duoc.pedidos360.auditoria.repository;

import cl.duoc.pedidos360.auditoria.model.EventoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    List<EventoAuditoria> findAllByOrderByFechaRegistroDesc();
}
