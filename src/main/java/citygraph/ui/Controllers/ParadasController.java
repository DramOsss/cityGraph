package citygraph.ui.Controllers;

import citygraph.core.AppState;
import citygraph.model.Parada;
import citygraph.service.CityGraphService;
import citygraph.ui.Navigator;
import citygraph.ui.StateAware;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ParadasController implements StateAware {

    @FXML private ListView<Parada> lstParadas;

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;

    @FXML private TextArea txtMsg;

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

                habilitarEdicion(false);
                msg("Parada seleccionada: " + newV.getId() + " -> " + newV.getNombre());
            });
        }
    }

    @Override
    public void setState(AppState state, Navigator nav) {
        this.nav = nav;
        this.service = state.getService();

        refrescarLista();
        msg("Listo. Puedes agregar, modificar y eliminar paradas.");
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


            service.agregarParada(new Parada(id, nombre));

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
            limpiarInputsYSeleccion();
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

            if (nuevoNombre.isBlank()) {
                nuevoNombre = null;
            }

            service.modificarParada(id, nuevoNombre);

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

    private void refrescarLista() {
        lstParadas.setItems(FXCollections.observableArrayList(service.listarParadasOrdenadas()));
    }

    private void limpiarInputsYSeleccion() {
        txtId.clear();
        txtNombre.clear();


        if (lstParadas != null) {
            lstParadas.getSelectionModel().clearSelection();
        }

        habilitarEdicion(true);
    }

    private void habilitarEdicion(boolean habilitar) {
        if (txtId != null) {
            txtId.setDisable(!habilitar);
        }
    }

    private void msg(String s) {
        txtMsg.setText(s);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static Double parseNullableDouble(String s) {
        String t = safe(s);
        if (t.isBlank()) return null;
        return Double.parseDouble(t);
    }
}