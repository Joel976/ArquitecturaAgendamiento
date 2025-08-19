package com.hospital.agendamiento_service.controller;

import com.hospital.agendamiento_service.dto.*;
import com.hospital.agendamiento_service.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/citas")
@CrossOrigin(origins = "*")
public class CitaController {

    @Autowired
    private CitaService citaService;

    /**
     * Agendar nueva cita
     * POST /api/v1/citas
     */
    @PostMapping
    public ResponseEntity<?> agendarCita(@Valid @RequestBody AgendarCitaRequest request) {
        try {
            CitaResponse cita = citaService.agendarCita(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(crearRespuestaExitosa("Cita agendada exitosamente", cita));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError("Error al agendar cita", e.getMessage()));
        }
    }

    /**
     * Modificar cita existente
     * PUT /api/v1/citas/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> modificarCita(@PathVariable Long id,
                                           @Valid @RequestBody ModificarCitaRequest request) {
        try {
            request.setIdCita(id);
            CitaResponse cita = citaService.modificarCita(request);
            return ResponseEntity.ok(crearRespuestaExitosa("Cita modificada exitosamente", cita));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError("Error al modificar cita", e.getMessage()));
        }
    }

    /**
     * Anular cita existente
     * DELETE /api/v1/citas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> anularCita(@PathVariable Long id,
                                        @Valid @RequestBody AnularCitaRequest request) {
        try {
            request.setIdCita(id);
            CitaResponse cita = citaService.anularCita(request);
            return ResponseEntity.ok(crearRespuestaExitosa("Cita anulada exitosamente", cita));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError("Error al anular cita", e.getMessage()));
        }
    }

    /**
     * Consultar cita por ID
     * GET /api/v1/citas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> consultarCita(@PathVariable Long id) {
        try {
            CitaResponse cita = citaService.consultarCita(id);
            return ResponseEntity.ok(crearRespuestaExitosa("Cita encontrada", cita));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError("Cita no encontrada", e.getMessage()));
        }
    }

    /**
     * Consultar citas por paciente
     * GET /api/v1/citas/paciente/{idPaciente}
     */
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<?> consultarCitasPorPaciente(@PathVariable Long idPaciente) {
        try {
            List<CitaResponse> citas = citaService.consultarCitasPorPaciente(idPaciente);
            return ResponseEntity.ok(crearRespuestaExitosa("Citas del paciente", citas));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError("Error al consultar citas del paciente", e.getMessage()));
        }
    }

    /**
     * Health check del servicio
     * GET /api/v1/citas/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "agendamiento-service");
        health.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(health);
    }

    // ================== MÉTODOS AUXILIARES ==================

    private Map<String, Object> crearRespuestaExitosa(String mensaje, Object data) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", mensaje);
        respuesta.put("data", data);
        respuesta.put("timestamp", java.time.LocalDateTime.now());
        return respuesta;
    }

    private Map<String, Object> crearRespuestaError(String mensaje, String detalle) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", false);
        respuesta.put("message", mensaje);
        respuesta.put("error", detalle);
        respuesta.put("timestamp", java.time.LocalDateTime.now());
        return respuesta;
    }
}