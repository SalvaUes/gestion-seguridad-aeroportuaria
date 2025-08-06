package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PlantillaVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.NecesidadVueloRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PlantillaVueloRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.VueloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlantillaVueloService {

    private final PlantillaVueloRepository plantillaVueloRepository;
    private final VueloRepository vueloRepository;
    private final NecesidadVueloRepository necesidadVueloRepository;

    @Transactional(readOnly = true)
    public List<PlantillaVuelo> findAll() {
        return plantillaVueloRepository.findAllWithAerolinea();
    }
    
    @Transactional
    public PlantillaVuelo save(PlantillaVuelo plantilla) {
        return plantillaVueloRepository.save(plantilla);
    }

    @Transactional
    public int generarVuelosDesdePlantilla(Long idPlantilla, LocalDate fechaInicio, LocalDate fechaFin) {
        PlantillaVuelo plantilla = plantillaVueloRepository.findById(idPlantilla)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con ID: " + idPlantilla));

        // --- CAMBIO: Lógica de comprobación de existencia mejorada ---
        // 1. Obtener las fechas de TODOS los vuelos existentes para este número de vuelo en el rango.
        List<Vuelo> vuelosExistentes = vueloRepository.findByNumeroVueloAndFechaHoraSalidaBetween(
            plantilla.getNumeroVuelo(),
            fechaInicio.atStartOfDay(),
            fechaFin.plusDays(1).atStartOfDay() // Aseguramos que cubra hasta el final del día de fechaFin
        );
        
        // 2. Extraer solo las fechas (sin la hora) a un Set para una búsqueda rápida y eficiente.
        Set<LocalDate> fechasYaCreadas = vuelosExistentes.stream()
                                            .map(vuelo -> vuelo.getFechaHoraSalida().toLocalDate())
                                            .collect(Collectors.toSet());

        List<Vuelo> nuevosVuelos = new ArrayList<>();
        LocalDate fechaActual = fechaInicio;

        while (!fechaActual.isAfter(fechaFin)) {
            // 3. Comprobar si el día de la semana coincide Y si la fecha no está ya en nuestro Set.
            if (plantilla.getDiasOperacion().contains(fechaActual.getDayOfWeek()) && !fechasYaCreadas.contains(fechaActual)) {
                
                Vuelo nuevoVuelo = new Vuelo();
                nuevoVuelo.setNumeroVuelo(plantilla.getNumeroVuelo());
                nuevoVuelo.setAerolinea(plantilla.getAerolinea());
                nuevoVuelo.setOrigen(plantilla.getOrigen());
                nuevoVuelo.setDestino(plantilla.getDestino());
                nuevoVuelo.setEstado(EstadoVuelo.PROGRAMADO);
                nuevoVuelo.setTipoOperacion(plantilla.getTipoOperacion());
                nuevoVuelo.setFechaHoraSalida(fechaActual.atTime(plantilla.getHoraSalida()));
                nuevoVuelo.setFechaHoraLlegada(fechaActual.atTime(plantilla.getHoraLlegada()));
                
                if (plantilla.getHoraFinOperacionSeguridad() != null) {
                    nuevoVuelo.setFinOperacionSeguridad(fechaActual.atTime(plantilla.getHoraFinOperacionSeguridad()));
                }

                Vuelo vueloGuardado = vueloRepository.save(nuevoVuelo);

                if (plantilla.getNecesidadesEstandar() != null && !plantilla.getNecesidadesEstandar().isEmpty()) {
                    Set<NecesidadVuelo> nuevasNecesidades = new HashSet<>();
                    for (NecesidadVuelo necesidadEstandar : plantilla.getNecesidadesEstandar()) {
                        NecesidadVuelo necesidadClonada = new NecesidadVuelo();
                        necesidadClonada.setPosicion(necesidadEstandar.getPosicion());
                        necesidadClonada.setCantidadAgentes(necesidadEstandar.getCantidadAgentes());
                        necesidadClonada.setInicioCobertura(fechaActual.atTime(necesidadEstandar.getInicioCobertura().toLocalTime()));
                        necesidadClonada.setFinCobertura(fechaActual.atTime(necesidadEstandar.getFinCobertura().toLocalTime()));
                        necesidadClonada.setVuelo(vueloGuardado);
                        nuevasNecesidades.add(necesidadClonada);
                    }
                    necesidadVueloRepository.saveAll(nuevasNecesidades);
                }
                
                nuevosVuelos.add(vueloGuardado);
            }
            fechaActual = fechaActual.plusDays(1);
        }

        return nuevosVuelos.size();
    }
}