package com.reliaquest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleEmployeeResponse {
    private EmployeeResponseDTO data;
    private String status;
}
