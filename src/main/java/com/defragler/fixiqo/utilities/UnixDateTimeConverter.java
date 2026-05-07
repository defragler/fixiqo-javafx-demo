package com.defragler.fixiqo.utilities;

import com.defragler.fixiqo.exceptions.*;

import com.defragler.fixiqo.exceptions.enums.*;
import java.time.*;

/**
 * Utility class for converting between Unix time (seconds since 1970-01-01 UTC)
 * and Java date-time representations.
 *
 * <p>This class is stateless and thread-safe.</p>
 * <p>All conversions are performed in UTC.</p>
 */
public final class UnixDateTimeConverter {

    private UnixDateTimeConverter() {}

    /**
     * Converts a {@link LocalDateTime} to Unix time (seconds since epoch).
     *
     * @param dateTime the date-time to convert; may be {@code null}
     * @return Unix time in seconds, or {@code null} if input is {@code null}
     * @throws UtilityException if conversion fails
     */
    public static Long toUnixSeconds(LocalDateTime dateTime) {
        try {
            if (dateTime == null) {
                return null;
            }
            return dateTime.toEpochSecond(ZoneOffset.UTC);
        } catch (Exception ex) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to convert LocalDateTime to Unix time", ex);
        }
    }

    /**
     * Converts an {@link Instant} to Unix time (seconds since epoch).
     *
     * @param instant the instant to convert; may be {@code null}
     * @return Unix time in seconds, or {@code null} if input is {@code null}
     * @throws UtilityException if conversion fails
     */
    public static Long toUnixSeconds(Instant instant) {
        try {
            if (instant == null) {
                return null;
            }
            return instant.getEpochSecond();
        } catch (Exception ex) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to convert Instant to Unix time", ex);
        }
    }

    /**
     * Converts Unix time (seconds since epoch) to {@link LocalDateTime} in UTC.
     *
     * @param unixSeconds Unix time in seconds; may be {@code null}
     * @return corresponding {@link LocalDateTime} in UTC, or {@code null} if input is {@code null}
     * @throws UtilityException if conversion fails
     */
    public static LocalDateTime fromUnixSeconds(Long unixSeconds) {
        try {
            if (unixSeconds == null) {
                return null;
            }
            return LocalDateTime.ofInstant(
                  Instant.ofEpochSecond(unixSeconds),
                  ZoneOffset.UTC
            );
        } catch (Exception ex) {
            throw new UtilityException(ExceptionLevel.ERROR,"Failed to convert Unix time to LocalDateTime", ex);
        }
    }
}
