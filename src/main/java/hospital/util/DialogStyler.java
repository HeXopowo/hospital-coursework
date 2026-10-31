package hospital.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public final class DialogStyler {

    private static final String CSS;

    static {
        var url = DialogStyler.class.getResource("/hospital/view/css/styles.css");
        CSS = (url != null) ? url.toExternalForm() : null;
    }

    private DialogStyler() {}

    public static void apply(Dialog<?> dialog) {
        apply(dialog.getDialogPane());
    }

    public static void apply(Alert alert) {
        apply(alert.getDialogPane());
    }

    public static void apply(DialogPane pane) {
        if (CSS != null && !pane.getStylesheets().contains(CSS)) {
            pane.getStylesheets().add(CSS);
        }
    }
}