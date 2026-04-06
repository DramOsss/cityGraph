package citygraph.model;

import java.util.Objects;

public class Parada {
    private final String id;
    private String nombre;


    public Parada(String id, String nombre) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id inválido");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre inválido");
        this.id = id.trim().toUpperCase();
        this.nombre = nombre.trim();
    }

    public Parada(String id, String nombre, Double lat, Double lon) {
        this(id.toUpperCase(), nombre);

    }

    public String getId() { return id.toUpperCase(); }
    public String getNombre() { return nombre; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre inválido");
        this.nombre = nombre.trim();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parada parada)) return false;
        return id.equals(parada.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " (" + id + ")";
    }
}