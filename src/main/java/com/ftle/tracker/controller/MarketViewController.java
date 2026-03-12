package com.ftle.tracker.controller;

import com.ftle.tracker.dto.MarketViewDto;
import com.ftle.tracker.entity.MarketView;
import com.ftle.tracker.service.MarketViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MarketViewController {

    private final MarketViewService service;

    @PostMapping("/private/market-views")
    public MarketView create(@RequestBody MarketViewDto dto){
        return service.create(dto);
    }

    @PostMapping("/private/market-views/draft")
    public MarketView saveDraft(@RequestBody MarketViewDto dto){
        return service.saveDraft(dto);
    }

    @PutMapping("/private/market-views/{id}")
    public MarketView update(@PathVariable("id") Long id,@RequestBody MarketViewDto dto){
        return service.update(id,dto);
    }

    @GetMapping("/public/market-views/latest")
    public List<MarketView> latest(){
        return service.getLatest();
    }

    @GetMapping("/public/market-views/latest/all")
    public List<MarketView> latestWithDraft(){
        return service.latestWithDraft();
    }

}
