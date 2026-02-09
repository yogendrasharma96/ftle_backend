package com.ftle.tracker.config;

import com.ftle.tracker.filter.FirebaseAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class SecurityConfig {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public FilterRegistrationBean<FirebaseAuthFilter> firebaseFilter(
            FirebaseAuthFilter filter
    ) {
        FilterRegistrationBean<FirebaseAuthFilter> reg =
                new FilterRegistrationBean<>();

        reg.setFilter(filter);
        reg.addUrlPatterns("/api/*");
        return reg;
    }

        @Bean
        public CorsFilter corsFilter() {

            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.addAllowedOrigin(frontendUrl);
            config.addAllowedHeader("*");
            config.addAllowedMethod("*");

            UrlBasedCorsConfigurationSource source =
                    new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);

            return new CorsFilter(source);
        }
}
