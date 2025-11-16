package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.ThirdPartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final Pattern configValidPattern = Pattern.compile("is(?<service>.*)ConfigValid");

    @GetMapping("services")
    public ResponseEntity<List<String>> getAvailableServices() {
        List<ThirdPartyService> services = new ArrayList<>();
        Method[] methods = thirdPartyConfig.getClass().getMethods();
        for (Method method:  methods) {
            Matcher matcher = configValidPattern.matcher(method.getName());
            if (matcher.matches()) {
                services.add(ThirdPartyService.from(matcher.group("service")));
            }
        }
        return ResponseEntity.ok(services.stream().map(s -> s.name().toLowerCase()).toList());
    }
}

