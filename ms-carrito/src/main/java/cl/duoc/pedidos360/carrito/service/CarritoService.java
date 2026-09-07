package cl.duoc.pedidos360.carrito.service;

import cl.duoc.pedidos360.carrito.model.ItemCarrito;
import cl.duoc.pedidos360.carrito.model.Producto;
import cl.duoc.pedidos360.carrito.repository.ItemCarritoRepository;
import cl.duoc.pedidos360.carrito.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CarritoService {

    private final ProductoRepository productos;
    private final ItemCarritoRepository items;

    public CarritoService(ProductoRepository productos, ItemCarritoRepository items) {
        this.productos = productos;
        this.items = items;
    }

    public List<Producto> catalogo() {
        return productos.findAll();
    }

    public List<ItemCarrito> verCarrito(String usuarioOid) {
        return items.findByUsuarioOid(usuarioOid);
    }

    /** Si el producto ya estaba en el carrito sumamos cantidades en vez de duplicar la linea. */
    public ItemCarrito agregar(String usuarioOid, Long productoId, int cantidad) {
        Producto producto = productos.findById(productoId)
                .orElseThrow(() -> new NoSuchElementException("No existe el producto " + productoId));

        return items.findByUsuarioOidAndProductoId(usuarioOid, productoId)
                .map(item -> {
                    item.setCantidad(item.getCantidad() + cantidad);
                    return items.save(item);
                })
                .orElseGet(() -> items.save(new ItemCarrito(usuarioOid, producto, cantidad)));
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
