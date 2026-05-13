package com.foodly.backend.controller;

import com.foodly.backend.dto.HealthProfileDto;
import com.foodly.backend.entity.HealthProfile;
import com.foodly.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody HealthProfileDto dto, Authentication authentication) {
        HealthProfile updated = profileService.updateProfile(dto, authentication.getName());

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "dailyCalories", updated.getDailyCalorieIntake().setScale(0, BigDecimal.ROUND_HALF_UP),
                "userId", updated.getId()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        log.info("Fetching profile for: {}", authentication.getName());
        return ResponseEntity.ok(profileService.getProfileByEmail(authentication.getName()));
    }
}
