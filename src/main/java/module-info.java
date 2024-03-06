    module cs.ubb.socialnetworkfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens cs.ubb.socialnetworkfx to javafx.fxml;
    exports cs.ubb.socialnetworkfx;

    opens cs.ubb.socialnetworkfx.controller to javafx.fxml;
    opens cs.ubb.socialnetworkfx.domain to javafx.base;
}