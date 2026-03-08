package com.ftle.tracker.config;

import com.ftle.tracker.dto.UserDetails;
import com.ftle.tracker.interceptor.UserContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        UserDetails user = UserContext.get();
        String uid = user.getUid();
        return Optional.of(uid);

    }
}
