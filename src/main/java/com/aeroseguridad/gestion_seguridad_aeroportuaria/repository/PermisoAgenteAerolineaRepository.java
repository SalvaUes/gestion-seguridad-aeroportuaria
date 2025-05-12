package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PermisoAgenteAerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoAgenteAerolineaRepository extends JpaRepository<PermisoAgenteAerolinea, Long> {

    List<PermisoAgenteAerolinea> findByAgente(Agente agente);
    List<PermisoAgenteAerolinea> findByAerolinea(Aerolinea aerolinea);
    Optional<PermisoAgenteAerolinea> findByAgenteAndAerolinea(Agente agente, Aerolinea aerolinea);

    // Para vistas que muestren detalles con JOIN FETCH para evitar N+1
    @Query("SELECT paa FROM PermisoAgenteAerolinea paa JOIN FETCH paa.agente JOIN FETCH paa.aerolinea")
    List<PermisoAgenteAerolinea> findAllFetchingAll();

    @Query("SELECT paa FROM PermisoAgenteAerolinea paa JOIN FETCH paa.agente JOIN FETCH paa.aerolinea WHERE paa.idPermisoAa = :id")
    Optional<PermisoAgenteAerolinea> findByIdFetchingAll(@Param("id") Long id);

    @Query("SELECT paa FROM PermisoAgenteAerolinea paa JOIN FETCH paa.agente JOIN FETCH paa.aerolinea WHERE paa.agente = :agente")
    List<PermisoAgenteAerolinea> findByAgenteFetchingAll(@Param("agente") Agente agente);

    @Query("SELECT paa FROM PermisoAgenteAerolinea paa JOIN FETCH paa.agente JOIN FETCH paa.aerolinea WHERE paa.aerolinea = :aerolinea")
    List<PermisoAgenteAerolinea> findByAerolineaFetchingAll(@Param("aerolinea") Aerolinea aerolinea);
}