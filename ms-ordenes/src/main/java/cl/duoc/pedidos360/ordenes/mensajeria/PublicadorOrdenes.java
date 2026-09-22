package cl.duoc.pedidos360.ordenes.mensajeria;

import cl.duoc.pedidos360.ordenes.config.RabbitConfig;
import cl.duoc.pedidos360.ordenes.model.DetalleOrden;
import cl.duoc.pedidos360.ordenes.model.Orden;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublicadorOrdenes {

    private final RabbitTemplate rabbitTemplate;

    public PublicadorOrdenes(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarOrdenCreada(Orden orden, String correoUsuario) {
        List<PedidoCreado.Item> items = orden.getDetalles().stream()
                .map(this::aItem)
                .toList();

        PedidoCreado mensaje = new PedidoCreado(
                orden.getId(), orden.getUsuarioOid(), correoUsuario, orden.getTotal(),
                orden.getFechaCreacion(), items);

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.CLAVE_PEDIDO_CREADO, mensaje);
    }

    private PedidoCreado.Item aItem(DetalleOrden detalle) {
        return new PedidoCreado.Item(
                detalle.getProductoId(), detalle.getNombreProducto(),
                detalle.getCantidad(), detalle.getPrecioUnitario());
    }
}
