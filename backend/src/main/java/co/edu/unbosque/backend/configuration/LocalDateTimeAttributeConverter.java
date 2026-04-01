package co.edu.unbosque.backend.configuration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Converter para persistir LocalDateTime como texto y evitar problemas de parseo
 * del driver SQLite con columnas timestamp.
 * La aplicacion maneja un unico formato: ISO_LOCAL_DATE_TIME.
 *
 * @author Sebastian Cardenas Garcia
 */
@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : FORMATTER.format(attribute);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dbData.trim(), FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "No fue posible convertir la fecha-hora almacenada al formato ISO_LOCAL_DATE_TIME: " + dbData,
                    ex
            );
        }
    }
}
