package com.hospital.agendamiento_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaResponse {
    private Long id;
    private Long idPaciente;
    private Long idMedico;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private String estado;
    private String motivo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}