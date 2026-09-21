package cl.duoc.pedidos360.productos.mensajeria;

import cl.duoc.pedidos360.productos.config.RabbitConfig;
import cl.duoc.pedidos360.productos.service.ProductoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumidorPedidos {

    private final ProductoService servicio;

    public ConsumidorPedidos(ProductoService servicio) {
        this.servicio = servicio;
    }

    @RabbitListener(queues = RabbitConfig.COLA)
    public void alCrearseUnPedido(PedidoCreado pedido) {
        servicio.descontarStock(pedido);
    }
}
