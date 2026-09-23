package cl.duoc.pedidos360.notificaciones.mensajeria;

import cl.duoc.pedidos360.notificaciones.config.RabbitConfig;
import cl.duoc.pedidos360.notificaciones.service.NotificacionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumidorPedidos {

    private final NotificacionService notificaciones;

    public ConsumidorPedidos(NotificacionService notificaciones) {
        this.notificaciones = notificaciones;
    }

    @RabbitListener(queues = RabbitConfig.COLA)
    public void alCrearseUnPedido(PedidoCreado pedido) {
        notificaciones.enviar(pedido.correoUsuario(), pedido.ordenId(), pedido.total());
    }
}
