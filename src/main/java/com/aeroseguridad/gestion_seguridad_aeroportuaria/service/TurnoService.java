package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.TurnoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation; // <-- Importar Propagation
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects; // <-- Importar Objects para Objects.equals
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final AgenteRepository agenteRepository; // Necesario si buscamos agente por ID

    @Transactional(readOnly = true)
    public List<Turno> findTurnosByDateRange(LocalDateTime rangoInicio, LocalDateTime rangoFin) {
        // Esta consulta debe permanecer read-only
        return turnoRepository.findByFechasSolapadasFetchingAgente(rangoInicio, rangoFin);
    }

     @Transactional(readOnly = true)
    public List<Turno> findTurnosByAgenteAndDateRange(Long idAgente, LocalDateTime rangoInicio, LocalDateTime rangoFin) {
         Agente agente = agenteRepository.findById(idAgente)
                 .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + idAgente));
         // Esta consulta también es read-only implícitamente por el método del repo
         return turnoRepository.findByAgenteAndFechasSolapadas(agente, rangoInicio, rangoFin);
    }


    @Transactional(readOnly = true)
    public Optional<Turno> findById(Long id) {
        return turnoRepository.findById(id);
    }

    // --- MODIFICADO: Añadido propagation = Propagation.REQUIRES_NEW ---
    @Transactional(propagation = Propagation.REQUIRES_NEW) // <-- Forzar nueva transacción para save
    public Turno save(Turno turno) {
        // --- Validación de Negocio (No solapar turnos del mismo agente) ---
        if (turno.getAgente() == null || turno.getAgente().getIdAgente() == null) {
            throw new IllegalArgumentException("El turno debe tener un agente asignado.");
        }
        // Obtener agente (podría ser necesario recargarlo si solo viene el ID)
        Agente agente = turno.getAgente();

        // Buscar turnos existentes que se solapen para ESE agente
        List<Turno> turnosSolapados = turnoRepository.findByAgenteAndFechasSolapadas(
                agente,
                turno.getInicioTurno(),
                turno.getFinTurno()
        );
        // Filtrar para excluir el propio turno si se está editando
        final Long idTurnoActual = turno.getIdTurno(); // ID del turno que estamos guardando
        boolean haySolapamiento = turnosSolapados.stream()
                // Comparar IDs de forma segura por si alguno es null (aunque no debería serlo en la BD)
                .anyMatch(t -> !Objects.equals(t.getIdTurno(), idTurnoActual));

        // Si hay solapamiento, lanzar error
        if (haySolapamiento) {
             throw new IllegalArgumentException("El agente ya tiene un turno asignado que se solapa en ese horario.");
        }
        // --- Fin Validación ---

        // Validación @AssertTrue (fin > inicio) se comprobará por JPA al hacer commit

        // Guardar y devolver el objeto (potencialmente con ID generado)
        return turnoRepository.save(turno);
    }
    // --- FIN MODIFICADO ---

    @Transactional // Propagación por defecto es REQUIRED
    public void deleteById(Long id) {
        if (!turnoRepository.existsById(id)) {
             throw new EntityNotFoundException("Turno no encontrado con ID: " + id);
        }
        turnoRepository.deleteById(id);
    }

    // No necesita transacción usualmente
    public long count() {
        return turnoRepository.count();
    }
}