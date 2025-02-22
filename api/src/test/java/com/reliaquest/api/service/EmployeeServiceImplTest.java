package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.reliaquest.api.dto.EmployeeListResponse;
import com.reliaquest.api.dto.EmployeeRequestDTO;
import com.reliaquest.api.dto.EmployeeResponseDTO;
import com.reliaquest.api.dto.SingleEmployeeResponse;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    private static final String BASE_URL = "http://test-api.com/employees";

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeResponseDTO sampleEmployee;
    private EmployeeRequestDTO sampleRequest;
    private final UUID sampleUUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(employeeService, "SERVER_URL", BASE_URL);

        sampleEmployee = new EmployeeResponseDTO();
        sampleEmployee.setId(sampleUUID);
        sampleEmployee.setEmployeeName("John Doe");
        sampleEmployee.setEmployeeSalary(100000);
        sampleEmployee.setEmployeeAge(30);

        sampleRequest = new EmployeeRequestDTO();
        sampleRequest.setEmployeeName("John Doe");
        sampleRequest.setEmployeeSalary(10000);
        sampleRequest.setEmployeeAge(30);
    }

    @Test
    void getAllEmployees_Success() {

        EmployeeListResponse mockResponse = new EmployeeListResponse();
        mockResponse.setData(Arrays.asList(sampleEmployee));

        when(restTemplate.getForEntity(BASE_URL, EmployeeListResponse.class))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        List<EmployeeResponseDTO> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleEmployee.getEmployeeName(), result.get(0).getEmployeeName());
        verify(restTemplate).getForEntity(BASE_URL, EmployeeListResponse.class);
    }

    @Test
    void getEmployeesByNameSearch_Success() {

        EmployeeListResponse mockResponse = new EmployeeListResponse();
        mockResponse.setData(Arrays.asList(sampleEmployee));

        when(restTemplate.getForEntity(BASE_URL, EmployeeListResponse.class))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        List<EmployeeResponseDTO> result = employeeService.getEmployeesByNameSearch("John");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getEmployeeName().contains("John"));
    }

    @Test
    void getHighestSalaryOfEmployees_Success() {

        EmployeeResponseDTO employee2 = new EmployeeResponseDTO();
        employee2.setEmployeeSalary(150000);

        EmployeeListResponse mockResponse = new EmployeeListResponse();
        mockResponse.setData(Arrays.asList(sampleEmployee, employee2));

        when(restTemplate.getForEntity(BASE_URL, EmployeeListResponse.class))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Integer result = employeeService.getHighestSalaryOfEmployees();

        assertEquals(150000, result);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {

        List<EmployeeResponseDTO> employees = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            EmployeeResponseDTO emp = new EmployeeResponseDTO();
            emp.setEmployeeName("Employee " + i);
            emp.setEmployeeSalary(100000 + (i * 10000));
            employees.add(emp);
        }

        EmployeeListResponse mockResponse = new EmployeeListResponse();
        mockResponse.setData(employees);

        when(restTemplate.getForEntity(BASE_URL, EmployeeListResponse.class))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(10, result.size());
        assertEquals("Employee 11", result.get(0)); // Highest salary
    }

    @Test
    void getEmployeeById_Success() {

        SingleEmployeeResponse mockResponse = new SingleEmployeeResponse();
        mockResponse.setData(sampleEmployee);

        when(restTemplate.getForEntity(BASE_URL + "/" + sampleUUID, SingleEmployeeResponse.class))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Optional<EmployeeResponseDTO> result = employeeService.getEmployeeById(sampleUUID);

        assertTrue(result.isPresent());
        assertEquals(sampleEmployee.getId(), result.get().getId());
    }

    @Test
    void createEmployee_Success() {

        SingleEmployeeResponse mockResponse = new SingleEmployeeResponse();
        mockResponse.setData(sampleEmployee);

        when(restTemplate.postForEntity(eq(BASE_URL), any(HttpEntity.class), eq(SingleEmployeeResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Optional<EmployeeResponseDTO> result = employeeService.createEmployee(sampleRequest);

        assertTrue(result.isPresent());
        assertEquals(sampleEmployee.getEmployeeName(), result.get().getEmployeeName());
    }

    @Test
    void deleteEmployeeById_Success() {

        SingleEmployeeResponse getResponse = new SingleEmployeeResponse();
        getResponse.setData(sampleEmployee);

        when(restTemplate.getForEntity(BASE_URL + "/" + sampleUUID, SingleEmployeeResponse.class))
                .thenReturn(new ResponseEntity<>(getResponse, HttpStatus.OK));

        when(restTemplate.exchange(eq(BASE_URL), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        boolean result = employeeService.deleteEmployeeById(sampleUUID);

        assertTrue(result);
        verify(restTemplate).exchange(eq(BASE_URL), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void deleteEmployeeById_NotFound() {

        when(restTemplate.getForEntity(BASE_URL + "/" + sampleUUID, SingleEmployeeResponse.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        boolean result = employeeService.deleteEmployeeById(sampleUUID);

        assertFalse(result);
        verify(restTemplate, never())
                .exchange(eq(BASE_URL), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
    }
}
