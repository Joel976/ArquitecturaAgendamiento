package com.hospital.agendamiento_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Exchange principal para eventos de citas
    public static final String CITAS_EXCHANGE = "citas.exchange";

    // Routing keys para eventos
    public static final String CITA_AGENDADA_KEY = "citas.cita-agendada";
    public static final String CITA_MODIFICADA_KEY = "citas.cita-modificada";
    public static final String CITA_ANULADA_KEY = "citas.cita-anulada";

    /**
     * Topic Exchange para eventos de citas
     */
    @Bean
    public TopicExchange citasExchange() {
        return new TopicExchange(CITAS_EXCHANGE, true, false);
    }

    /**
     * Converter para mensajes JSON
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configurado con converter JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // ================== COLAS PARA TESTING ==================
    // Estas colas las crearán los otros microservicios que las consuman
    // Las incluyo aquí solo para desarrollo y testing

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable("notificaciones.queue").build();
    }

    @Bean
    public Queue medicosQueue() {
        return QueueBuilder.durable("medicos.queue").build();
    }

    @Bean
    public Queue pacientesQueue() {
        return QueueBuilder.durable("pacientes.queue").build();
    }

    // Bindings para testing
    @Bean
    public Binding bindingNotificaciones() {
        return BindingBuilder
                .bind(notificacionesQueue())
                .to(citasExchange())
                .with("citas.*");
    }

    @Bean
    public Binding bindingMedicos() {
        return BindingBuilder
                .bind(medicosQueue())
                .to(citasExchange())
                .with("citas.cita-agendada");
    }

    @Bean
    public Binding bindingPacientes() {
        return BindingBuilder
                .bind(pacientesQueue())
                .to(citasExchange())
                .with("citas.*");
    }
}