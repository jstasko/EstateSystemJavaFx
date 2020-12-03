module EstateSystem {
    requires javafx.controls;
    requires javafx.fxml;



    // export controller
    opens sk.stasko.controller to javafx.fxml;
    exports sk.stasko.controller;


    exports sk.stasko.model.realEstate;
    exports sk.stasko.model.gps;
    exports sk.stasko.core.savableObject;
    opens sk.stasko;
}