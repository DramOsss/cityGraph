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
    exports citygraph.ui.Controllers;
    opens citygraph.ui.Controllers to javafx.fxml;
}
