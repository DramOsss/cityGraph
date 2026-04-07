package citygraph.ui.Controllers;

import citygraph.core.AppState;
import citygraph.model.Parada;
import citygraph.model.Ruta;
import citygraph.model.TipoTransporte;
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

/**
 * Controlador de la interfaz de usuario para la gestión de rutas (aristas) del sistema.
 * * Esta clase permite administrar las conexiones entre paradas, definiendo sus pesos
 * (tiempo, distancia, costo) y el medio de transporte. Incluye lógica avanzada para
 * la creación de rutas bidireccionales y validaciones de integridad para evitar
 * la formación de ciclos negativos o conexiones redundantes.
 */
public class RutasController implements StateAware {

    @FXML private ListView<Ruta> lstRutas;

    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;
    @FXML private ComboBox<TipoTransporte> cmbTipo;

    @FXML private TextField txtTiempo;
    @FXML private TextField txtDistancia;
    @FXML private TextField txtCosto;

    @FXML private TextArea txtMsg;

    private Navigator nav;
    private CityGraphService service;

    /**
     * Inicializa los componentes de la interfaz y configura los escuchadores de eventos.
     * * Al seleccionar una ruta de la lista, el formulario se completa automáticamente
     * con los datos actuales y se deshabilitan los selectores de origen/destino
     * para proteger la clave primaria de la conexión.
     */
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
                cmbTipo.getSelectionModel().select(r.getTipoTransporte());

