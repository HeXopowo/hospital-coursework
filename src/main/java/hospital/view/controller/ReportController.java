package hospital.view.controller;

import hospital.ReportDao;
import hospital.daomodel.Report;
import hospital.util.DialogStyler;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportController {

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private TableView<Report> reportTable;
    @FXML private TableColumn<Report, String> categoryColumn;
    @FXML private TableColumn<Report, Number> countColumn;
    @FXML private TableColumn<Report, String> additionalColumn;
    @FXML private TableColumn<Report, String> extra1Column;
    @FXML private TableColumn<Report, String> extra2Column;
    @FXML private Label summaryLabel;

    private final ObservableList<Report> reportData = FXCollections.observableArrayList();
    private final ReportDao reportDao = new ReportDao();
    private List<Report> lastResults;

    @FXML
    private void initialize() {
        categoryColumn   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        countColumn      .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCount()));
        additionalColumn .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAdditionalInfo()));
        extra1Column     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getExtra1()));
        extra2Column     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getExtra2()));

        reportTable.setItems(reportData);
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Приёмы за период",
                "Загрузка врачей",
                "Пациенты по участкам",
                "Статистика по статусам",
                "Общая сводка"));
        reportTypeComboBox.setValue("Приёмы за период");

        LocalDate now = LocalDate.now();
        dateFromPicker.setValue(now.minusDays(30));
        dateToPicker.setValue(now);

        reportTypeComboBox.valueProperty().addListener((obs, o, v) -> {
            boolean needsPeriod = !"Пациенты по участкам".equals(v);
            dateFromPicker.setDisable(!needsPeriod);
            dateToPicker.setDisable(!needsPeriod);
        });
    }

    @FXML
    private void handleGenerateReport() {
        String type = reportTypeComboBox.getValue();
        if (type == null) { showError("Выберите тип отчёта"); return; }

        LocalDate from = dateFromPicker.getValue();
        LocalDate to   = dateToPicker.getValue();
        boolean needsPeriod = !"Пациенты по участкам".equals(type);

        if (needsPeriod) {
            if (from == null || to == null) { showError("Укажите период"); return; }
            if (from.isAfter(to))           { showError("Дата начала позже даты окончания"); return; }
        }

        try {
            List<Report> rows = reportDao.generateReport(type, from, to);
            reportData.setAll(FXCollections.observableArrayList(rows));
            lastResults = rows;

            updateColumnsForType(type);
            updateSummary(type, rows, from, to);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка формирования отчёта: " + e.getMessage());
        }
    }

    // Настраивает видимость столбцов и их заголовки под текущий тип отчёта
    private void updateColumnsForType(String type) {
        // По умолчанию все столбцы видимы, потом точечно скрываем
        setAll(true);

        switch (type) {
            case "Приёмы за период" -> {
                categoryColumn.setText("Дата и время");
                additionalColumn.setText("Врач");
                extra1Column.setText("Пациент");
                extra2Column.setText("Статус");
                hide(countColumn);
            }
            case "Загрузка врачей" -> {
                categoryColumn.setText("Врач");
                countColumn.setText("Приёмов");
                additionalColumn.setText("Специализация");
                hide(extra1Column);
                hide(extra2Column);
            }
            case "Пациенты по участкам" -> {
                categoryColumn.setText("Участок");
                countColumn.setText("Пациентов");
                // Лишний 3-й столбец «пациентов» убираем
                hide(additionalColumn);
                hide(extra1Column);
                hide(extra2Column);
            }
            case "Статистика по статусам" -> {
                categoryColumn.setText("Статус");
                countColumn.setText("Количество");
                additionalColumn.setText("Доля");
                hide(extra1Column);
                hide(extra2Column);
            }
            case "Общая сводка" -> {
                categoryColumn.setText("Показатель");
                countColumn.setText("Значение");
                additionalColumn.setText("Комментарий");
                hide(extra1Column);
                hide(extra2Column);
            }
        }
    }

    private void setAll(boolean visible) {
        for (TableColumn<?, ?> c : new TableColumn<?, ?>[]{
                categoryColumn, countColumn, additionalColumn, extra1Column, extra2Column}) {
            c.setVisible(visible);
        }
    }

    private void hide(TableColumn<?, ?> c) {
        c.setVisible(false);
    }

    private void updateSummary(String type, List<Report> rows, LocalDate from, LocalDate to) {
        String summary = switch (type) {
            case "Приёмы за период"        -> "Всего приёмов: " + rows.size();
            case "Загрузка врачей"         -> "Всего врачей: " + rows.size();
            case "Пациенты по участкам"    -> "Всего пациентов: " +
                    rows.stream().mapToInt(Report::getCount).sum();
            case "Статистика по статусам"  -> "Всего приёмов: " +
                    rows.stream().mapToInt(Report::getCount).sum();
            case "Общая сводка"            -> "Период: " + from + " — " + to;
            default -> "";
        };
        summaryLabel.setText(rows.isEmpty() ? summary + " (нет данных)" : summary);
    }

    // CSV
    @FXML
    private void handleExportCsv() {
        if (lastResults == null || lastResults.isEmpty()) {
            showError("Сначала сформируйте отчёт"); return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Сохранить отчёт");
        fc.setInitialFileName("report_" + reportTypeComboBox.getValue().replace(" ", "_") + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fc.showSaveDialog(new Stage());
        if (file == null) return;

        // Собираем ТОЛЬКО видимые столбцы (скрытые в отчёт не попадают)
        List<TableColumn<Report, ?>> visibleColumns = new java.util.ArrayList<>();
        if (categoryColumn.isVisible())   visibleColumns.add(categoryColumn);
        if (countColumn.isVisible())      visibleColumns.add(countColumn);
        if (additionalColumn.isVisible()) visibleColumns.add(additionalColumn);
        if (extra1Column.isVisible())     visibleColumns.add(extra1Column);
        if (extra2Column.isVisible())     visibleColumns.add(extra2Column);

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            pw.write('\ufeff'); // BOM для Excel

            // Заголовок
            StringBuilder header = new StringBuilder();
            for (int i = 0; i < visibleColumns.size(); i++) {
                if (i > 0) header.append(';');
                header.append(csvSafe(visibleColumns.get(i).getText()));
            }
            pw.println(header);

            // Данные
            for (Report r : lastResults) {
                StringBuilder row = new StringBuilder();
                for (int i = 0; i < visibleColumns.size(); i++) {
                    if (i > 0) row.append(';');
                    row.append(csvSafe(getValueForColumn(r, visibleColumns.get(i))));
                }
                pw.println(row);
            }
            showInfo("Отчёт сохранён:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Ошибка экспорта: " + e.getMessage());
        }
    }

    // Возвращает значение поля Report, соответствующее конкретному столбцу
    private String getValueForColumn(Report r, TableColumn<Report, ?> col) {
        if (col == categoryColumn)   return r.getCategory();
        if (col == countColumn)      return String.valueOf(r.getCount());
        if (col == additionalColumn) return r.getAdditionalInfo();
        if (col == extra1Column)     return r.getExtra1();
        if (col == extra2Column)     return r.getExtra2();
        return "";
    }

    private String csvSafe(String value) {
        if (value == null) return "";
        // ISO-дата или дата-время
        if (value.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            return "=\"" + value.replace("\"", "\"\"") + "\"";
        }
        // Обычное экранирование
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String join(String sep, String... parts) {
        return String.join(sep, parts);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Ошибка");
        a.setHeaderText(null);
        a.setContentText(msg);
        DialogStyler.apply(a);
        a.showAndWait();
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Информация");
        a.setHeaderText(null);
        a.setContentText(msg);
        DialogStyler.apply(a);
        a.showAndWait();
    }
}