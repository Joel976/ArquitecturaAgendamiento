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
public class AgendarCitaRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long idPaciente;

    @NotNull(message = "El ID del médico es obligatorio")
    private Long idMedico;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime inicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    private LocalDateTime fin;

    private String motivo;

    @NotNull(message = "El requestId es obligatorio para idempotencia")
    private String requestId;
}