                habilitarEdicionPk(false);
                msg("Ruta seleccionada: " + r.getOrigenId() + " -> " + r.getDestinoId());
            });
        }
    }

    /**
     * Establece el estado inicial del controlador, cargando los catálogos de
     * paradas, tipos de transporte y el listado de rutas existentes.
     * * @param state El estado global de la aplicación.
     * @param nav El navegador para la gestión de flujos de la UI.
     */
    @Override
    public void setState(AppState state, Navigator nav) {
        this.nav = nav;
        this.service = state.getService();

        cmbTipo.setItems(FXCollections.observableArrayList(TipoTransporte.values()));
        cmbTipo.getSelectionModel().select(TipoTransporte.BUS);

        refrescarCombos();
        refrescarListaRutas();

        habilitarEdicionPk(true);
        msg("Listo. Selecciona una ruta para modificar o agrega una nueva.");
    }

    @FXML
    private void onVolver() {
        nav.goTo("/citygraph/home-view.fxml", "CityGraph - Inicio");
    }

    /**
     * Procesa la creación de una nueva ruta dirigida.
     * * Tras una inserción exitosa, el sistema ofrece mediante un cuadro de diálogo
     * la posibilidad de crear automáticamente la ruta inversa con los mismos
     * parámetros, facilitando la creación de redes bidireccionales.
     */
    @FXML
    private void onAgregarRuta() {
        try {
            Parada origen = cmbOrigen.getValue();
            Parada destino = cmbDestino.getValue();
            TipoTransporte tipo = cmbTipo.getValue();

            if (origen == null || destino == null) {
                msg("Selecciona origen y destino.");
                return;
            }

            if (tipo == null) {
                msg("Selecciona un tipo de transporte.");
                return;
            }

            if (origen.getId().equals(destino.getId())) {
                msg("Origen y destino no pueden ser iguales.");
                return;
            }

            double tiempo = Double.parseDouble(txtTiempo.getText().trim());
            double distancia = Double.parseDouble(txtDistancia.getText().trim());
            double costo = Double.parseDouble(txtCosto.getText().trim());

            Ruta rutaPrincipal = new Ruta(
                    origen.getId(),
                    destino.getId(),
                    tiempo,
                    distancia,
                    costo,
                    tipo
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
                            tipo
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
    /**
     * Procesa la eliminación de la conexión seleccionada en la interfaz de usuario.
     * * El método realiza las siguientes acciones:
     * 1. **Verificación de Selección:** Valida que el usuario haya marcado una
     * arista específica en el {@link ListView} de rutas.
     * 2. **Remoción Persistente:** Invoca al {@link CityGraphService} para
     * ejecutar la eliminación tanto en la estructura de datos en memoria como
     * en el almacenamiento físico (Base de Datos).
     * 3. **Actualización de Interfaz:** Refresca la lista visual de rutas para
     * reflejar el cambio, limpia los campos de entrada de datos y notifica
     * el éxito o fracaso de la operación mediante un mensaje emergente.
     * * Maneja excepciones de integridad referencial o conectividad informando
     * al usuario sobre la causa del error.
     */
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

    /**
     * Actualiza las métricas y el tipo de transporte de una ruta existente.
     * * No permite modificar el origen o el destino; para cambiar la topología,
     * se debe eliminar la ruta y crear una nueva.
     */
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
            TipoTransporte tipo = cmbTipo.getValue();

            if (tiempo == null || distancia == null || costo == null) {
                msg("Tiempo, distancia y costo son obligatorios.");
                return;
            }

            if (tipo == null) {
                msg("Selecciona un tipo de transporte.");
                return;
            }

            Ruta rutaModificada = new Ruta(
                    origenId,
                    destinoId,
                    tiempo,
                    distancia,
                    costo,
                    tipo
            );

            service.modificarRuta(rutaModificada);

            refrescarListaRutas();
            msg("Ruta modificada: " + origenId + " -> " + destinoId);

        } catch (Exception e) {
            msg("Error: " + e.getMessage());
        }
    }

    /**
     * Prepara la interfaz para la creación de una nueva conexión entre paradas.
     * * Invoca la limpieza de los campos de entrada y notifica al usuario que
     * el sistema se encuentra en modo de inserción, permitiendo nuevamente
     * la selección de los puntos de origen y destino.
     */
    @FXML
    private void onNuevaRuta() {
        limpiarInputsYSeleccion();
        msg("Modo agregar: selecciona origen/destino, tipo y completa los campos.");
    }

    /**
     * Sincroniza los selectores de origen y destino con las paradas registradas.
     * * Actualiza los {@link ComboBox} con la lista ordenada de paradas obtenida
     * del servicio y establece selecciones por defecto (generalmente los dos
     * primeros elementos) para agilizar la entrada de datos.
     */
    private void refrescarCombos() {
        List<Parada> paradas = service.listarParadasOrdenadas();

        cmbOrigen.setItems(FXCollections.observableArrayList(paradas));
        cmbDestino.setItems(FXCollections.observableArrayList(paradas));

        if (!paradas.isEmpty()) {
            cmbOrigen.getSelectionModel().select(0);
            cmbDestino.getSelectionModel().select(Math.min(1, paradas.size() - 1));
        }
    }

    /**
     * Actualiza el componente visual de la lista de rutas.
     * * Consulta al {@link CityGraphService} para obtener el listado completo
     * de aristas ordenadas y refresca el {@code ListView} sincronizándolo con
     * el estado actual del grafo.
     */
    private void refrescarListaRutas() {
        lstRutas.setItems(FXCollections.observableArrayList(service.listarRutasOrdenadas()));
    }

    /**
     * Restablece los componentes de entrada de datos a su estado inicial.
     * * Limpia los campos de texto numéricos, devuelve el selector de transporte
     * a su valor predeterminado (BUS) y habilita la edición de la clave primaria
     * (origen/destino) para permitir el registro de una nueva ruta.
     */
    private void limpiarInputsYSeleccion() {
        txtTiempo.clear();
        txtDistancia.clear();
        txtCosto.clear();

        if (cmbTipo != null) {
            cmbTipo.getSelectionModel().select(TipoTransporte.BUS);
        }

        if (lstRutas != null) {
            lstRutas.getSelectionModel().clearSelection();
        }

        habilitarEdicionPk(true);
    }

    /**
     * Controla la edición de los selectores que conforman la clave primaria de la ruta.
     * * @param habilitar {@code true} para permitir seleccionar origen/destino,
     * {@code false} para bloquearlos durante una edición.
     */
    private void habilitarEdicionPk(boolean habilitar) {
        if (cmbOrigen != null) cmbOrigen.setDisable(!habilitar);
        if (cmbDestino != null) cmbDestino.setDisable(!habilitar);
    }

    /**
     * Muestra un mensaje informativo en el área de estado de la interfaz.
     * * @param s El mensaje a mostrar.
     */
    private void msg(String s) {
        txtMsg.setText(s);
    }

    /**
     * Intenta convertir una cadena de texto en un valor numérico de doble precisión,
     * manejando de forma segura entradas nulas o vacías.
     * * El método realiza una limpieza de espacios en blanco (trim) antes de la
     * evaluación para evitar errores de formato comunes en la entrada del usuario.
     * * @param s La cadena de texto a convertir.
     * @return El valor de tipo {@code Double} si la cadena es un número válido;
     * {@code null} si la cadena es nula, está vacía o solo contiene espacios.
     * @throws NumberFormatException Si la cadena no es nula ni vacía pero no
     * representa un número válido.
     */
    private static Double parseNullableDouble(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isBlank()) return null;
        return Double.parseDouble(t);
    }

    /**
     * Utilidad para sincronizar la selección de un ComboBox de paradas basándose
     * únicamente en el identificador alfanumérico.
     * * @param combo El componente ComboBox a manipular.
     * @param paradaId El ID de la parada que debe ser seleccionada.
     */
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