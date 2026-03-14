package citygraph.model;

public class Ruta {
    private final String origenId;
    private final String destinoId;

    private double tiempoMin;
    private double distanciaKm;
    private double costo;
    private int transbordos;

    public Ruta(String origenId, String destinoId,
                double tiempoMin, double distanciaKm, double costo, int transbordos) {

        if (origenId == null || origenId.isBlank()) throw new IllegalArgumentException("origenId invalido");
        if (destinoId == null || destinoId.isBlank()) throw new IllegalArgumentException("destinoId invalido");
        if (tiempoMin < 0) throw new IllegalArgumentException("tiempoMin no puede ser negativo");
        if (distanciaKm < 0) throw new IllegalArgumentException("distanciaKm no puede ser negativa");
        if (costo < 0) throw new IllegalArgumentException("costo no puede ser negativo");
        if (transbordos < 0) throw new IllegalArgumentException("transbordos no puede ser negativo");

        this.origenId = origenId.trim().toUpperCase();
        this.destinoId = destinoId.trim().toUpperCase();
        this.tiempoMin = tiempoMin;
        this.distanciaKm = distanciaKm;
        this.costo = costo;
        this.transbordos = transbordos;
    }

    public String getOrigenId() { return origenId.toUpperCase(); }
    public String getDestinoId() { return destinoId.toUpperCase(); }

    public double getTiempoMin() { return tiempoMin; }
    public double getDistanciaKm() { return distanciaKm; }
    public double getCosto() { return costo; }
    public int getTransbordos() { return transbordos; }

    public void setTiempoMin(double tiempoMin) {
        if (tiempoMin < 0) throw new IllegalArgumentException("tiempoMin no puede ser negativo");
        this.tiempoMin = tiempoMin;
    }

    public void setDistanciaKm(double distanciaKm) {
        if (distanciaKm < 0) throw new IllegalArgumentException("distanciaKm no puede ser negativa");
        this.distanciaKm = distanciaKm;
    }

    public void setCosto(double costo) {
        if (costo < 0) throw new IllegalArgumentException("costo no puede ser negativo");
        this.costo = costo;
    }

    public void setTransbordos(int transbordos) {
        if (transbordos < 0) throw new IllegalArgumentException("transbordos no puede ser negativo");
        this.transbordos = transbordos;
    }

    @Override
    public String toString() {
        return origenId + " -> " + destinoId +
                " [t=" + tiempoMin + "min, d=" + distanciaKm + "km, $" + costo + ", tr=" + transbordos + "]";
    }
}