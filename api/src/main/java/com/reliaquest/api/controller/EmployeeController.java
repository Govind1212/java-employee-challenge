package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeRequestDTO;
import com.reliaquest.api.dto.EmployeeResponseDTO;
import com.reliaquest.api.service.EmployeeServiceImpl;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {
    private final EmployeeServiceImpl service;

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(service.getAllEmployees());
    }

    @GetMapping("/highestSalary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(service.getHighestSalaryOfEmployees());
    }

    @GetMapping("/topTenHighestEarningEmployeeNames")
    public ResponseEntity<List<String>> getTop10HighestEarningEmployeeNames() {
        return ResponseEntity.ok(service.getTopTenHighestEarningEmployeeNames());
    }

    @GetMapping("/{id:[0-9a-fA-F-]{36}}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable(name = "id", required = true) String id) {
        return service.getEmployeeById(UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/{searchString}")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByNameSearch(
            @PathVariable(name = "searchString", required = true) String searchString) {
        return ResponseEntity.ok(service.getEmployeesByNameSearch(searchString));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO employeeInput) {
        log.info("Received payload: {}", employeeInput);
        return service.createEmployee(employeeInput)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable(name = "id", required = true) String id) {
        return service.deleteEmployeeById(UUID.fromString(id))
                ? ResponseEntity.ok("Employee deleted successfully.")
                : ResponseEntity.notFound().build();
    }
}
