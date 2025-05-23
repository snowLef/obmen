package org.example.repository;

import org.example.model.Setting;
import org.example.model.enums.SettingKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByKey(SettingKey key);
}
