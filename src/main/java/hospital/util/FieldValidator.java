package hospital.util;

import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

// Валидирует поле "на лету": красная рамка + подсказка при несоответствии

public class FieldValidator {
    public static void attach(TextInputControl field, String regex, String hint) {
        Tooltip tip = new Tooltip(hint);
        field.textProperty().addListener((obs, o, v) -> {
            boolean empty   = (v == null || v.isEmpty());
            boolean invalid = !empty && !v.matches(regex);

            if (invalid) {
                if (!field.getStyleClass().contains("field-error")) {
                    field.getStyleClass().add("field-error");
                }
                field.setTooltip(tip);
            } else {
                field.getStyleClass().remove("field-error");
                field.setTooltip(null);
            }
        });
    }
}