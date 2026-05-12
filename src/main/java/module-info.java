module com.defragler.fixiqo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires eu.hansolo.fx.countries;
    requires eu.hansolo.toolbox;
    
    requires org.xerial.sqlitejdbc;
    requires jakarta.mail;
    requires jbcrypt;
    requires com.fasterxml.jackson.annotation;
    requires java.desktop;
    requires atlantafx.base;
    requires com.jthemedetector;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.reflections;

    opens com.defragler.fixiqo.entities.contexts to com.fasterxml.jackson.databind;
    opens com.defragler.fixiqo.services.enums to com.fasterxml.jackson.databind;
    opens com.defragler.fixiqo.views.controllers.windows to javafx.fxml;
    opens com.defragler.fixiqo.views.controllers.modals to javafx.fxml;
    opens com.defragler.fixiqo.views.controllers.pages to javafx.fxml;
    opens com.defragler.fixiqo.views.controllers to javafx.fxml;
    
    opens com.defragler.fixiqo to javafx.fxml;
    exports com.defragler.fixiqo;
}