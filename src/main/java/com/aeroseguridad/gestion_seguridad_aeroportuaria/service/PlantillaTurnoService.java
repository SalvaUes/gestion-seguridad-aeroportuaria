package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PlantillaTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.ReglaDeTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PlantillaTurnoRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.TurnoRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PlantillaTurnoService {

    private static final Logger logger = LoggerFactory.getLogger(PlantillaTurnoService.class);

    private final PlantillaTurnoRepository plantillaTurnoRepository;
    private final AgenteRepository agenteRepository;
    private final TurnoRepository turnoRepository;
    private final PermisoService permisoService;

    // --- MÉTODOS CRUD PARA GESTIONAR PLANTILLAS ---

    public PlantillaTurno savePlantilla(PlantillaTurno plantilla) {
        return plantillaTurnoRepository.save(plantilla);
    }

    public void deletePlantilla(Long plantillaId) {
        plantillaTurnoRepository.deleteById(plantillaId);
    }
    
    @Transactional(readOnly = true)
    public PlantillaTurno findPlantillaById(Long plantillaId) {
        return plantillaTurnoRepository.findById(plantillaId)
                .orElseThrow(() -> new EntityNotFoundException("Plantilla no encontrada con ID: " + plantillaId));
    }

    @Transactional(readOnly = true)
    public List<PlantillaTurno> findAllPlantillasByAgente(Long agenteId) {
        // Esta consulta puede ser optimizada si es necesario
        return plantillaTurnoRepository.findAll().stream()
                .filter(p -> p.getAgente().getIdAgente().equals(agenteId))
                .collect(Collectors.toList());
    }
    
    // --- LÓGICA DE NEGOCIO AVANZADA ---
    
    public int generarTurnosDesdePlantilla(Long agenteId, int anio, int mes) {
        Agente agente = agenteRepository.findById(agenteId)
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + agenteId));

        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate finMes = yearMonth.atEndOfMonth();

        logger.info("Borrando turnos PROGRAMADOS existentes para {} en {}/{}", agente.getNombreCompleto(), mes, anio);
        turnoRepository.deleteByAgenteAndEstadoTurnoAndInicioTurnoBetween(
                agente, EstadoTurno.PROGRAMADO, inicioMes.atStartOfDay(), finMes.atTime(23, 59, 59)
        );

        List<PlantillaTurno> plantillasActivas = plantillaTurnoRepository.findActiveTemplatesForAgentInPeriod(agenteId, inicioMes, finMes);
        if (plantillasActivas.isEmpty()) {
            logger.info("No se encontraron plantillas activas para el agente {} en {}/{}", agente.getNombreCompleto(), mes, anio);
            return 0;
        }
        
        // --- INICIO DE LA LÓGICA CORREGIDA ---
        
        // 1. Obtenemos una lista plana de todas las reglas de todas las plantillas activas.
        List<ReglaDeTurno> todasLasReglas = plantillasActivas.stream()
                .flatMap(plantilla -> plantilla.getReglas().stream())
                .collect(Collectors.toList());

        List<Turno> nuevosTurnos = new ArrayList<>();

        // 2. Iteramos por cada día del mes.
        for (LocalDate diaActual = inicioMes; !diaActual.isAfter(finMes); diaActual = diaActual.plusDays(1)) {
            DayOfWeek diaDeLaSemanaActual = diaActual.getDayOfWeek();

            // Verificamos si el agente tiene un permiso aprobado para este día ANTES de procesar las reglas.
            boolean tienePermiso = permisoService.hasApprovedLeave(agente, diaActual.atStartOfDay(), diaActual.atTime(23, 59, 59));
            if (tienePermiso) {
                logger.info("Omitiendo generación de turno para el agente {} el día {} por permiso aprobado.", agente.getNombreCompleto(), diaActual);
                continue; // Saltar al siguiente día
            }

            // 3. Por cada día, iteramos sobre todas las reglas para ver si alguna aplica.
            for (ReglaDeTurno regla : todasLasReglas) {
                // 4. La comprobación clave: ¿el conjunto de días de esta regla contiene el día actual?
                if (regla.getDiasDeLaSemana().contains(diaDeLaSemanaActual)) {
                    Turno nuevoTurno = new Turno();
                    nuevoTurno.setAgente(agente);
                    nuevoTurno.setInicioTurno(diaActual.atTime(regla.getHoraInicio()));
                    nuevoTurno.setFinTurno(diaActual.atTime(regla.getHoraFin()));
                    nuevoTurno.setTipoTurno(regla.getTipoTurno());
                    nuevoTurno.setEstadoTurno(EstadoTurno.PROGRAMADO);
                    nuevosTurnos.add(nuevoTurno);
                }
            }
        }
        // --- FIN DE LA LÓGICA CORREGIDA ---

        if (!nuevosTurnos.isEmpty()) {
            turnoRepository.saveAll(nuevosTurnos);
            logger.info("Se generaron y guardaron {} nuevos turnos para el agente {} en {}/{}", nuevosTurnos.size(), agente.getNombreCompleto(), mes, anio);
        }
        return nuevosTurnos.size();
    }
    
    public PlantillaTurno copiarPlantilla(Long plantillaOrigenId, Long agenteDestinoId) {
        PlantillaTurno plantillaOrigen = findPlantillaById(plantillaOrigenId);
        Agente agenteDestino = agenteRepository.findById(agenteDestinoId)
                .orElseThrow(() -> new EntityNotFoundException("Agente destino no encontrado con ID: " + agenteDestinoId));

        PlantillaTurno nuevaPlantilla = new PlantillaTurno();
        nuevaPlantilla.setAgente(agenteDestino);
        nuevaPlantilla.setNombrePlantilla(plantillaOrigen.getNombrePlantilla() + " (Copia)");
        nuevaPlantilla.setFechaInicioVigencia(plantillaOrigen.getFechaInicioVigencia());
        nuevaPlantilla.setFechaFinVigencia(plantillaOrigen.getFechaFinVigencia());

        for (ReglaDeTurno reglaOrigen : plantillaOrigen.getReglas()) {
            nuevaPlantilla.addRegla(new ReglaDeTurno(reglaOrigen));
        }
        
        return savePlantilla(nuevaPlantilla);
    }
}