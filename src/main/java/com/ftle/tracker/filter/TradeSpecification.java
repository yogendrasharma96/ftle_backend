package com.ftle.tracker.filter;

import com.ftle.tracker.entity.Trade;
import org.springframework.data.jpa.domain.Specification;

public class TradeSpecification {

    public static Specification<Trade> filterBy(String financialYear, String status) {
        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            if (financialYear != null && !financialYear.equalsIgnoreCase("ALL")) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("financialYear"), financialYear));
            }

            if (status != null && !status.equalsIgnoreCase("ALL")) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), status));
            }

            return predicates;
        };
    }
}
