package com.ftle.tracker.service.impl;

import com.ftle.tracker.dto.MarketViewDto;
import com.ftle.tracker.entity.MarketView;
import com.ftle.tracker.repository.MarketViewRepository;
import com.ftle.tracker.service.MarketViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MarketViewServiceImpl implements MarketViewService {

    private final MarketViewRepository repository;

    @Override
    public MarketView create(MarketViewDto dto) {

        MarketView view = new MarketView();

        view.setTitle(dto.getTitle());
        view.setContent(dto.getContent());
        view.setTags(dto.getTags());
        view.setSentiment(dto.getSentiment());
        view.setDraft(dto.isDraft());

        view.setCreatedAt(LocalDateTime.now());
        view.setUpdatedAt(LocalDateTime.now());

        return repository.save(view);
    }

    @Override
    public MarketView saveDraft(MarketViewDto dto) {

        MarketView view = new MarketView();

        view.setTitle(dto.getTitle());
        view.setContent(dto.getContent());
        view.setTags(dto.getTags());
        view.setSentiment(dto.getSentiment());

        view.setDraft(true);

        view.setCreatedAt(LocalDateTime.now());
        view.setUpdatedAt(LocalDateTime.now());

        return repository.save(view);
    }

    @Override
    public MarketView update(Long id, MarketViewDto dto) {

        MarketView view = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Market view not found"));

        view.setTitle(dto.getTitle());
        view.setContent(dto.getContent());
        view.setTags(dto.getTags());
        view.setSentiment(dto.getSentiment());
        view.setDraft(dto.isDraft());

        view.setUpdatedAt(LocalDateTime.now());

        return repository.save(view);
    }

    @Override
    public void publish(Long id) {

        MarketView view = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Market view not found"));

        view.setDraft(false);
        view.setUpdatedAt(LocalDateTime.now());

        repository.save(view);
    }

    @Override
    @Transactional(readOnly = true)
    public MarketView getById(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Market view not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketView> getLatest() {

        return repository.findTop20ByDraftFalseOrderByCreatedAtDesc();
    }

    @Override
    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new RuntimeException("Market view not found");
        }

        repository.deleteById(id);
    }

    @Override
    public List<MarketView> latestWithDraft() {
         return repository.findTop20ByOrderByCreatedAtDesc();
    }
}
