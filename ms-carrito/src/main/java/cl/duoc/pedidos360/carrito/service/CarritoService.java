package cl.duoc.pedidos360.carrito.service;

import cl.duoc.pedidos360.carrito.model.ItemCarrito;
import cl.duoc.pedidos360.carrito.repository.ItemCarritoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CarritoService {

    private final ItemCarritoRepository items;

    public CarritoService(ItemCarritoRepository items) {
        this.items = items;
    }

    public List<ItemCarrito> verCarrito(String usuarioOid) {
        return items.findByUsuarioOid(usuarioOid);
    }

    /**
     * El servicio no conoce el catalogo: los datos del producto vienen en la peticion,
     * porque un microservicio no consulta a otro. Si el producto ya estaba en el carrito
     * sumamos cantidades en vez de duplicar la linea.
     */
    public ItemCarrito agregar(String usuarioOid, Long productoId, String nombreProducto,
                               Integer precioUnitario, int cantidad) {
        return items.findByUsuarioOidAndProductoId(usuarioOid, productoId)
                .map(item -> {
                    item.setCantidad(item.getCantidad() + cantidad);
                    return items.save(item);
                })
                .orElseGet(() -> items.save(
                        new ItemCarrito(usuarioOid, productoId, nombreProducto, precioUnitario, cantidad)));
    }

    /** Solo se puede borrar un item propio, aunque se adivine el id de otro usuario. */
    public void quitar(String usuarioOid, Long itemId) {
        ItemCarrito item = items.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("No existe el item " + itemId));

        if (!item.getUsuarioOid().equals(usuarioOid)) {
            throw new NoSuchElementException("No existe el item " + itemId);
        }
        items.delete(item);
    }

    @Transactional
    public void vaciar(String usuarioOid) {
        items.deleteByUsuarioOid(usuarioOid);
    }
}
