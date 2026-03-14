package citygraph.ui.Controllers;

import citygraph.core.AppState;
import citygraph.service.CityGraphService;
import citygraph.ui.StateAware;
import citygraph.ui.Navigator;
import citygraph.graph.GrafoTransporte;
import citygraph.model.Parada;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Comparator;
import java.util.List;

public class ParadasController implements StateAware {

    @FXML private ListView<Parada> lstParadas;

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtLat;
    @FXML private TextField txtLon;

    @FXML private TextArea txtMsg;

    private AppState state;
    private Navigator nav;
    private CityGraphService service;


    @FXML
    private void initialize() {
        if (lstParadas != null) {
            lstParadas.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV == null) {
                   habilitarEdicion(true);
                   return;
                }
                txtId.setText(newV.getId());
                txtNombre.setText(newV.getNombre());
                txtLat.setText(newV.getLat() == null ? "" : String.valueOf(newV.getLat()));
                txtLon.setText(newV.getLon() == null ? "" : String.valueOf(newV.getLon()));


                habilitarEdicion(false);
                msg("Parada seleccionada: " + newV.getId() + " -> " + newV.getNombre());



            });
        }
    }

    @Override
    public void setState(AppState state, Navigator nav) {
        this.state = state;
        this.nav = nav;
        this.service = state.getService();

        refrescarLista();
        msg("Listo. Puedes agregar y eliminar paradas.");
    }

    @FXML
    private void onVolver() {
        nav.goTo("/citygraph/home-view.fxml", "CityGraph - Inicio");
    }

    @FXML
    private void onAgregarParada() {
        try {
            String id = safe(txtId.getText());
            String nombre = safe(txtNombre.getText());

            if (id.isBlank() || nombre.isBlank()) {
                msg("ID y Nombre son obligatorios.");
                return;
            }

            Double lat = parseNullableDouble(txtLat.getText());
            Double lon = parseNullableDouble(txtLon.getText());

            service.agregarParada(new Parada(id, nombre, lat, lon));

            refrescarLista();
            limpiarInputsYSeleccion();
            msg("Parada agregada: " + id);
        } catch (Exception e) {
            msg("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onEliminarParada() {
        Parada p = lstParadas.getSelectionModel().getSelectedItem();
        if (p == null) {
            msg("Selecciona una parada para eliminar.");
            return;
        }
        try {
            service.eliminarParada(p.getId());
            refrescarLista();
            msg("Parada eliminada: " + p.getId());
        } catch (Exception e) {
            msg("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onModificarParada() {
        Parada sel = lstParadas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            msg("Selecciona una parada para modificar.");
            return;
        }

        try {
            String id = sel.getId();
            String nuevoNombre = safe(txtNombre.getText());
            Double lat = parseNullableDouble(txtLat.getText());
            Double lon = parseNullableDouble(txtLon.getText());


            if (nuevoNombre.isBlank()) nuevoNombre = null;

            service.modificarParada(id, nuevoNombre, lat, lon);

            refrescarLista();
            msg("Parada modificada: " + id);
        } catch (Exception e) {
            msg("Error: " + e.getMessage());
        }
    }


    @FXML
    private void onNuevaParada() {
        limpiarInputsYSeleccion();
        msg("Modo agregar: completa los campos.");
    }

    private void limpiarInputsYSeleccion() {
        // Limpiar inputs
        txtId.clear();
        txtNombre.clear();
        txtLat.clear();
        txtLon.clear();



        if (lstParadas != null) {
            lstParadas.getSelectionModel().clearSelection();
        }

        habilitarEdicion(true);
    }

    private void habilitarEdicion(boolean habilitar) {
        if (txtId != null) txtId.setDisable(!habilitar);

    }

    private void refrescarLista() {
        List<Parada> paradas = service.getGrafo().listarParadas().stream()
                .sorted(Comparator.comparing(Parada::getId))
                .toList();
        lstParadas.setItems(FXCollections.observableArrayList(paradas));
    }



    private void msg(String s) {
        txtMsg.setText(s);
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.trim();
    }

    private static Double parseNullableDouble(String s) {
        String t = safe(s);
        if (t.isBlank()) return null;
        return Double.parseDouble(t);
    }
}