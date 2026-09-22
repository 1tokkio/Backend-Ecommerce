package cl.duoc.pedidos360.ordenes.mensajeria;

import java.time.LocalDateTime;
import java.util.List;

/** Mensaje que se publica en el exchange pedidos360 cada vez que se crea una orden. */
public record PedidoCreado(Long ordenId, String usuarioOid, String correoUsuario, Integer total,
                           LocalDateTime fecha, List<Item> items) {

    public record Item(Long productoId, String nombreProducto, Integer cantidad, Integer precioUnitario) {
    }
}
