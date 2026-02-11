package com.codingshuttle.TestingApp.repositories;
import static org.assertj.core.api.Assertions.assertThat;

import com.codingshuttle.TestingApp.TestContainerConfiguration;
import com.codingshuttle.TestingApp.entities.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@Testcontainers
@Import(TestContainerConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp(){
        employee=Employee.builder()
                .name("Shubham")
                .email("shubham@Gmail.com")
                .salary(100L)
                .build();
    }

    @Test
    void testFindByEmail_whenEmailIsPresent_thenReturnEmployee() {

    //Arrange, Given
    employeeRepository.save(employee);

    //Act, when
     List<Employee> employeeList=employeeRepository.findByEmail(employee.getEmail());

    //Assert, Then
     assertThat(employeeList).isNotNull();
     assertThat(employeeList).isNotEmpty();
     assertThat(employeeList.get(0).getEmail().equals(employee.getEmail()));


    }
    @Test
    void testFindByEmail_whenEmailIsNotFound_thenReturnEmptyEmployeeList() {
         //given
     String email = "notPresent.123@gmail.com";
        //when
        List<Employee> employeeList=employeeRepository.findByEmail(email);
        //then
        assertThat(employeeList).isNotNull();
        assertThat(employeeList).isEmpty();

    }
}