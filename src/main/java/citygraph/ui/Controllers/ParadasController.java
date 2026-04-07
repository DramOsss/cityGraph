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

/**
 * Controlador de la interfaz de usuario para la gestión de paradas (nodos) del sistema.
 * * Proporciona la lógica necesaria para realizar operaciones CRUD sobre las paradas
 * de la red de transporte. Gestiona la sincronización entre el formulario de entrada,
 * la lista visual de elementos y el servicio de lógica de negocio, permitiendo
 * la inserción, edición y eliminación de vértices en el grafo.
 */
public class ParadasController implements StateAware {

    @FXML private ListView<Parada> lstParadas;

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;

    @FXML private TextArea txtMsg;

    private Navigator nav;
    private CityGraphService service;

    /**
     * Inicializa los escuchadores de eventos para los componentes de la interfaz.
     * * Configura el {@code selectionModel} de la lista para que, al seleccionar una parada,
     * los campos de texto se carguen automáticamente y se bloquee la edición del ID
     * para mantener la integridad referencial.
     */
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

    /**
     * Establece el estado inicial del controlador tras la navegación.
     * * @param state El estado global de la aplicación.
     * @param nav El navegador para la gestión de flujos de la UI.
     */
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
    /**
     * Procesa la creación de una nueva parada en el sistema.
     * * Valida que los campos obligatorios no estén vacíos y solicita al servicio
     * la creación del nodo tanto en memoria como en la base de datos.
     */
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

    /**
     * Elimina la parada seleccionada actualmente en la lista.
     * * Esta operación desencadena la eliminación en cascada de las rutas
     * asociadas a dicho nodo en el servicio.
     */
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
    /**
     * Actualiza la información descriptiva de una parada existente.
     * * El identificador (ID) se mantiene constante para preservar la topología
     * del grafo, permitiendo únicamente el cambio del nombre comercial o descriptivo.
     */
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
    /*
     * Prepara la interfaz para la creación de una nueva parada.
     * * Resetea el formulario de entrada y notifica al usuario que el sistema
     * se encuentra en modo de inserción, permitiendo la definición de un nuevo
     * identificador único.
     */
    private void onNuevaParada() {
        limpiarInputsYSeleccion();
        msg("Modo agregar: completa los campos.");
    }

    /**
     * Sincroniza el componente visual de la lista con los datos del servicio.
     * * Consulta al {@link CityGraphService} para obtener la colección actualizada
     * de paradas ordenadas y refresca el {@code ListView} mediante un
     * {@link javafx.collections.ObservableList}.
     */
    private void refrescarLista() {
        lstParadas.setItems(FXCollections.observableArrayList(service.listarParadasOrdenadas()));
    }

    /**
     * Restablece los componentes de entrada de datos a su estado inicial.
     * * Limpia los campos de texto de ID y Nombre, remueve cualquier selección
     * activa en la lista y asegura que el campo de identificador sea editable
     * para permitir nuevas entradas.
     */
    private void limpiarInputsYSeleccion() {
        txtId.clear();
        txtNombre.clear();


        if (lstParadas != null) {
            lstParadas.getSelectionModel().clearSelection();
        }

        habilitarEdicion(true);
    }
    /**
     * Controla la disponibilidad del campo de identificador.
     * * @param habilitar {@code true} para permitir la escritura, {@code false} para bloquearla.
     */
    private void habilitarEdicion(boolean habilitar) {
        if (txtId != null) {
            txtId.setDisable(!habilitar);
        }
    }

    /**
     * Muestra un mensaje informativo o de error en el área de notificaciones de la vista.
     * * @param s El mensaje a mostrar.
     */
    private void msg(String s) {
        txtMsg.setText(s);
    }

    /**
     * Normaliza una cadena de texto eliminando espacios en blanco innecesarios.
     * * @param s Cadena de entrada.
     * @return Cadena procesada o vacía si la entrada es nula.
     */
    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Intenta convertir una cadena en un valor numérico de doble precisión.
     * * @param s Cadena con formato numérico.
     * @return El valor convertido o {@code null} si la entrada es inválida o vacía.
     */
    private static Double parseNullableDouble(String s) {
        String t = safe(s);
        if (t.isBlank()) return null;
        return Double.parseDouble(t);
    }
}