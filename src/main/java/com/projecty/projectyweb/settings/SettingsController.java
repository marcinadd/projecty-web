package com.projecty.projectyweb.settings;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("settings")
public class SettingsController {
    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public Settings getSettings() {
        return settingsService.getSettingsForCurrentUser();
    }

    @PatchMapping
    public Settings patchSettings(@RequestBody Settings patchedSettings) {
        return settingsService.patchSettings(patchedSettings);
    }
}
