package cl.duoc.pedidos360.auditoria.mensajeria;

import cl.duoc.pedidos360.auditoria.config.RabbitConfig;
import cl.duoc.pedidos360.auditoria.service.AuditoriaService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ConsumidorEventos {

    private final AuditoriaService servicio;

    public ConsumidorEventos(AuditoriaService servicio) {
        this.servicio = servicio;
    }

    /**
     * Se recibe el mensaje crudo, no un tipo especifico: auditoria no conoce la forma
     * de cada evento, solo la registra tal cual llego, con la routing key que la identifica.
     */
    @RabbitListener(queues = RabbitConfig.COLA)
    public void alLlegarUnEvento(Message mensaje) {
        String routingKey = mensaje.getMessageProperties().getReceivedRoutingKey();
        String contenido = new String(mensaje.getBody(), StandardCharsets.UTF_8);
        servicio.registrar(routingKey, contenido);
    }
}
