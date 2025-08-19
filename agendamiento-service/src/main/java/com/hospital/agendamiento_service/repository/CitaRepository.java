package com.hospital.agendamiento_service.repository;

import com.hospital.agendamiento_service.entity.Cita;
import com.hospital.agendamiento_service.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Buscar por requestId para idempotencia
    Optional<Cita> findByRequestId(String requestId);

    // Verificar disponibilidad del médico en un horario específico
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.idMedico = :idMedico " +
            "AND c.estado IN ('AGENDADA', 'CONFIRMADA') " +
            "AND ((c.inicio <= :inicio AND c.fin > :inicio) " +
            "OR (c.inicio < :fin AND c.fin >= :fin) " +
            "OR (c.inicio >= :inicio AND c.fin <= :fin))")
    int countCitasConflictivasMedico(@Param("idMedico") Long idMedico,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fin") LocalDateTime fin);

    // Verificar disponibilidad del paciente en un horario específico
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.idPaciente = :idPaciente " +
            "AND c.estado IN ('AGENDADA', 'CONFIRMADA') " +
            "AND ((c.inicio <= :inicio AND c.fin > :inicio) " +
            "OR (c.inicio < :fin AND c.fin >= :fin) " +
            "OR (c.inicio >= :inicio AND c.fin <= :fin))")
    int countCitasConflictivasPaciente(@Param("idPaciente") Long idPaciente,
                                       @Param("inicio") LocalDateTime inicio,
                                       @Param("fin") LocalDateTime fin);

    // Buscar citas por paciente
    List<Cita> findByIdPacienteAndEstadoInOrderByInicioAsc(Long idPaciente, List<EstadoCita> estados);

    // Buscar citas por médico
    List<Cita> findByIdMedicoAndEstadoInOrderByInicioAsc(Long idMedico, List<EstadoCita> estados);

    // Buscar citas por fecha
    @Query("SELECT c FROM Cita c WHERE c.inicio >= :fechaInicio AND c.inicio < :fechaFin " +
            "AND c.estado IN :estados ORDER BY c.inicio ASC")
    List<Cita> findCitasByFechaAndEstados(@Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFin") LocalDateTime fechaFin,
                                          @Param("estados") List<EstadoCita> estados);

    // Buscar citas activas (no anuladas)
    @Query("SELECT c FROM Cita c WHERE c.estado != 'ANULADA' ORDER BY c.inicio ASC")
    List<Cita> findCitasActivas();
}