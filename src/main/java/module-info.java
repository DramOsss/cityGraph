/**
 * Configuración del sistema de módulos de la aplicación CityGraph.
 * * Este descriptor define la modularidad del proyecto bajo el sistema Java Platform Module System (JPMS).
 * Establece las dependencias externas necesarias para el funcionamiento de la interfaz gráfica (JavaFX)
 * y la conectividad de datos (SQL), además de controlar la visibilidad y accesibilidad de los paquetes
 * para garantizar el correcto funcionamiento del motor de reflexión de JavaFX.
 */
module com.citygraph {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    exports citygraph;

    exports citygraph.ui;
    exports citygraph.model;
    exports citygraph.algorithms;
    exports citygraph.service;

    opens citygraph.ui to javafx.fxml;
    /** * Exporta y abre el paquete de controladores para permitir que el cargador de
     * FXML pueda instanciar e inyectar componentes en las clases controladoras.
     */
    exports citygraph.ui.Controllers;
    opens citygraph.ui.Controllers to javafx.fxml;
}
