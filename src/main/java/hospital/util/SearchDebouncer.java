package hospital.util;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.function.Consumer;

// Задерживает вызов действия до паузы ввода
// Защищает БД от штурма запросами при быстром наборе текста
public class SearchDebouncer {

    private final PauseTransition pause;
    private String pendingValue = "";

    public SearchDebouncer(int delayMs, Consumer<String> action) {
        this.pause = new PauseTransition(Duration.millis(delayMs));
        this.pause.setOnFinished(e -> action.accept(pendingValue));
    }

    public SearchDebouncer(Consumer<String> action) {
        this(300, action);
    }

    public void trigger(String value) {
        pendingValue = value == null ? "" : value;
        pause.playFromStart();
    }
}