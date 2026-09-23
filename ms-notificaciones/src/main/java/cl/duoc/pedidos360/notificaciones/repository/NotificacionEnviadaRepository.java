package cl.duoc.pedidos360.notificaciones.repository;

import cl.duoc.pedidos360.notificaciones.model.NotificacionEnviada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionEnviadaRepository extends JpaRepository<NotificacionEnviada, Long> {

    List<NotificacionEnviada> findAllByOrderByFechaEnvioDesc();
}
