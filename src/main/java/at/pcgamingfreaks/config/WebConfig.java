package at.pcgamingfreaks.config;

import at.pcgamingfreaks.mapper.converter.StringToContentTypeConverter;
import at.pcgamingfreaks.mapper.converter.StringToServiceConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToServiceConverter());
        registry.addConverter(new StringToContentTypeConverter());
    }
}
