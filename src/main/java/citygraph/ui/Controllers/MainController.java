package citygraph.ui.Controllers;

import citygraph.algorithms.Dijkstra;
import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.Parada;
import citygraph.model.ResultadoRuta;
import citygraph.model.Ruta;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

import java.util.Comparator;
import java.util.List;

public class MainController {

    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;
    @FXML private ComboBox<CriterioOptimizacion> cmbCriterio;
    @FXML private TextArea txtResultado;

    private final GrafoTransporte grafo = new GrafoTransporte();

    @FXML
    public void initialize() {
        cmbCriterio.setItems(FXCollections.observableArrayList(CriterioOptimizacion.values()));
        cmbCriterio.getSelectionModel().select(CriterioOptimizacion.TIEMPO);
        txtResultado.setText("Presiona 'Cargar Datos Demo' para cargar una red de ejemplo.");
    }

    @FXML
    private void onCargarDemo() {
//        // Limpio y vuelvo a crear (simple para el avance)
//        // (Si quieres, luego hacemos un método reset en el grafo)
//        cargarDatosDemo();
//
//        List<Parada> paradas = grafo.listarParadas().stream()
//                .sorted(Comparator.comparing(Parada::getId))
//                .toList();
//
//        cmbOrigen.setItems(FXCollections.observableArrayList(paradas));
//        cmbDestino.setItems(FXCollections.observableArrayList(paradas));
//
//        if (!paradas.isEmpty()) {
//            cmbOrigen.getSelectionModel().select(0);
//            cmbDestino.getSelectionModel().select(Math.min(1, paradas.size() - 1));
//        }
//
//        txtResultado.setText("Demo cargado. Selecciona origen, destino y criterio, luego 'Calcular Ruta'.");
    }

    @FXML
    private void onCalcular() {
        Parada origen = cmbOrigen.getValue();
        Parada destino = cmbDestino.getValue();
        CriterioOptimizacion criterio = cmbCriterio.getValue();

        if (origen == null || destino == null || criterio == null) {
            txtResultado.setText("Selecciona origen, destino y criterio.");
            return;
        }

        ResultadoRuta res = Dijkstra.calcular(grafo, origen.getId(), destino.getId(), criterio);
        txtResultado.setText(res.toString());
    }

    private void cargarDatosDemo() {
//        // Re-crear grafo (para mantener simple). Si ya cargaste antes, reinicia el proyecto o crea un reset.
//        // Para evitar duplicados, lo más simple es crear un grafo nuevo: (pero el campo es final)
//        // Solución rápida: solo carga una vez si está vacío.
//        if (grafo.numeroParadas() > 0) return;
//
//        grafo.agregarParada(new Parada("A", "Centro"));
//        grafo.agregarParada(new Parada("B", "Hospital Central"));
//        grafo.agregarParada(new Parada("C", "Universidad"));
//        grafo.agregarParada(new Parada("D", "Aeropuerto"));
//
//        grafo.agregarRuta(new Ruta("A", "B", 5, 2.0, 35, 0));
//        grafo.agregarRuta(new Ruta("A", "C", 3, 1.5, 25, 0));
//        grafo.agregarRuta(new Ruta("C", "B", 2, 1.0, 15, 0));
//        grafo.agregarRuta(new Ruta("B", "D", 6, 3.0, 40, 1));
//        grafo.agregarRuta(new Ruta("C", "D", 10, 6.0, 50, 1));
    }
}