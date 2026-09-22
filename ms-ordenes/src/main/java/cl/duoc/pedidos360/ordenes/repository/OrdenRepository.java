package cl.duoc.pedidos360.ordenes.repository;

import cl.duoc.pedidos360.ordenes.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByUsuarioOidOrderByFechaCreacionDesc(String usuarioOid);
}
