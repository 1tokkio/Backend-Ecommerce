package cl.duoc.pedidos360.carrito.repository;

import cl.duoc.pedidos360.carrito.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    List<ItemCarrito> findByUsuarioOid(String usuarioOid);

    Optional<ItemCarrito> findByUsuarioOidAndProductoId(String usuarioOid, Long productoId);

    void deleteByUsuarioOid(String usuarioOid);
}
