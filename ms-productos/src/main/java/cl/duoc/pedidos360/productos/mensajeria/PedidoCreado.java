package cl.duoc.pedidos360.productos.mensajeria;

import java.util.List;

/** Mensaje que publica ms-ordenes. De todo el contenido aqui solo importan los items. */
public record PedidoCreado(Long ordenId, List<Item> items) {

    public record Item(Long productoId, Integer cantidad) {
    }
}
