package com.codingshuttle.TestingApp.controllers;
import com.codingshuttle.TestingApp.dto.EmployeeDto;
import com.codingshuttle.TestingApp.entities.Employee;
import com.codingshuttle.TestingApp.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;


class EmployeeControllerTestIT extends AbstractIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private EmployeeDto testEmployeeDto;
    private Employee testEmployee;

    @BeforeEach
    void setUp(){
        testEmployee = Employee.builder()
                .name("Shubham")
                .email("shubham@gmail.com")
                .salary(200L)
                .build();
        testEmployeeDto = EmployeeDto.builder()
                .name("Shubham")
                .email("shubham@gmail.com")
                .salary(200L)
                .build();
        employeeRepository.deleteAll();
    }

    @Test
    void testGetEmployeeById_success(){
        Employee savedEmployee=employeeRepository.save(testEmployee);
        webTestClient.get()
                .uri("/employees/{id}", savedEmployee.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedEmployee.getId())
                .jsonPath("$.email").isEqualTo(savedEmployee.getEmail());

    }

    @Test
    void testGetEmployeeById_Failure(){
        webTestClient.get()
                .uri("/employees/1")
                .exchange()
                .expectStatus().isNotFound();

    }

    @Test
    void testCreateNewEmployee_WhenEmployeeAlreadyExists_ThenThrowException(){
        Employee savedEmployee=employeeRepository.save(testEmployee);

        webTestClient.post()
                .uri("/employees")
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testCreateNewEmployee_WhenEmployeeDoesNotExists_ThenCreateNewEmployee(){
        webTestClient.post()
                .uri("/employees")
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.email").isEqualTo(testEmployeeDto.getEmail())
                .jsonPath("$.name").isEqualTo(testEmployeeDto.getName());
    }

    @Test
    void testUpdateEmployee_whenEmployeeDoesNotExist_thenThrowException(){
        webTestClient.put()
                .uri("/employees/999")
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateEmployee_whenAttemptingToUpdateTheEmail_thenThrowException(){
        Employee savedEmployee = employeeRepository.save(testEmployee);
        testEmployeeDto.setName("random name");
        testEmployeeDto.setEmail("random@gmail.com");

        webTestClient.put()
                .uri("/employees/{id}", savedEmployee.getId())
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testUpdateEmployee_whenEmployeeIsValid_thenUpdateEmployee(){
        Employee savedEmployee = employeeRepository.save(testEmployee);
        testEmployeeDto.setName("random name");
        testEmployeeDto.setSalary(250L);
        testEmployeeDto.setId(1L);

        webTestClient.put()
                .uri("/employees/{id}", savedEmployee.getId() )
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmployeeDto.class)
                .isEqualTo(testEmployeeDto);
    }

    @Test
    void testDeleteEmployee_whenEmployeeDoesNotExists_thenThrowException(){
        webTestClient.delete()
                .uri("/employees/111")
                .exchange()
                .expectStatus().isNotFound();

    }

    @Test
    void testDeleteEmployee_whenEmployeeExists_thenThrowException(){
        Employee savedEmployee = employeeRepository.save(testEmployee);
        webTestClient.delete()
                .uri("/employees/{id}", savedEmployee.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

    }



}