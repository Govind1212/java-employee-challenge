package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {
    @NotBlank(message = "Name cannot be blank")
    @JsonProperty("name")
    private String employeeName;

    @Positive(message = "Salary must be greater than zero") @JsonProperty("salary")
    private int employeeSalary;

    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age cannot be greater than 75")
    @JsonProperty("age")
    private int employeeAge;

    @NotBlank(message = "Title cannot be blank")
    @JsonProperty("title")
    private String employeeTitle;
}
