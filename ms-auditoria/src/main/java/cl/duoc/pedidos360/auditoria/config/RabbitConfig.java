package cl.duoc.pedidos360.auditoria.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** La clave "#" hace que la cola reciba cualquier evento que pase por el exchange. */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pedidos360";
    public static final String COLA = "auditoria.eventos";
    public static final String CLAVE = "#";

    @Bean
    public TopicExchange intercambio() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaEventos() {
        return new Queue(COLA, true);
    }

    @Bean
    public Binding enlaceEventos(Queue colaEventos, TopicExchange intercambio) {
        return BindingBuilder.bind(colaEventos).to(intercambio).with(CLAVE);
    }

    @Bean
    public MessageConverter convertidor() {
        return new Jackson2JsonMessageConverter();
    }
}
