package TNB.SmsGateway.utils;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Convertir Instant en String ISO
     */
    public static String toIsoString(Instant instant) {
        if (instant == null) return null;
        return instant.toString();
    }

    /**
     * Convertir String ISO en Instant
     */
    public static Instant fromIsoString(String isoString) {
        if (isoString == null || isoString.isEmpty()) return null;
        return Instant.parse(isoString);
    }

    /**
     * Convertir Date en Instant
     */
    public static Instant toInstant(Date date) {
        return date.toInstant();
    }

    /**
     * Convertir Instant en Date
     */
    public static Date toDate(Instant instant) {
        return Date.from(instant);
    }

    /**
     * Convertir LocalDateTime en Instant
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Convertir Instant en LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * Formatter une date
     */
    public static String format(Instant instant, String pattern) {
        if (instant == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(instant.atZone(ZoneId.systemDefault()));
    }

    /**
     * Vérifier si une date est dans le passé
     */
    public static boolean isPast(Instant instant) {
        return instant.isBefore(Instant.now());
    }

    /**
     * Vérifier si une date est dans le futur
     */
    public static boolean isFuture(Instant instant) {
        return instant.isAfter(Instant.now());
    }

    /**
     * Ajouter des minutes à une date
     */
    public static Instant plusMinutes(Instant instant, long minutes) {
        return instant.plusSeconds(minutes * 60);
    }

    /**
     * Ajouter des heures à une date
     */
    public static Instant plusHours(Instant instant, long hours) {
        return instant.plusSeconds(hours * 3600);
    }

    /**
     * Ajouter des jours à une date
     */
    public static Instant plusDays(Instant instant, long days) {
        return instant.plusSeconds(days * 86400);
    }

    /**
     * Calculer la différence en minutes entre deux dates
     */
    public static long diffInMinutes(Instant start, Instant end) {
        return (end.getEpochSecond() - start.getEpochSecond()) / 60;
    }

    /**
     * Calculer la différence en heures entre deux dates
     */
    public static long diffInHours(Instant start, Instant end) {
        return (end.getEpochSecond() - start.getEpochSecond()) / 3600;
    }

    /**
     * Début de la journée
     */
    public static Instant startOfDay(Instant instant) {
        LocalDateTime date = instant.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
        return date.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Fin de la journée
     */
    public static Instant endOfDay(Instant instant) {
        LocalDateTime date = instant.atZone(ZoneId.systemDefault())
                .toLocalDate().atTime(23, 59, 59);
        return date.atZone(ZoneId.systemDefault()).toInstant();
    }
}