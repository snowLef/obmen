package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.Setting;
import org.example.model.enums.SettingKey;
import org.example.repository.SettingRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository repository;

    public String get(SettingKey key) {
        return repository.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Настройка %s не найдена".formatted(key)))
                .getValue();
    }

    public long getLong(SettingKey key) {
        return Long.parseLong(get(key));
    }

    public void set(SettingKey key, String value) {
        Setting setting = repository.findByKey(key)
                .orElse(new Setting(null, key, value));
        setting.setValue(value);
        repository.save(setting);
    }
}
