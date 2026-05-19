package com.foodly.backend.dto;

import com.foodly.backend.entity.Role;
import lombok.Data;

@Data
public class UpdateRoleRequest {

	private Role role;

}