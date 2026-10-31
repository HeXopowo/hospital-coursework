package hospital.util;

import java.util.List;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;

public final class Constants {
    private Constants() {}


    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_DOCTOR = "DOCTOR";
    public static final String ROLE_PATIENT = "PATIENT";

    // Статусы приёмов
    public static final String STATUS_SCHEDULED = "Запланирован";
    public static final String STATUS_COMPLETED = "Завершён";
    public static final String STATUS_CANCELLED = "Отменён";
    public static final String STATUS_CONFIRMED = "Подтверждён";
    public static final List<String> FORBIDDEN_PAST_STATUSES =
            Arrays.asList(STATUS_SCHEDULED, STATUS_CONFIRMED);

    // Форматтеры даты/времени
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");
    public static final String TIME_PATTERN = "^([01]?\\d|2[0-3]):[0-5]\\d$";

    // SQL
    public static final String SELECT_ALL_DOCTORS = "SELECT * FROM Doctors";
}