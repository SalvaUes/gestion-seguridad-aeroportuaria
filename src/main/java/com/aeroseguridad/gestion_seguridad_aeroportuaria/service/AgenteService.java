package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PosicionSeguridadRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Importar StringUtils

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgenteService {

    private final AgenteRepository agenteRepository;
    private final PosicionSeguridadRepository posicionSeguridadRepository;

    @Transactional(readOnly = true)
    public List<Agente> findAllActiveForView(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) { // Usar StringUtils.hasText
            return agenteRepository.findActivosFetchingPosiciones();
        } else {
            return agenteRepository.searchActivosByNombreOrApellidoFetchingPosiciones(searchTerm.trim());
        }
    }

    @Transactional(readOnly = true)
    public Optional<Agente> findById(Long id) {
        // Devuelve con o sin posiciones según la necesidad. Si se necesita siempre con posiciones:
        // return agenteRepository.findByIdFetchingPosiciones(id);
        return agenteRepository.findById(id);
    }

     @Transactional(readOnly = true)
    public Optional<Agente> findByIdFetchingPosiciones(Long id) {
         return agenteRepository.findByIdFetchingPosiciones(id);
    }


    // --- NUEVO: Método para buscar por número de carnet ---
    @Transactional(readOnly = true)
    public Optional<Agente> findActivoByNumeroCarnet(String numeroCarnet) {
        if (!StringUtils.hasText(numeroCarnet)) {
            return Optional.empty();
        }
        return agenteRepository.findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(numeroCarnet.trim());
    }
    // --- FIN NUEVO MÉTODO ---

    @Transactional
    public Agente save(Agente agente) {
        if (agente.getIdAgente() == null && agente.getActivo() == null) { // Asegurar que nuevos sean activos
            agente.setActivo(true);
        } else if (agente.getIdAgente() == null) { // Si es nuevo pero activo ya está seteado
             agente.setActivo(true); // Redundante pero seguro
        }
        return agenteRepository.save(agente);
    }

    @Transactional
    public void deactivateById(Long id) {
        Agente agente = agenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + id));
        agente.setActivo(false);
        agenteRepository.save(agente);
    }

    public long countActive() { // Podrías necesitar un método en el repo: countByActivoTrue()
        return agenteRepository.findActivosFetchingPosiciones().size(); // Menos eficiente que un count query
    }

    public long countAll() {
        return agenteRepository.count();
    }

    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findAllPosiciones() {
        // Devolver solo posiciones activas para asignar a agentes?
        // return posicionSeguridadRepository.findByActivoTrueOrderByNombrePosicionAsc();
        return posicionSeguridadRepository.findAll(); // O todas, y se filtra en UI si es necesario
    }
}