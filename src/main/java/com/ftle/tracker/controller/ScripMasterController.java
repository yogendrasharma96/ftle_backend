package com.ftle.tracker.controller;

import com.ftle.tracker.dto.ScripMasterDto;
import com.ftle.tracker.service.ScripMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScripMasterController {

    private final ScripMasterService service;

    @GetMapping("/public/scrips")
    public List<ScripMasterDto> getAllScrips() {
        return service.getScripMasterData();
    }
}
