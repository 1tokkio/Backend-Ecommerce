package cl.duoc.pedidos360.productos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara el exchange, la cola y el enlace. Si ya existen en RabbitMQ no pasa nada,
 * asi el servicio arranca en cualquier orden respecto de los demas.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pedidos360";
    public static final String COLA = "productos.stock";
    public static final String CLAVE = "pedido.creado";

    @Bean
    public TopicExchange intercambio() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaStock() {
        return new Queue(COLA, true);
    }

    @Bean
    public Binding enlaceStock(Queue colaStock, TopicExchange intercambio) {
        return BindingBuilder.bind(colaStock).to(intercambio).with(CLAVE);
    }

    /** Con este convertidor el mensaje viaja como JSON y se puede leer en la consola de RabbitMQ. */
    @Bean
    public MessageConverter convertidor() {
        return new Jackson2JsonMessageConverter();
    }
}
