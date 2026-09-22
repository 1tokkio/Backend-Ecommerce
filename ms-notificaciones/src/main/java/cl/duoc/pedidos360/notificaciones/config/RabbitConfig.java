package cl.duoc.pedidos360.notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pedidos360";
    public static final String COLA = "notificaciones.correo";
    public static final String CLAVE = "pedido.creado";

    @Bean
    public TopicExchange intercambio() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaCorreo() {
        return new Queue(COLA, true);
    }

    @Bean
    public Binding enlaceCorreo(Queue colaCorreo, TopicExchange intercambio) {
        return BindingBuilder.bind(colaCorreo).to(intercambio).with(CLAVE);
    }

    @Bean
    public MessageConverter convertidor() {
        return new Jackson2JsonMessageConverter();
    }
}
