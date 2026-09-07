package cl.duoc.pedidos360.carrito.repository;

import cl.duoc.pedidos360.carrito.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
