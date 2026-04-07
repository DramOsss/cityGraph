package citygraph.model;

import java.util.Objects;


/**
 * Clase que representa un nodo (vértice) dentro del grafo de transporte urbano.
 * * Define los atributos fundamentales de una parada, como su identificador único
 * y su nombre descriptivo. La clase garantiza la integridad de los datos
 * mediante validaciones en el constructor y métodos de acceso, asegurando
 * que cada nodo sea identificable de forma unívoca en la red.
 */
public class Parada {
    private final String id;
    private String nombre;


    /**
     * Constructor principal para crear una parada con validaciones de seguridad.
     * * @param id El identificador único de la parada. No puede ser nulo ni estar vacío.
     * @param nombre El nombre de la parada. No puede ser nulo ni estar vacío.
     * @throws IllegalArgumentException Si el ID o el nombre no cumplen con los requisitos de formato.
     */
    public Parada(String id, String nombre) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id inválido");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre inválido");
        this.id = id.trim().toUpperCase();
        this.nombre = nombre.trim();
    }

    /**
     * Constructor extendido para compatibilidad con coordenadas geográficas.
     * * @param id Identificador único de la parada.
     * @param nombre Nombre descriptivo de la parada.
     * @param lat Valor de latitud para geolocalización.
     * @param lon Valor de longitud para geolocalización.
     */
    public Parada(String id, String nombre, Double lat, Double lon) {
        this(id.toUpperCase(), nombre);

    }

    public String getId() { return id.toUpperCase(); }
    public String getNombre() { return nombre; }

    /**
     * Actualiza el nombre de la parada después de validar que el nuevo valor sea válido.
     * * @param nombre El nuevo nombre descriptivo a asignar.
     * @throws IllegalArgumentException Si el nombre proporcionado es nulo o está en blanco.
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre inválido");
        this.nombre = nombre.trim();
    }


    /**
     * Compara esta parada con otro objeto basándose estrictamente en su identificador único.
     * * @param o Objeto a comparar.
     * @return true si ambos objetos representan la misma parada por ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parada parada)) return false;
        return id.equals(parada.id);
    }

    /**
     * Genera un código hash basado en el identificador de la parada para su uso
     * eficiente en estructuras como HashMap o HashSet.
     * * @return Valor hash del objeto.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Devuelve una representación legible de la parada.
     * * @return Una cadena con el formato "Nombre (ID)".
     */
    @Override
    public String toString() {
        return nombre + " (" + id + ")";
    }
}