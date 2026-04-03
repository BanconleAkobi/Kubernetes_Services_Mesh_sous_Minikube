package com.insa.tp_archi_step_1;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@RestController
@RequestMapping("/monservice")
public class TpArchiStep1Controller {

    @GetMapping("/echo")
    public Map<String, String> echo(@RequestParam String nom) {
        return Map.of("message", "Echo : " + nom);
    }

    @PostMapping("/hello")
    public Map<String, String> hello(@RequestBody Map<String, String> body) {
        String nom = body.getOrDefault("nom", "inconnu");
        return Map.of("message", "Hello " + nom + " !");
    }
}