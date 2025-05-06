package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity; 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


// Importaciones de Lombok para simplificar el codigo
import lombok.AllArgsConstructor; 
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// --- Anotaciones a Nivel de Clase ---

@Entity // 1. Marca esta clase como una Entidad JPA (un objeto que representa datos de la DB)
@Table(name = "aerolineas") // 2. Especifica el nombre de la tabla en la base de datos



// --- Anotaciones de Lombok  ---

@Getter // 3. Genera automáticamente todos los métodos getter (ej. getNombre())
@Setter // 4. Genera automáticamente todos los métodos setter (ej. setNombre(String nombre))
@NoArgsConstructor // 5. Genera un constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // 6. Genera un constructor con todos los campos como argumentos
@ToString // 7. Genera un método toString() útil para mostrar el objeto en logs
public class Aerolinea {



    // --- Atributos (Campos/Columnas) ---

    @Id // 8. Marca este campo como la Clave Primaria (Primary Key) de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 9. Configura la generación automática del ID
    //    strategy = GenerationType.IDENTITY: Usa la estrategia de auto-incremento de la DB (común en PostgreSQL)
    private Long idAerolinea; // 10. El campo para el ID ( Long que mapea bien a BIGINT/BIGSERIAL)

    @Column(name = "nombre", nullable = false, length = 100) // 11. Mapea este campo a una columna
    //    name = "nombre": Nombre de la columna en la DB.
    //    nullable = false: La columna NO puede ser nula (es obligatoria).
    //    length = 100: Longitud máxima de la cadena de texto en la DB.
    private String nombre; // 12. El campo para el nombre de la aerolínea

    @Column(name = "codigo_iata", length = 3, unique = true) // 13. Mapea a la columna 'codigo_iata'
    //    length = 3: Longitud del código (ej. "AV", "IB", a veces 3 caracteres).
    //    unique = true: El valor en esta columna debe ser único en toda la tabla.
    //    nullable = true (por defecto): Puede ser nulo si no se especifica.
    private String codigoIata; // 14. El campo para el código IATA

    @Column(name = "activo", nullable = false) // 15. Columna para indicar si el registro está activo
    //    nullable = false: Debe tener un valor (true o false).
    private Boolean activo = true; // 16. Campo booleano. Lo inicializamos a 'true' por defecto.



    // --- Constructores, Getters, Setters, toString() ---

    
    // public Aerolinea() {}
    // public Aerolinea(String nombre, String codigoIata, Boolean activo) { ... }
    // public Long getIdAerolinea() { return idAerolinea; }
    // public void setIdAerolinea(Long idAerolinea) { this.idAerolinea = idAerolinea; }
    // ... y así para todos los campos ...
    // public String toString() { ... }

    // Esto se evito porque Lombok genera automáticamente estos métodos****************
}