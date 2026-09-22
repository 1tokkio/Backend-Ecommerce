package cl.duoc.pedidos360.notificaciones.mensajeria;

import cl.duoc.pedidos360.notificaciones.config.RabbitConfig;
import cl.duoc.pedidos360.notificaciones.service.CorreoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumidorPedidos {

    private final CorreoService correo;

    public ConsumidorPedidos(CorreoService correo) {
        this.correo = correo;
    }

    @RabbitListener(queues = RabbitConfig.COLA)
    public void alCrearseUnPedido(PedidoCreado pedido) {
        correo.enviarConfirmacion(pedido.correoUsuario(), pedido.ordenId(), pedido.total());
    }
}
