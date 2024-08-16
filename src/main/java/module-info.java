module com.example.tree {
    requires javafx.controls;
    requires javafx.fxml;

    requires net.synedra.validatorfx;

    opens com.example.tree to javafx.fxml;
    exports com.example.tree;
    exports com.example.tree.controller;
    exports com.example.tree.exception;
    opens com.example.tree.controller to javafx.fxml;
    opens com.example.tree.exception to javafx.fxml;
}