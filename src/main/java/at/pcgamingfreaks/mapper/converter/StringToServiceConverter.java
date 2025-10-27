package at.pcgamingfreaks.mapper.converter;

import at.pcgamingfreaks.model.ThirdPartyService;
import org.springframework.core.convert.converter.Converter;

public class StringToServiceConverter implements Converter<String, ThirdPartyService> {
    @Override
    public ThirdPartyService convert(String source) {
        return ThirdPartyService.valueOf(source.toUpperCase());
    }
}
