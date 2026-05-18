package com.foodly.backend.controller;

import com.foodly.backend.dto.UpdateRoleRequest;
import com.foodly.backend.entity.User;
import com.foodly.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable UUID id, @RequestBody UpdateRoleRequest request) {
        adminService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok("User role updated successfully");
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<String> banUser(@PathVariable UUID id) {
        adminService.toggleUserBan(id, true);
        return ResponseEntity.ok("User has been banned");
    }

    @PutMapping("/users/{id}/unban")
    public ResponseEntity<String> unbanUser(@PathVariable UUID id) {
        adminService.toggleUserBan(id, false);
        return ResponseEntity.ok("User has been unbanned");
    }
}