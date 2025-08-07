package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.TurnoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final AgenteRepository agenteRepository;
    private final PCAService pcaService;

    @Transactional(readOnly = true)
    public List<Turno> findTurnosByDateRange(LocalDateTime rangoInicio, LocalDateTime rangoFin) {
        return turnoRepository.findByFechasSolapadasFetchingAgente(rangoInicio, rangoFin);
    }

    @Transactional(readOnly = true)
    public List<Turno> findAllTurnosFetchingAgente() {
        return turnoRepository.findAllFetchingAgenteOrderByInicioTurnoAsc();
    }

    // --- MÉTODO REINCORPORADO ---
    /**
     * Busca todos los turnos para un agente específico que se solapan con un rango de fechas.
     * Este método es utilizado por la vista de detalle del planificador.
     */
    @Transactional(readOnly = true)
    public List<Turno> findTurnosByAgenteAndDateRange(Long idAgente, LocalDateTime rangoInicio, LocalDateTime rangoFin) {
         Agente agente = agenteRepository.findById(idAgente)
                       .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + idAgente));
         return turnoRepository.findByAgenteAndFechasSolapadas(agente, rangoInicio, rangoFin);
    }
    // --- FIN DEL MÉTODO REINCORPORADO ---

    @Transactional(readOnly = true)
    public Optional<Turno> findById(Long id) {
        return turnoRepository.findById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Turno save(Turno turno) {
        if (turno.getAgente() == null || turno.getAgente().getIdAgente() == null) {
            throw new IllegalArgumentException("El turno debe tener un agente asignado.");
        }

        Turno turnoOriginal = null;
        if (turno.getIdTurno() != null) {
            turnoOriginal = turnoRepository.findById(turno.getIdTurno())
                                           .orElse(null); // Usamos orElse(null) para manejar la creación
        }
        pcaService.verificarConflictoPorCambioDeTurno(turnoOriginal, turno);

        List<Turno> turnosSolapados = turnoRepository.findByAgenteAndFechasSolapadas(
                turno.getAgente(),
                turno.getInicioTurno(),
                turno.getFinTurno()
        );
        boolean haySolapamiento = turnosSolapados.stream()
                .anyMatch(t -> !Objects.equals(t.getIdTurno(), turno.getIdTurno()));
        if (haySolapamiento) {
            throw new IllegalArgumentException("El agente ya tiene un turno asignado que se solapa en ese horario.");
        }

        return turnoRepository.save(turno);
    }

    @Transactional
    public void deleteById(Long id) {
        Turno turnoABorrar = turnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Turno no encontrado con ID: " + id));

        pcaService.verificarConflictoPorCambioDeTurno(turnoABorrar, null);

        turnoRepository.deleteById(id);
    }

    public long count() {
        return turnoRepository.count();
    }
}