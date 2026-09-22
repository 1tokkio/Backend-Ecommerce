package cl.duoc.pedidos360.ordenes.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ms-ordenes solo publica, no declara colas: cada consumidor declara la suya
 * al arrancar. Lo unico que este servicio necesita es el exchange.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pedidos360";
    public static final String CLAVE_PEDIDO_CREADO = "pedido.creado";

    @Bean
    public TopicExchange intercambio() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter convertidor() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory conexion,
                                         MessageConverter convertidor) {
        RabbitTemplate template = new RabbitTemplate(conexion);
        template.setMessageConverter(convertidor);
        return template;
    }
}
