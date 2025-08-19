package com.hospital.agendamiento_service.service;

import com.hospital.agendamiento_service.dto.*;
import com.hospital.agendamiento_service.entity.Cita;
import com.hospital.agendamiento_service.entity.EstadoCita;
import com.hospital.agendamiento_service.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private EventPublisher eventPublisher;

    /**
     * Agendar nueva cita - Flujo principal
     */
    public CitaResponse agendarCita(AgendarCitaRequest request) {
        // 1. Verificar idempotencia
        Optional<Cita> citaExistente = citaRepository.findByRequestId(request.getRequestId());
        if (citaExistente.isPresent()) {
            return convertirACitaResponse(citaExistente.get());
        }

        // 2. Validar horarios
        validarHorarios(request.getInicio(), request.getFin());

        // 3. Verificar disponibilidad del médico
        if (!esMedicoDisponible(request.getIdMedico(), request.getInicio(), request.getFin())) {
            throw new RuntimeException("El médico no está disponible en el horario solicitado");
        }

        // 4. Verificar disponibilidad del paciente
        if (!esPacienteDisponible(request.getIdPaciente(), request.getInicio(), request.getFin())) {
            throw new RuntimeException("El paciente ya tiene una cita en el horario solicitado");
        }

        // 5. Crear y guardar la cita
        Cita nuevaCita = new Cita(
                request.getIdPaciente(),
                request.getIdMedico(),
                request.getInicio(),
                request.getFin(),
                request.getMotivo(),
                request.getRequestId()
        );

        Cita citaGuardada = citaRepository.save(nuevaCita);

        // 6. Publicar evento
        eventPublisher.publicarCitaAgendada(citaGuardada);

        return convertirACitaResponse(citaGuardada);
    }

    /**
     * Modificar cita existente
     */
    public CitaResponse modificarCita(ModificarCitaRequest request) {
        // 1. Verificar idempotencia
        Optional<Cita> citaPorRequest = citaRepository.findByRequestId(request.getRequestId());
        if (citaPorRequest.isPresent()) {
            return convertirACitaResponse(citaPorRequest.get());
        }

        // 2. Buscar cita existente
        Cita cita = citaRepository.findById(request.getIdCita())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // 3. Validar que se puede modificar
        if (cita.getEstado() == EstadoCita.ANULADA) {
            throw new RuntimeException("No se puede modificar una cita anulada");
        }

        // 4. Validar nuevos horarios si se proporcionan
        LocalDateTime nuevoInicio = request.getInicio() != null ? request.getInicio() : cita.getInicio();
        LocalDateTime nuevoFin = request.getFin() != null ? request.getFin() : cita.getFin();

        validarHorarios(nuevoInicio, nuevoFin);

        // 5. Verificar disponibilidad si cambió el horario
        if (!nuevoInicio.equals(cita.getInicio()) || !nuevoFin.equals(cita.getFin())) {
            if (!esMedicoDisponibleParaModificacion(cita.getIdMedico(), nuevoInicio, nuevoFin, cita.getId())) {
                throw new RuntimeException("El médico no está disponible en el nuevo horario");
            }

            if (!esPacienteDisponibleParaModificacion(cita.getIdPaciente(), nuevoInicio, nuevoFin, cita.getId())) {
                throw new RuntimeException("El paciente ya tiene una cita en el nuevo horario");
            }
        }

        // 6. Actualizar cita
        cita.setInicio(nuevoInicio);
        cita.setFin(nuevoFin);
        if (request.getMotivo() != null) {
            cita.setMotivo(request.getMotivo());
        }
        cita.setEstado(EstadoCita.MODIFICADA);
        cita.setRequestId(request.getRequestId());

        Cita citaActualizada = citaRepository.save(cita);

        // 7. Publicar evento
        eventPublisher.publicarCitaModificada(citaActualizada);

        return convertirACitaResponse(citaActualizada);
    }

    /**
     * Anular cita existente
     */
    public CitaResponse anularCita(AnularCitaRequest request) {
        // 1. Verificar idempotencia
        Optional<Cita> citaPorRequest = citaRepository.findByRequestId(request.getRequestId());
        if (citaPorRequest.isPresent()) {
            return convertirACitaResponse(citaPorRequest.get());
        }

        // 2. Buscar cita existente
        Cita cita = citaRepository.findById(request.getIdCita())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // 3. Validar que se puede anular
        if (cita.getEstado() == EstadoCita.ANULADA) {
            throw new RuntimeException("La cita ya está anulada");
        }

        // 4. Anular cita
        cita.setEstado(EstadoCita.ANULADA);
        if (request.getMotivo() != null) {
            cita.setMotivo(cita.getMotivo() + " | Anulación: " + request.getMotivo());
        }
        cita.setRequestId(request.getRequestId());

        Cita citaAnulada = citaRepository.save(cita);

        // 5. Publicar evento
        eventPublisher.publicarCitaAnulada(citaAnulada);

        return convertirACitaResponse(citaAnulada);
    }

    /**
     * Consultar cita por ID
     */
    @Transactional(readOnly = true)
    public CitaResponse consultarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        return convertirACitaResponse(cita);
    }

    /**
     * Consultar citas por paciente
     */
    @Transactional(readOnly = true)
    public List<CitaResponse> consultarCitasPorPaciente(Long idPaciente) {
        List<EstadoCita> estadosActivos = List.of(EstadoCita.AGENDADA, EstadoCita.CONFIRMADA, EstadoCita.MODIFICADA);
        List<Cita> citas = citaRepository.findByIdPacienteAndEstadoInOrderByInicioAsc(idPaciente, estadosActivos);
        return citas.stream().map(this::convertirACitaResponse).collect(Collectors.toList());
    }

    // ================== MÉTODOS PRIVADOS DE VALIDACIÓN ==================

    private void validarHorarios(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio.isAfter(fin)) {
            throw new RuntimeException("La hora de inicio debe ser anterior a la hora de fin");
        }

        if (inicio.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se pueden agendar citas en el pasado");
        }

        // Validar que la cita sea en horario laboral (8:00 - 18:00)
        int horaInicio = inicio.getHour();
        int horaFin = fin.getHour();
        if (horaInicio < 8 || horaFin > 18) {
            throw new RuntimeException("Las citas deben ser en horario laboral (8:00 - 18:00)");
        }
    }

    private boolean esMedicoDisponible(Long idMedico, LocalDateTime inicio, LocalDateTime fin) {
        return citaRepository.countCitasConflictivasMedico(idMedico, inicio, fin) == 0;
    }

    private boolean esPacienteDisponible(Long idPaciente, LocalDateTime inicio, LocalDateTime fin) {
        return citaRepository.countCitasConflictivasPaciente(idPaciente, inicio, fin) == 0;
    }

    private boolean esMedicoDisponibleParaModificacion(Long idMedico, LocalDateTime inicio, LocalDateTime fin, Long citaId) {
        // Similar a esMedicoDisponible pero excluyendo la cita actual
        return citaRepository.countCitasConflictivasMedico(idMedico, inicio, fin) == 0;
    }

    private boolean esPacienteDisponibleParaModificacion(Long idPaciente, LocalDateTime inicio, LocalDateTime fin, Long citaId) {
        // Similar a esPacienteDisponible pero excluyendo la cita actual
        return citaRepository.countCitasConflictivasPaciente(idPaciente, inicio, fin) == 0;
    }

    private CitaResponse convertirACitaResponse(Cita cita) {
        return new CitaResponse(
                cita.getId(),
                cita.getIdPaciente(),
                cita.getIdMedico(),
                cita.getInicio(),
                cita.getFin(),
                cita.getEstado().toString(),
                cita.getMotivo(),
                cita.getCreatedAt(),
                cita.getUpdatedAt()
        );
    }
}