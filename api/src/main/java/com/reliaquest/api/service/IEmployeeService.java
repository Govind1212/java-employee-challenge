package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeRequestDTO;
import com.reliaquest.api.dto.EmployeeResponseDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IEmployeeService {
    List<EmployeeResponseDTO> getAllEmployees();

    List<EmployeeResponseDTO> getEmployeesByNameSearch(String searchString);

    Optional<EmployeeResponseDTO> getEmployeeById(UUID id);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    Optional<EmployeeResponseDTO> createEmployee(EmployeeRequestDTO employeeInput);

    boolean deleteEmployeeById(UUID id);
}
