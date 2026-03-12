package com.ftle.tracker.repository;

import com.ftle.tracker.entity.MarketView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketViewRepository extends JpaRepository<MarketView, Long> {

   List<MarketView> findTop20ByDraftFalseOrderByCreatedAtDesc();

    Page<MarketView> findByDraftFalse(Pageable pageable);

    List<MarketView> findByCreatedByAndDraftTrue(String createdBy);

     List<MarketView> findTop20ByOrderByCreatedAtDesc();

}
