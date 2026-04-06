package citygraph.ui.Controllers;

import citygraph.core.AppState;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.Parada;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;
import citygraph.service.CityGraphService;
import citygraph.ui.Navigator;
import citygraph.ui.StateAware;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalcularController implements StateAware {

    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;
    @FXML private ComboBox<CriterioOptimizacion> cmbCriterio;
    @FXML private TextArea txtResultado;
    @FXML private Pane mapPane;

    private Navigator nav;
    private CityGraphService service;

    private static final double MAP_WIDTH = 700;
    private static final double MAP_HEIGHT = 420;
    private static final double PADDING = 40;

    @Override
    public void setState(AppState state, Navigator nav) {
        this.nav = nav;
        this.service = state.getService();

        cmbCriterio.setItems(FXCollections.observableArrayList(CriterioOptimizacion.values()));
        cmbCriterio.getSelectionModel().select(CriterioOptimizacion.TIEMPO);

        refrescarParadas();
        dibujarMapaBase();

        txtResultado.setText("Selecciona origen, destino y criterio, luego presiona Calcular.");
    }

    @FXML
    private void onVolver() {
        nav.goTo("/citygraph/home-view.fxml", "CityGraph - Inicio");
    }

    @FXML
    private void onRefrescar() {
        refrescarParadas();
        dibujarMapaBase();
        txtResultado.setText("Paradas y mapa refrescados.");
    }

    @FXML
    private void onCalcular() {
        Parada o = cmbOrigen.getValue();
        Parada d = cmbDestino.getValue();
        CriterioOptimizacion criterio = cmbCriterio.getValue();

        if (o == null || d == null) {
            txtResultado.setText("Selecciona origen y destino.");
            return;
        }

        if (o.getId().equals(d.getId())) {
            txtResultado.setText("Origen y destino no pueden ser iguales.");
            return;
        }

        if (criterio == null) {
            txtResultado.setText("Selecciona un criterio.");
            return;
        }

        try {
            ResultadoRuta res = service.calcularRuta(o.getId(), d.getId(), criterio);

            if (res == null || !res.existeRuta()) {
                txtResultado.setText(
                        "No se encontró un camino entre las paradas seleccionadas.\n" +
                                "Verifique que exista una conexión entre ellas."
                );
                dibujarMapaBase();
                resaltarOrigenDestino(o, d);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Ruta encontrada\n\n");
            sb.append("Camino: ").append(String.join(" -> ", res.getCamino())).append("\n\n");
            sb.append("Peso óptimo (criterio): ").append(res.getPesoOptimo()).append("\n\n");
            sb.append("Totales:\n");
            sb.append("- Tiempo (min): ").append(res.getTiempoTotal()).append("\n");
            sb.append("- Distancia (km): ").append(res.getDistanciaTotal()).append("\n");
            sb.append("- Costo: ").append(res.getCostoTotal()).append("\n");
            sb.append("- Transbordos: ").append(res.getTransbordosTotal()).append("\n");

            txtResultado.setText(sb.toString());

            dibujarMapaBase();
            dibujarMejorRuta(res.getCamino());
            resaltarOrigenDestino(o, d);

        } catch (Exception e) {
            txtResultado.setText("Error: " + e.getMessage());
            dibujarMapaBase();
        }
    }

    private void refrescarParadas() {
        List<Parada> paradas = service.listarParadasOrdenadas();

        cmbOrigen.setItems(FXCollections.observableArrayList(paradas));
        cmbDestino.setItems(FXCollections.observableArrayList(paradas));

        if (!paradas.isEmpty()) {
            cmbOrigen.getSelectionModel().select(0);
            cmbDestino.getSelectionModel().select(Math.min(1, paradas.size() - 1));
        }
    }

    // =========================
    // MAPA
    // =========================

    private void dibujarMapaBase() {
        mapPane.getChildren().clear();

        List<Parada> paradas = service.listarParadasOrdenadas();
        List<Ruta> rutas = service.listarRutasOrdenadas();

        if (paradas.isEmpty()) return;

        Map<String, Punto> puntos = calcularPuntos(paradas);

        // Rutas base en gris, separadas por dirección
        for (Ruta ruta : rutas) {
            Punto p1 = puntos.get(ruta.getOrigenId());
            Punto p2 = puntos.get(ruta.getDestinoId());

            if (p1 == null || p2 == null) continue;

            Line linea = crearLineaDirigida(ruta, p1, p2, Color.LIGHTGRAY, 2.0);
            mapPane.getChildren().add(linea);
            agregarFlecha(linea.getStartX(), linea.getStartY(), linea.getEndX(), linea.getEndY(), Color.LIGHTGRAY, 2.0);
        }

        // Nodos
        for (Parada parada : paradas) {
            Punto p = puntos.get(parada.getId());
            if (p == null) continue;

            Circle circle = new Circle(p.x, p.y, 7, Color.DARKSLATEBLUE);
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(1.5);

            Text label = new Text(p.x + 10, p.y - 10, parada.getId());
            label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

            mapPane.getChildren().addAll(circle, label);
        }
    }

    private void dibujarMejorRuta(List<String> camino) {
        if (camino == null || camino.size() < 2) return;

        List<Parada> paradas = service.listarParadasOrdenadas();
        Map<String, Punto> puntos = calcularPuntos(paradas);

        for (int i = 0; i < camino.size() - 1; i++) {
            String origen = camino.get(i);
            String destino = camino.get(i + 1);

            Punto p1 = puntos.get(origen);
            Punto p2 = puntos.get(destino);

            if (p1 == null || p2 == null) continue;

            Ruta ruta = buscarRutaEntre(origen, destino);
            if (ruta == null) continue;

            Line linea = crearLineaDirigida(ruta, p1, p2, Color.GREEN, 4.0);
            mapPane.getChildren().add(linea);
            agregarFlecha(linea.getStartX(), linea.getStartY(), linea.getEndX(), linea.getEndY(), Color.GREEN, 3.0);
        }

        for (String id : camino) {
            Punto p = puntos.get(id);
            if (p == null) continue;

            Circle circle = new Circle(p.x, p.y, 8, Color.FORESTGREEN);
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(2);

            Text label = new Text(p.x + 10, p.y - 10, id);
            label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

            mapPane.getChildren().addAll(circle, label);
        }
    }

    private void resaltarOrigenDestino(Parada origen, Parada destino) {
        if (origen == null || destino == null) return;

        List<Parada> paradas = service.listarParadasOrdenadas();
        Map<String, Punto> puntos = calcularPuntos(paradas);

        Punto po = puntos.get(origen.getId());
        Punto pd = puntos.get(destino.getId());

        if (po != null) {
            Circle cOrigen = new Circle(po.x, po.y, 10, Color.DODGERBLUE);
            cOrigen.setStroke(Color.WHITE);
            cOrigen.setStrokeWidth(2);
            Text tOrigen = new Text(po.x + 12, po.y + 16, "Origen");
            tOrigen.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            mapPane.getChildren().addAll(cOrigen, tOrigen);
        }

        if (pd != null) {
            Circle cDestino = new Circle(pd.x, pd.y, 10, Color.RED);
            cDestino.setStroke(Color.WHITE);
            cDestino.setStrokeWidth(2);
            Text tDestino = new Text(pd.x + 12, pd.y + 16, "Destino");
            tDestino.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            mapPane.getChildren().addAll(cDestino, tDestino);
        }
    }

    private Map<String, Punto> calcularPuntos(List<Parada> paradas) {
        Map<String, Punto> puntos = new HashMap<>();

        int n = paradas.size();
        if (n == 0) return puntos;

        double cx = MAP_WIDTH / 2.0;
        double cy = MAP_HEIGHT / 2.0;
        double radio = Math.min(MAP_WIDTH, MAP_HEIGHT) / 2.0 - 70;

        if (n == 1) {
            puntos.put(paradas.get(0).getId(), new Punto(cx, cy));
            return puntos;
        }

        for (int i = 0; i < n; i++) {
            double angulo = -Math.PI / 2 + (2 * Math.PI * i / n);

            double x = cx + radio * Math.cos(angulo);
            double y = cy + radio * Math.sin(angulo);

            puntos.put(paradas.get(i).getId(), new Punto(x, y));
        }

        return puntos;
    }

    private Ruta buscarRutaEntre(String origenId, String destinoId) {
        return service.listarRutas().stream()
                .filter(r -> r.getOrigenId().equals(origenId) && r.getDestinoId().equals(destinoId))
                .findFirst()
                .orElse(null);
    }

    private Line crearLineaDirigida(Ruta ruta, Punto p1, Punto p2, Color color, double grosor) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double len = Math.sqrt(dx * dx + dy * dy);

        if (len == 0) {
            Line linea = new Line(p1.x, p1.y, p2.x, p2.y);
            linea.setStroke(color);
            linea.setStrokeWidth(grosor);
            return linea;
        }

        double ux = dx / len;
        double uy = dy / len;

        double nx = -uy;
        double ny = ux;

        boolean sentidoNatural = ruta.getOrigenId().compareTo(ruta.getDestinoId()) < 0;
        double offset = sentidoNatural ? 6 : -6;

        double radioNodo = 8;
        double espacioFlecha = 10;

        double x1 = p1.x + nx * offset + ux * radioNodo;
        double y1 = p1.y + ny * offset + uy * radioNodo;

        double x2 = p2.x + nx * offset - ux * (radioNodo + espacioFlecha);
        double y2 = p2.y + ny * offset - uy * (radioNodo + espacioFlecha);

        Line linea = new Line(x1, y1, x2, y2);
        linea.setStroke(color);
        linea.setStrokeWidth(grosor);
        return linea;
    }

    private void agregarFlecha(double x1, double y1, double x2, double y2, Color color, double grosor) {
        double phi = Math.toRadians(25);
        double barb = 10;

        double dy = y2 - y1;
        double dx = x2 - x1;
        double theta = Math.atan2(dy, dx);

        double xA = x2 - barb * Math.cos(theta + phi);
        double yA = y2 - barb * Math.sin(theta + phi);

        double xB = x2 - barb * Math.cos(theta - phi);
        double yB = y2 - barb * Math.sin(theta - phi);

        Line a = new Line(x2, y2, xA, yA);
        Line b = new Line(x2, y2, xB, yB);

        a.setStroke(color);
        b.setStroke(color);
        a.setStrokeWidth(grosor);
        b.setStrokeWidth(grosor);

        mapPane.getChildren().addAll(a, b);
    }

    private static class Punto {
        double x;
        double y;

        Punto(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}