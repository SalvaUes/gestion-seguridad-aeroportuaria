package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.ReglaDeTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReglaDeTurnoRepository extends JpaRepository<ReglaDeTurno, Long> {
    // De momento no necesita métodos personalizados. JpaRepository es suficiente.
}