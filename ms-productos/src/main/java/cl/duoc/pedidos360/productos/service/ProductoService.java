package cl.duoc.pedidos360.productos.service;

import cl.duoc.pedidos360.productos.mensajeria.PedidoCreado;
import cl.duoc.pedidos360.productos.model.Producto;
import cl.duoc.pedidos360.productos.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Producto> listar() {
        return repositorio.findAll();
    }

    public Optional<Producto> buscar(Long id) {
        return repositorio.findById(id);
    }

    /**
     * Se ejecuta cuando ms-ordenes publica un pedido. Un producto que ya no existe
     * se ignora: la orden ya esta creada y no hay nada que descontar.
     */
    @Transactional
    public void descontarStock(PedidoCreado pedido) {
        for (PedidoCreado.Item item : pedido.items()) {
            repositorio.findById(item.productoId())
                    .ifPresent(producto -> producto.descontar(item.cantidad()));
        }
    }
}
