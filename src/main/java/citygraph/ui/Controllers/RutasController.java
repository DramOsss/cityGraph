package citygraph.ui.Controllers;

import citygraph.core.AppState;
import citygraph.model.Parada;
import citygraph.model.Ruta;
import citygraph.service.CityGraphService;
import citygraph.ui.Navigator;
import citygraph.ui.StateAware;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Optional;

public class RutasController implements StateAware {

    @FXML private ListView<Ruta> lstRutas;

    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;

    @FXML private TextField txtTiempo;
    @FXML private TextField txtDistancia;
    @FXML private TextField txtCosto;
    @FXML private TextField txtTransbordos;

    @FXML private TextArea txtMsg;

    private Navigator nav;
    private CityGraphService service;

    @FXML
    private void initialize() {
        if (lstRutas != null) {
            lstRutas.getSelectionModel().selectedItemProperty().addListener((obs, oldV, r) -> {
                if (r == null) {
                    habilitarEdicionPk(true);
                    return;
                }

                seleccionarParadaEnCombo(cmbOrigen, r.getOrigenId());
                seleccionarParadaEnCombo(cmbDestino, r.getDestinoId());

                txtTiempo.setText(String.valueOf(r.getTiempoMin()));
                txtDistancia.setText(String.valueOf(r.getDistanciaKm()));
                txtCosto.setText(String.valueOf(r.getCosto()));
                txtTransbordos.setText(String.valueOf(r.getTransbordos()));

                habilitarEdicionPk(false);
                msg("Ruta seleccionada: " + r.getOrigenId() + " -> " + r.getDestinoId());
            });
        }
    }

    @Override
    public void setState(AppState state, Navigator nav) {
        this.nav = nav;
        this.service = state.getService();

        refrescarCombos();
        refrescarListaRutas();

        habilitarEdicionPk(true);
        msg("Listo. Selecciona una ruta para modificar o agrega una nueva.");
    }

    @FXML
    private void onVolver() {
        nav.goTo("/citygraph/home-view.fxml", "CityGraph - Inicio");
    }

