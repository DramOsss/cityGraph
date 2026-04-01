package citygraph.model;

public class Ruta {

    private final String origenId;
    private final String destinoId;

    private double tiempoMin;
    private double distanciaKm;
    private double costo;
    private TipoTransporte tipoTransporte;

    public Ruta(String origenId, String destinoId,
                double tiempoMin, double distanciaKm, double costo,
                TipoTransporte tipoTransporte) {

        if (origenId == null || origenId.isBlank()) throw new IllegalArgumentException("origenId invalido");
        if (destinoId == null || destinoId.isBlank()) throw new IllegalArgumentException("destinoId invalido");
        if (tiempoMin < 0) throw new IllegalArgumentException("tiempoMin no puede ser negativo");
        if (distanciaKm < 0) throw new IllegalArgumentException("distanciaKm no puede ser negativa");
        if (tipoTransporte == null) throw new IllegalArgumentException("tipoTransporte no puede ser null");

        this.origenId = origenId.trim().toUpperCase();
        this.destinoId = destinoId.trim().toUpperCase();
        this.tiempoMin = tiempoMin;
        this.distanciaKm = distanciaKm;
        this.costo = costo;
        this.tipoTransporte = tipoTransporte;
    }

    public String getOrigenId() { return origenId; }
    public String getDestinoId() { return destinoId; }

    public double getTiempoMin() { return tiempoMin; }
    public double getDistanciaKm() { return distanciaKm; }
    public double getCosto() { return costo; }
    public TipoTransporte getTipoTransporte() { return tipoTransporte; }

    public void setTiempoMin(double tiempoMin) {
        if (tiempoMin < 0) throw new IllegalArgumentException("tiempoMin no puede ser negativo");
        this.tiempoMin = tiempoMin;
    }

    public void setDistanciaKm(double distanciaKm) {
        if (distanciaKm < 0) throw new IllegalArgumentException("distanciaKm no puede ser negativa");
        this.distanciaKm = distanciaKm;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public void setTipoTransporte(TipoTransporte tipoTransporte) {
        if (tipoTransporte == null) throw new IllegalArgumentException("tipoTransporte no puede ser null");
        this.tipoTransporte = tipoTransporte;
    }

    @Override
    public String toString() {
        return origenId + " -> " + destinoId +
                " [t=" + tiempoMin + "min, d=" + distanciaKm + "km, $" + costo +
                ", tipo=" + tipoTransporte + "]";
    }
}