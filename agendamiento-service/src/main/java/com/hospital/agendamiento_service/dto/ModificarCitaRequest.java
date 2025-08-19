package com.hospital.agendamiento_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModificarCitaRequest {

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long idCita;

    @Future(message = "La fecha debe ser futura")
    private LocalDateTime inicio;

    private LocalDateTime fin;
    private String motivo;

    @NotNull(message = "El requestId es obligatorio para idempotencia")
    private String requestId;
}