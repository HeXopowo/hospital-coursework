package hospital.util;

import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.function.Function;

// Добавляет таблице контекстное меню (правый клик) и двойной клик для редактирования
public class TableActions {

    public static <T> void install(TableView<T> table,
                                   Runnable onEdit,
                                   Runnable onDelete,
                                   Function<T, String> toText) {
        ContextMenu ctx = new ContextMenu();
        MenuItem edit   = new MenuItem("Изменить");
        MenuItem delete = new MenuItem("Удалить");
        MenuItem copy   = new MenuItem("Копировать");

        edit  .setOnAction(e -> onEdit.run());
        delete.setOnAction(e -> onDelete.run());
        copy  .setOnAction(e -> {
            T sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ClipboardContent c = new ClipboardContent();
                c.putString(toText.apply(sel));
                Clipboard.getSystemClipboard().setContent(c);
            }
        });
        ctx.getItems().addAll(edit, delete, copy);
        table.setContextMenu(ctx);

        table.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) onEdit.run();
            });
            return row;
        });
    }
}