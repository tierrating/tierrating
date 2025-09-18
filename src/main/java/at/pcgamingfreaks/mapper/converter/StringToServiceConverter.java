package at.pcgamingfreaks.mapper.converter;

import at.pcgamingfreaks.model.Service;
import org.springframework.core.convert.converter.Converter;

public class StringToServiceConverter implements Converter<String, Service> {
    @Override
    public Service convert(String source) {
        return Service.valueOf(source.toUpperCase());
    }
}
