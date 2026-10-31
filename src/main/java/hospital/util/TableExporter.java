package hospital.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/* Экспорт текущего содержимого TableView в CSV-файл */
public class TableExporter {

    public static <T> void exportCsv(TableView<T> table, String defaultName) {
        if (table.getItems() == null || table.getItems().isEmpty()) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Сохранить таблицу в CSV");
        fc.setInitialFileName(defaultName + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fc.showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {

            pw.write('\ufeff'); // BOM для Excel

            // Заголовки
            StringBuilder h = new StringBuilder();
            for (int i = 0; i < table.getColumns().size(); i++) {
                if (i > 0) h.append(';');
                h.append(escape(table.getColumns().get(i).getText()));
            }
            pw.println(h);

            // Строки
            for (T row : table.getItems()) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < table.getColumns().size(); i++) {
                    if (i > 0) line.append(';');
                    var observable = table.getColumns().get(i).getCellObservableValue(row);
                    Object val = (observable == null) ? "" : observable.getValue();
                    line.append(escape(val == null ? "" : val.toString()));
                }
                pw.println(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        // ISO-дата: оборачиваем в формулу, чтобы Excel не показывал ####
        if (s.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            return "=\"" + s.replace("\"", "\"\"") + "\"";
        }
        if (s.contains(";") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}