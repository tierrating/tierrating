package at.pcgamingfreaks.mapper.converter;

import at.pcgamingfreaks.model.ContentType;
import org.springframework.core.convert.converter.Converter;

public class StringToContentTypeConverter implements Converter<String, ContentType> {
    @Override
    public ContentType convert(String source) {
        return ContentType.valueOf(source.toUpperCase());
    }
}
