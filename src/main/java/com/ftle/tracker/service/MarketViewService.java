package com.ftle.tracker.service;

import com.ftle.tracker.dto.MarketViewDto;
import com.ftle.tracker.entity.MarketView;

import java.util.List;

public interface MarketViewService {

    MarketView create(MarketViewDto dto);

    MarketView saveDraft(MarketViewDto dto);

    MarketView update(Long id, MarketViewDto dto);

    void publish(Long id);

    MarketView getById(Long id);

    List<MarketView> getLatest();

    void delete(Long id);

    List<MarketView> latestWithDraft();
}
