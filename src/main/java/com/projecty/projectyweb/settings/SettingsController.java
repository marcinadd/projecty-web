package com.projecty.projectyweb.settings;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("settings")
public class SettingsController {
    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<Settings> getSettings() {
        return new ResponseEntity<>(settingsService.getSettingsForCurrentUser(), HttpStatus.OK);
    }

    @PatchMapping
    public ResponseEntity<Settings> patchSettings(@RequestBody Settings patchedSettings) {
        return new ResponseEntity<>(settingsService.patchSettings(patchedSettings), HttpStatus.OK);
    }
}
