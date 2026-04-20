package com.shop.shop.service.logistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogisticsFactory {

    private final List<LogisticsService> logisticsServices;

    private final Map<String, LogisticsService> serviceCache = new ConcurrentHashMap<>();

    public LogisticsService getService(String providerCode) {
        if (providerCode == null || providerCode.isBlank()) {
            return null;
        }

        String code = providerCode.toUpperCase().trim();

        return serviceCache.computeIfAbsent(code, k -> {
            return logisticsServices.stream()
                    .filter(s -> k.equals(s.getProviderCode().toUpperCase()))
                    .findFirst()
                    .orElse(null);
        });
    }

    public LogisticsService getAvailableService(String providerCode) {
        LogisticsService service = getService(providerCode);
        if (service != null && service.isAvailable()) {
            return service;
        }
        return null;
    }

    public List<LogisticsService> getAllServices() {
        return logisticsServices;
    }

    public List<LogisticsService> getAvailableServices() {
        return logisticsServices.stream()
                .filter(LogisticsService::isAvailable)
                .toList();
    }
}
