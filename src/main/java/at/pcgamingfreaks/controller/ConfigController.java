package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.config.ServiceConfig;
import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.ThirdPartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {
    private final ThirdPartyConfig thirdPartyConfig;
    private final Pattern configValidPattern = Pattern.compile("get(?<service>.*)");

    @GetMapping("services")
    public ResponseEntity<List<String>> getAvailableServices() throws InvocationTargetException, IllegalAccessException {
        List<ThirdPartyService> services = new ArrayList<>();
        Method[] methods = thirdPartyConfig.getClass().getMethods();
        for (Method method:  methods) {
            if (method.getReturnType().equals(ServiceConfig.class)) {
                ServiceConfig serviceConfig = (ServiceConfig) method.invoke(thirdPartyConfig);
                Matcher matcher = configValidPattern.matcher(method.getName());
                if (serviceConfig.isValid() && matcher.find()) {
                    services.add(ThirdPartyService.from(matcher.group("service")));
                }
            }
        }
        return ResponseEntity.ok(services.stream().map(s -> s.name().toLowerCase()).toList());
    }
}

