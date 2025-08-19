package com.hospital.agendamiento_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "id_paciente")
    private Long idPaciente;

    @NotNull
    @Column(name = "id_medico")
    private Long idMedico;

    @NotNull
    @Column(name = "inicio")
    private LocalDateTime inicio;

    @NotNull
    @Column(name = "fin")
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoCita estado = EstadoCita.AGENDADA;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "request_id", unique = true)
    private String requestId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructor personalizado
    public Cita(Long idPaciente, Long idMedico, LocalDateTime inicio, LocalDateTime fin, String motivo, String requestId) {
        this.idPaciente = idPaciente;
        this.idMedico = idMedico;
        this.inicio = inicio;
        this.fin = fin;
        this.motivo = motivo;
        this.requestId = requestId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Método personalizado para actualizar estado
    public void setEstado(EstadoCita estado) {
        this.estado = estado;
        this.updatedAt = LocalDateTime.now();
    }
}