package citygraph.service;

import citygraph.graph.GrafoTransporte;
import citygraph.model.Parada;
import citygraph.model.Ruta;
import citygraph.util.DBConnection;

import java.sql.*;
import java.util.Objects;

public class CityGraphService {

    private final GrafoTransporte grafo = new GrafoTransporte();

    public GrafoTransporte getGrafo() {
        return grafo;
    }


    public void cargarDesdeBD() {
        try (Connection cn = DBConnection.connect()) {


            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT id, nombre, lat, lon FROM paradas ORDER BY id"
            );
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String id = rs.getString("id");
                    String nombre = rs.getString("nombre");
                    Double lat = (Double) rs.getObject("lat");
                    Double lon = (Double) rs.getObject("lon");
                    grafo.agregarParada(new Parada(id, nombre, lat, lon));
                }
            }


            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT origen_id, destino_id, tiempo_min, distancia_km, costo, transbordos FROM rutas"
            );
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    grafo.agregarRuta(new Ruta(
                            rs.getString("origen_id"),
                            rs.getString("destino_id"),
                            rs.getDouble("tiempo_min"),
                            rs.getDouble("distancia_km"),
                            rs.getDouble("costo"),
                            rs.getInt("transbordos")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error cargando datos desde BD: " + e.getMessage(), e);
        }
    }


    // Paradas (CRUD)

    public void agregarParada(Parada p) {
        Objects.requireNonNull(p, "Parada no puede ser null");

        // Memoria
        grafo.agregarParada(p);

        // BD
        try (Connection cn = DBConnection.connect();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO paradas(id, nombre, lat, lon) VALUES (?,?,?,?)"
             )) {
            ps.setString(1, p.getId());
            ps.setString(2, p.getNombre());
            ps.setObject(3, p.getLat());
            ps.setObject(4, p.getLon());
            ps.executeUpdate();
        } catch (SQLException e) {
            try { grafo.eliminarParada(p.getId()); } catch (Exception ignored) {}
            throw new RuntimeException("Error insertando parada en BD: " + e.getMessage(), e);
        }
    }

    public void modificarParada(String id, String nuevoNombre, Double lat, Double lon) {

        grafo.modificarParada(id, nuevoNombre, lat, lon);


        try (Connection cn = DBConnection.connect();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE paradas SET nombre = COALESCE(?, nombre), lat = ?, lon = ? WHERE id = ?"
             )) {
            ps.setString(1, (nuevoNombre == null || nuevoNombre.isBlank()) ? null : nuevoNombre);
            ps.setObject(2, lat);
            ps.setObject(3, lon);
            ps.setString(4, id);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No existe la parada en BD: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando parada en BD: " + e.getMessage(), e);
        }
    }

    public void eliminarParada(String id) {

        grafo.eliminarParada(id);


        try (Connection cn = DBConnection.connect();
             PreparedStatement ps = cn.prepareStatement(
                     "DELETE FROM paradas WHERE id = ?"
             )) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No existe la parada en BD: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando parada en BD: " + e.getMessage(), e);
        }
    }


    // Rutas (CRUD)

    public void agregarRuta(Ruta r) {
        Objects.requireNonNull(r, "Ruta no puede ser null");


        grafo.agregarRuta(r);


        try (Connection cn = DBConnection.connect();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO rutas(origen_id, destino_id, tiempo_min, distancia_km, costo, transbordos) " +
                             "VALUES (?,?,?,?,?,?)"
             )) {
            ps.setString(1, r.getOrigenId());
            ps.setString(2, r.getDestinoId());
            ps.setDouble(3, r.getTiempoMin());
            ps.setDouble(4, r.getDistanciaKm());
            ps.setDouble(5, r.getCosto());
            ps.setInt(6, r.getTransbordos());
            ps.executeUpdate();
        } catch (SQLException e) {
            try { grafo.eliminarRuta(r.getOrigenId(), r.getDestinoId()); } catch (Exception ignored) {}
            throw new RuntimeException("Error insertando ruta en BD: " + e.getMessage(), e);
        }
    }

    public void modificarRuta(String origenId, String destinoId,
                              Double tiempoMin, Double distanciaKm, Double costo, Integer transbordos) {

        grafo.modificarRuta(origenId, destinoId, tiempoMin, distanciaKm, costo, transbordos);


        try (Connection cn = DBConnection.connect();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE rutas SET " +
                             "tiempo_min = COALESCE(?, tiempo_min), " +
                             "distancia_km = COALESCE(?, distancia_km), " +
                             "costo = COALESCE(?, costo), " +
                             "transbordos = COALESCE(?, transbordos) " +
                             "WHERE origen_id = ? AND destino_id = ?"
             )) {
            ps.setObject(1, tiempoMin);
            ps.setObject(2, distanciaKm);
            ps.setObject(3, costo);
            ps.setObject(4, transbordos);
            ps.setString(5, origenId);
            ps.setString(6, destinoId);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No existe la ruta en BD: " + origenId + " -> " + destinoId);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando ruta en BD: " + e.getMessage(), e);
        }
    }

    public void eliminarRuta(String origenId, String destinoId) {

        grafo.eliminarRuta(origenId, destinoId);


        try (Connection cn = DBConnection.connect();
             PreparedStatement ps = cn.prepareStatement(
                     "DELETE FROM rutas WHERE origen_id = ? AND destino_id = ?"
             )) {
            ps.setString(1, origenId);
            ps.setString(2, destinoId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No existe la ruta en BD: " + origenId + " -> " + destinoId);
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando ruta en BD: " + e.getMessage(), e);
        }
    }
}