package com.foodly.backend.service;

import com.foodly.backend.dto.HealthProfileDto;
import com.foodly.backend.entity.HealthProfile;
import com.foodly.backend.entity.User;
import com.foodly.backend.repository.HealthProfileRepository;
import com.foodly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final HealthProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final NutritionService nutritionService;

    @Transactional
    public HealthProfile updateProfile(HealthProfileDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        HealthProfile profile = user.getHealthProfile();

        if (profile == null) {
            profile = new HealthProfile();
            profile.setId(user.getId());
            profile.setUser(user);
            user.setHealthProfile(profile);
        }

        profile.setAge(dto.getAge());
        profile.setWeight(dto.getWeight());
        profile.setHeight(dto.getHeight());
        profile.setGender(dto.getGender());
        profile.setActivityMultiplier(dto.getActivityMultiplier());
        profile.setTarget(dto.getTarget());
        profile.setAllergens(dto.getAllergens());
        BigDecimal dci = nutritionService.calculateFullDci(profile);
        profile.setDailyCalorieIntake(dci);

        userRepository.save(user);

        return user.getHealthProfile();
    }

    @Transactional(readOnly = true)
    public HealthProfile getProfileByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getHealthProfile)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + email));
    }
}