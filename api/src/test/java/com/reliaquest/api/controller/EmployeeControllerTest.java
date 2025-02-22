package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.dto.EmployeeRequestDTO;
import com.reliaquest.api.dto.EmployeeResponseDTO;
import com.reliaquest.api.service.EmployeeServiceImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeServiceImpl employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private EmployeeResponseDTO sampleEmployee;
    private EmployeeRequestDTO sampleEmployeeRequest;
    private final UUID sampleUUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sampleEmployee = new EmployeeResponseDTO();
        // Set sample employee properties here

        sampleEmployeeRequest = new EmployeeRequestDTO();
        // Set sample request properties here
    }

    @Test
    void getAllEmployees_ShouldReturnListOfEmployees() {
        // Arrange
        List<EmployeeResponseDTO> expectedEmployees = Arrays.asList(sampleEmployee);
        when(employeeService.getAllEmployees()).thenReturn(expectedEmployees);

        // Act
        ResponseEntity<List<EmployeeResponseDTO>> response = employeeController.getAllEmployees();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedEmployees, response.getBody());
        verify(employeeService).getAllEmployees();
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnHighestSalary() {
        // Arrange
        int expectedSalary = 100000;
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(expectedSalary);

        // Act
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSalary, response.getBody());
        verify(employeeService).getHighestSalaryOfEmployees();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnListOfNames() {
        // Arrange
        List<String> expectedNames = Arrays.asList("John Doe", "Jane Smith");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(expectedNames);

        // Act
        ResponseEntity<List<String>> response = employeeController.getTop10HighestEarningEmployeeNames();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedNames, response.getBody());
        verify(employeeService).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void getEmployeeById_WhenEmployeeExists_ShouldReturnEmployee() {
        // Arrange
        when(employeeService.getEmployeeById(sampleUUID)).thenReturn(Optional.of(sampleEmployee));

        // Act
        ResponseEntity<EmployeeResponseDTO> response = employeeController.getEmployeeById(sampleUUID.toString());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleEmployee, response.getBody());
        verify(employeeService).getEmployeeById(sampleUUID);
    }

    @Test
    void getEmployeeById_WhenEmployeeDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        when(employeeService.getEmployeeById(sampleUUID)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<EmployeeResponseDTO> response = employeeController.getEmployeeById(sampleUUID.toString());

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(employeeService).getEmployeeById(sampleUUID);
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnMatchingEmployees() {
        // Arrange
        String searchString = "John";
        List<EmployeeResponseDTO> expectedEmployees = Arrays.asList(sampleEmployee);
        when(employeeService.getEmployeesByNameSearch(searchString)).thenReturn(expectedEmployees);

        // Act
        ResponseEntity<List<EmployeeResponseDTO>> response = employeeController.getEmployeesByNameSearch(searchString);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedEmployees, response.getBody());
        verify(employeeService).getEmployeesByNameSearch(searchString);
    }

    @Test
    void createEmployee_WhenValid_ShouldReturnCreatedEmployee() {
        // Arrange
        when(employeeService.createEmployee(sampleEmployeeRequest)).thenReturn(Optional.of(sampleEmployee));

        // Act
        ResponseEntity<EmployeeResponseDTO> response = employeeController.createEmployee(sampleEmployeeRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleEmployee, response.getBody());
        verify(employeeService).createEmployee(sampleEmployeeRequest);
    }

    @Test
    void createEmployee_WhenInvalid_ShouldReturnBadRequest() {
        // Arrange
        when(employeeService.createEmployee(sampleEmployeeRequest)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<EmployeeResponseDTO> response = employeeController.createEmployee(sampleEmployeeRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(employeeService).createEmployee(sampleEmployeeRequest);
    }

    @Test
    void deleteEmployeeById_WhenEmployeeExists_ShouldReturnSuccess() {
        // Arrange
        when(employeeService.deleteEmployeeById(sampleUUID)).thenReturn(true);

        // Act
        ResponseEntity<String> response = employeeController.deleteEmployeeById(sampleUUID.toString());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Employee deleted successfully.", response.getBody());
        verify(employeeService).deleteEmployeeById(sampleUUID);
    }

    @Test
    void deleteEmployeeById_WhenEmployeeDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        when(employeeService.deleteEmployeeById(sampleUUID)).thenReturn(false);

        // Act
        ResponseEntity<String> response = employeeController.deleteEmployeeById(sampleUUID.toString());

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(employeeService).deleteEmployeeById(sampleUUID);
    }
}
