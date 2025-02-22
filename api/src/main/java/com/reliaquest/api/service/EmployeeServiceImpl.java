package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeListResponse;
import com.reliaquest.api.dto.EmployeeRequestDTO;
import com.reliaquest.api.dto.EmployeeResponseDTO;
import com.reliaquest.api.dto.SingleEmployeeResponse;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements IEmployeeService {
    private final RestTemplate restTemplate;

    @Value("${com.reliaquest.employee.api.base-url}")
    private String SERVER_URL;

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 30_500;

    private <T> ResponseEntity<T> executeWithRetries(RequestExecutor<T> executor, String operation) {
        int retryCount = 0;
        long currentDelay = RETRY_DELAY_MS;

        while (retryCount < MAX_RETRIES) {
            try {
                return executor.execute();
            } catch (HttpClientErrorException.TooManyRequests e) {
                retryCount++;
                log.warn(
                        "Received 429 Too Many Requests during {}. Retrying {}/{} ...",
                        operation,
                        retryCount,
                        MAX_RETRIES);
                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(currentDelay);
                        currentDelay *= 1.5; // Modify local variable instead of static field
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrupted while waiting to retry.");
                        throw new RuntimeException("Server overloaded. Please try again later.");
                    }
                }
            }
        }
        throw new RuntimeException("Too many requests. Failed after " + MAX_RETRIES + " retries.");
    }

    @FunctionalInterface
    private interface RequestExecutor<T> {
        ResponseEntity<T> execute();
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        ResponseEntity<EmployeeListResponse> response = executeWithRetries(
                () -> restTemplate.getForEntity(SERVER_URL, EmployeeListResponse.class), "fetching all employees");
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<EmployeeResponseDTO> employees = response.getBody().getData();
            log.info("Successfully retrieved {} employees", employees.size());
            return employees;
        } else {
            log.error("Failed to retrieve employees: {}", response.getStatusCode());
            throw new RuntimeException("Failed to retrieve employees: " + response.getStatusCode());
        }
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByNameSearch(String searchString) {
        log.info("Retrieving Employee {}", searchString);
        return getAllEmployees().stream()
                .filter(emp -> emp.getEmployeeName() != null
                        && emp.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        return getAllEmployees().stream()
                .mapToInt(EmployeeResponseDTO::getEmployeeSalary)
                .max()
                .orElse(0);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return getAllEmployees().stream()
                .sorted((a, b) -> Integer.compare(b.getEmployeeSalary(), a.getEmployeeSalary()))
                .limit(10)
                .map(EmployeeResponseDTO::getEmployeeName)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EmployeeResponseDTO> getEmployeeById(UUID id) {
        ResponseEntity<SingleEmployeeResponse> response = executeWithRetries(
                () -> restTemplate.getForEntity(SERVER_URL + "/" + id, SingleEmployeeResponse.class),
                "fetching employee by ID");

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            log.info("Successfully retrieved employee : "
                    + response.getBody().getData().getId());
            return Optional.ofNullable(response.getBody().getData());
        } else {
            log.error("Failed to retrieve employee with ID {}: {}", id, response.getStatusCode());
            return Optional.empty();
        }
    }

    @Override
    public Optional<EmployeeResponseDTO> createEmployee(EmployeeRequestDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeRequestDTO> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<SingleEmployeeResponse> response = executeWithRetries(
                    () -> restTemplate.postForEntity(SERVER_URL, entity, SingleEmployeeResponse.class),
                    "creating employee");

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                SingleEmployeeResponse employee = response.getBody();

                if (employee.getData().getId() == null || employee.getData().getEmployeeName() == null) {
                    log.error("Invalid employee response: {}", employee);
                    return Optional.empty();
                }

                log.info("Successfully created employee: {}", employee);
                return Optional.of(response.getBody().getData());
            } else {
                log.error(
                        "Unexpected response while creating employee: {} - {}",
                        response.getStatusCode(),
                        response.getBody());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Unexpected error while creating employee: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteEmployeeById(UUID id) {
        return getEmployeeById(id)
                .map(employee -> {
                    try {
                        Map<String, String> requestBody = new HashMap<>();
                        requestBody.put("name", employee.getEmployeeName());

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

                        executeWithRetries(
                                () -> restTemplate.exchange(SERVER_URL, HttpMethod.DELETE, requestEntity, Void.class),
                                "deleting employee");

                        log.info("Successfully deleted employee with ID {}", id);
                        return true;
                    } catch (Exception e) {
                        log.error("Failed to delete employee with ID {}: {}", id, e.getMessage());
                        return false;
                    }
                })
                .orElseGet(() -> {
                    log.error("Employee with ID {} not found.", id);
                    return false;
                });
    }
}
