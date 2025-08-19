package com.hospital.agendamiento_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnularCitaRequest {

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long idCita;

    @NotNull(message = "El requestId es obligatorio para idempotencia")
    private String requestId;

    private String motivo;
}