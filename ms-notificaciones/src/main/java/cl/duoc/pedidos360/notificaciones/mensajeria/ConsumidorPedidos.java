package cl.duoc.pedidos360.notificaciones.mensajeria;

import cl.duoc.pedidos360.notificaciones.config.RabbitConfig;
import cl.duoc.pedidos360.notificaciones.service.NotificacionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConsumidorPedidos {

    private final NotificacionService notificaciones;

    public ConsumidorPedidos(NotificacionService notificaciones) {
        this.notificaciones = notificaciones;
    }

    @RabbitListener(queues = RabbitConfig.COLA)
    public void alCrearseUnPedido(PedidoCreado pedido) {
        List<String> detalle = pedido.items().stream()
                .map(item -> item.cantidad() + " x " + item.nombreProducto()
                        + " - $" + (item.precioUnitario() * item.cantidad()))
                .toList();

        notificaciones.enviar(pedido.correoUsuario(), pedido.ordenId(), pedido.total(), detalle);
    }
}
