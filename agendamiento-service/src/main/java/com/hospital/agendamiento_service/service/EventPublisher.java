package com.hospital.agendamiento_service.service;

import com.hospital.agendamiento_service.entity.Cita;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String EXCHANGE = "citas.exchange";

    /**
     * Publicar evento cuando se agenda una cita
     */
    public void publicarCitaAgendada(Cita cita) {
        try {
            Map<String, Object> evento = crearEventoBase(cita);
            evento.put("tipo", "CITA_AGENDADA");
            evento.put("timestamp", LocalDateTime.now().toString());

            String mensaje = objectMapper.writeValueAsString(evento);

            rabbitTemplate.convertAndSend(
                    EXCHANGE,
                    "citas.cita-agendada",
                    mensaje
            );

            System.out.println("Evento publicado: citas.cita-agendada para cita ID: " + cita.getId());

        } catch (Exception e) {
            System.err.println("Error al publicar evento cita-agendada: " + e.getMessage());
        }
    }

    /**
     * Publicar evento cuando se modifica una cita
     */
    public void publicarCitaModificada(Cita cita) {
        try {
            Map<String, Object> evento = crearEventoBase(cita);
            evento.put("tipo", "CITA_MODIFICADA");
            evento.put("timestamp", LocalDateTime.now().toString());

            String mensaje = objectMapper.writeValueAsString(evento);

            rabbitTemplate.convertAndSend(
                    EXCHANGE,
                    "citas.cita-modificada",
                    mensaje
            );

            System.out.println("Evento publicado: citas.cita-modificada para cita ID: " + cita.getId());

        } catch (Exception e) {
            System.err.println("Error al publicar evento cita-modificada: " + e.getMessage());
        }
    }

    /**
     * Publicar evento cuando se anula una cita
     */
    public void publicarCitaAnulada(Cita cita) {
        try {
            Map<String, Object> evento = crearEventoBase(cita);
            evento.put("tipo", "CITA_ANULADA");
            evento.put("timestamp", LocalDateTime.now().toString());

            String mensaje = objectMapper.writeValueAsString(evento);

            rabbitTemplate.convertAndSend(
                    EXCHANGE,
                    "citas.cita-anulada",
                    mensaje
            );

            System.out.println("Evento publicado: citas.cita-anulada para cita ID: " + cita.getId());

        } catch (Exception e) {
            System.err.println("Error al publicar evento cita-anulada: " + e.getMessage());
        }
    }

    /**
     * Crear estructura base del evento
     */
    private Map<String, Object> crearEventoBase(Cita cita) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("citaId", cita.getId());
        evento.put("idPaciente", cita.getIdPaciente());
        evento.put("idMedico", cita.getIdMedico());
        evento.put("inicio", cita.getInicio().toString());
        evento.put("fin", cita.getFin().toString());
        evento.put("estado", cita.getEstado().toString());
        evento.put("motivo", cita.getMotivo());
        evento.put("requestId", cita.getRequestId());
        return evento;
    }
}