    @FXML
    private void onAgregarRuta() {
        try {
            Parada origen = cmbOrigen.getValue();
            Parada destino = cmbDestino.getValue();

            if (origen == null || destino == null) {
                msg("Selecciona origen y destino.");
                return;
            }

            if (origen.getId().equals(destino.getId())) {
                msg("Origen y destino no pueden ser iguales.");
                return;
            }

            double tiempo = Double.parseDouble(txtTiempo.getText().trim());
            double distancia = Double.parseDouble(txtDistancia.getText().trim());
            double costo = Double.parseDouble(txtCosto.getText().trim());
            int transbordos = parseIntOrZero(txtTransbordos.getText());

            Ruta rutaPrincipal = new Ruta(
                    origen.getId(),
                    destino.getId(),
                    tiempo,
                    distancia,
                    costo,
                    transbordos
            );

            service.agregarRuta(rutaPrincipal);

            boolean usuarioPidioInversa = false;
            boolean inversaAgregada = false;
            String mensajeFinal = "Ruta agregada: " + origen.getId() + " -> " + destino.getId();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Ruta inversa");
            alert.setHeaderText("¿Desea agregar también la ruta inversa?");
            alert.setContentText("Se agregará la ruta: " +
                    destino.getId() + " -> " + origen.getId());

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                usuarioPidioInversa = true;

                try {
                    Ruta rutaInversa = new Ruta(
                            destino.getId(),
                            origen.getId(),
                            tiempo,
                            distancia,
                            costo,
                            transbordos
                    );

                    service.agregarRuta(rutaInversa);
                    inversaAgregada = true;

                } catch (Exception e) {
                    mensajeFinal = "Ruta agregada: " + origen.getId() + " -> " + destino.getId()
                            + ". La inversa " + destino.getId() + " -> " + origen.getId()
                            + " no se pudo agregar: " + e.getMessage();
                }
            }

            if (usuarioPidioInversa && inversaAgregada) {
                mensajeFinal = "Ruta agregada: " + origen.getId() + " -> " + destino.getId()
                        + " y su inversa: " + destino.getId() + " -> " + origen.getId();
            }

            refrescarListaRutas();
            limpiarInputsYSeleccion();
            msg(mensajeFinal);

        } catch (Exception e) {
            msg("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onEliminarRuta() {
        Ruta r = lstRutas.getSelectionModel().getSelectedItem();
        if (r == null) {
            msg("Selecciona una ruta para eliminar.");
            return;
        }

        try {
            service.eliminarRuta(r.getOrigenId(), r.getDestinoId());
            refrescarListaRutas();
            limpiarInputsYSeleccion();
            msg("Ruta eliminada: " + r.getOrigenId() + " -> " + r.getDestinoId());

        } catch (Exception e) {
            msg("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onModificarRuta() {
        Ruta sel = lstRutas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            msg("Selecciona una ruta para modificar.");
            return;
        }

        try {
            String origenId = sel.getOrigenId();
            String destinoId = sel.getDestinoId();

            Double tiempo = parseNullableDouble(txtTiempo.getText());
            Double distancia = parseNullableDouble(txtDistancia.getText());
            Double costo = parseNullableDouble(txtCosto.getText());
            Integer transbordos = parseNullableInt(txtTransbordos.getText());

            Ruta rutaModificada = new Ruta(
                    origenId,
                    destinoId,
                    tiempo,
                    distancia,
                    costo,
                    transbordos
            );

            service.modificarRuta(rutaModificada);

            refrescarListaRutas();
            msg("Ruta modificada: " + origenId + " -> " + destinoId);

        } catch (Exception e) {
            msg("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onNuevaRuta() {
        limpiarInputsYSeleccion();
        msg("Modo agregar: selecciona origen/destino y completa los campos.");
    }

    private void refrescarCombos() {
        List<Parada> paradas = service.listarParadasOrdenadas();

        cmbOrigen.setItems(FXCollections.observableArrayList(paradas));
        cmbDestino.setItems(FXCollections.observableArrayList(paradas));

        if (!paradas.isEmpty()) {
            cmbOrigen.getSelectionModel().select(0);
            cmbDestino.getSelectionModel().select(Math.min(1, paradas.size() - 1));
        }
    }

    private void refrescarListaRutas() {
        lstRutas.setItems(FXCollections.observableArrayList(service.listarRutasOrdenadas()));
    }

    private void limpiarInputsYSeleccion() {
        txtTiempo.clear();
        txtDistancia.clear();
        txtCosto.clear();
        txtTransbordos.clear();

        if (lstRutas != null) {
            lstRutas.getSelectionModel().clearSelection();
        }

        habilitarEdicionPk(true);
    }

    private void habilitarEdicionPk(boolean habilitar) {
        if (cmbOrigen != null) cmbOrigen.setDisable(!habilitar);
        if (cmbDestino != null) cmbDestino.setDisable(!habilitar);
    }

    private void msg(String s) {
        txtMsg.setText(s);
    }

    private static int parseIntOrZero(String s) {
        if (s == null) return 0;
        String t = s.trim();
        if (t.isBlank()) return 0;
        return Integer.parseInt(t);
    }

    private static Double parseNullableDouble(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isBlank()) return null;
        return Double.parseDouble(t);
    }

    private static Integer parseNullableInt(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isBlank()) return null;
        return Integer.parseInt(t);
    }

    private static void seleccionarParadaEnCombo(ComboBox<Parada> combo, String paradaId) {
        if (combo == null || paradaId == null) return;

        for (Parada p : combo.getItems()) {
            if (paradaId.equals(p.getId())) {
                combo.getSelectionModel().select(p);
                return;
            }
        }
    }
}