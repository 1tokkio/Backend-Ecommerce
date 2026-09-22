package cl.duoc.pedidos360.notificaciones.mensajeria;

import java.time.LocalDateTime;
import java.util.List;

/** Mensaje que publica ms-ordenes. Trae todo lo necesario para armar el correo. */
public record PedidoCreado(Long ordenId, String usuarioOid, String correoUsuario, Integer total,
                           LocalDateTime fecha, List<Item> items) {

    public record Item(Long productoId, String nombreProducto, Integer cantidad, Integer precioUnitario) {
    }
}
