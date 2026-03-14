package citygraph.ui.Controllers;

import citygraph.algorithms.Dijkstra;
import citygraph.core.AppState;
import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.Parada;
import citygraph.model.ResultadoRuta;
import citygraph.ui.Navigator;
import citygraph.ui.StateAware;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

import java.util.Comparator;
import java.util.List;

public class CalcularController implements StateAware {

    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;
    @FXML private ComboBox<CriterioOptimizacion> cmbCriterio;
    @FXML private TextArea txtResultado;

    private Navigator nav;
    private GrafoTransporte grafo;

    @Override
    public void setState(AppState state, Navigator nav) {
        this.nav = nav;
        this.grafo = state.getService().getGrafo();

        cmbCriterio.setItems(FXCollections.observableArrayList(CriterioOptimizacion.values()));
        cmbCriterio.getSelectionModel().select(CriterioOptimizacion.TIEMPO);

        refrescarParadas();
        txtResultado.setText("Selecciona origen, destino y criterio, luego presiona Calcular.");
    }

    @FXML
    private void onVolver() {
        nav.goTo("/citygraph/home-view.fxml", "CityGraph - Inicio");
    }

    @FXML
    private void onRefrescar() {
        refrescarParadas();
        txtResultado.setText("Paradas refrescadas.");
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
            ResultadoRuta res = Dijkstra.calcular(grafo, o.getId(), d.getId(), criterio);

            if (res == null || res.getCamino().isEmpty() || Double.isInfinite(res.getPesoOptimo())) {

                txtResultado.setText(
                        "No se encontró un camino entre las paradas seleccionadas.\n"  +
                        "Verifique que exista una conexión entre ellas.");

            } else {


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
                txtResultado.setText(sb.toString());
            }
        } catch (Exception e) {
            txtResultado.setText("Error: " + e.getMessage());
        }
    }

    private void refrescarParadas() {
        List<Parada> paradas = grafo.listarParadas().stream()
                .sorted(Comparator.comparing(Parada::getId))
                .toList();

        cmbOrigen.setItems(FXCollections.observableArrayList(paradas));
        cmbDestino.setItems(FXCollections.observableArrayList(paradas));

        if (!paradas.isEmpty()) {
            cmbOrigen.getSelectionModel().select(0);
            cmbDestino.getSelectionModel().select(Math.min(1, paradas.size() - 1));
        }
    }
}