package com.codingshuttle.TestingApp.services.impl;
import com.codingshuttle.TestingApp.TestContainerConfiguration;
import com.codingshuttle.TestingApp.dto.EmployeeDto;
import com.codingshuttle.TestingApp.entities.Employee;
import com.codingshuttle.TestingApp.exceptions.ResourceNotFoundException;
import com.codingshuttle.TestingApp.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfiguration.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee mockEmployee;
    private EmployeeDto mockEmployeeDto;

    @BeforeEach
    void setup(){
          mockEmployee = Employee.builder()
                .id(1L)
                .name("Shubham")
                .email("shubham@gmail.com")
                .salary(200L)
                .build();

        mockEmployeeDto = modelMapper.map(mockEmployee, EmployeeDto.class);
    }


    @Test
    void TestGetEmployeeById_WhenIdIsPresent_ThenReturnEmployeeDto(){
//        assign
        Long id= mockEmployee.getId();

        when(employeeRepository.findById(id)).thenReturn(Optional.of(mockEmployee));

//       act
        EmployeeDto employeeDto = employeeService.getEmployeeById(id);

//        assert
        assertThat(employeeDto).isNotNull();
        assertThat(employeeDto.getId()).isEqualTo(id);
        assertThat(employeeDto.getEmail()).isEqualTo(mockEmployee.getEmail());
        verify(employeeRepository, times(1)).findById(id);
    }

    @Test
    void TestGetEmployeeById_WhenIdIsNotPresent_ThenThrowException(){
//        arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

//       act and assert
        assertThatThrownBy(()->employeeService.getEmployeeById(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id: 2");

        verify(employeeRepository).findById(2L);
    }

    @Test
    void  testCreateNewEmployee_WhenValidEmployee_ThenCreateNewEmployee(){
//        assign
        when(employeeRepository.findByEmail(anyString())).thenReturn(List.of());
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

//        act
        EmployeeDto employeeDto = employeeService.createNewEmployee(mockEmployeeDto);

//        assert
        assertThat(employeeDto).isNotNull();
        assertThat(employeeDto.getEmail()).isEqualTo(mockEmployeeDto.getEmail());

        ArgumentCaptor<Employee> employeeArgumentCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeArgumentCaptor.capture());

        Employee capturedEmployee = employeeArgumentCaptor.getValue();
        assertThat(capturedEmployee.getEmail()).isEqualTo(mockEmployee.getEmail());

    }

    @Test
    void  testCreateNewEmployee_WhenAttemptingToCreateEmployeeWithExistingEmail_ThenThrowException(){
//        arrange
        when(employeeRepository.findByEmail(anyString())).thenReturn(List.of(mockEmployee));

//        act and assert
        assertThatThrownBy(()-> employeeService.createNewEmployee(mockEmployeeDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Employee already exists with email: "+mockEmployeeDto.getEmail());
        verify(employeeRepository).findByEmail(anyString());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testUpdateEmployee_whenEmployeeDoesNotExist_thenThrowException(){
//        arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
//        act and assert
        assertThatThrownBy(()-> employeeService.updateEmployee(1L, mockEmployeeDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id: 1");

        verify(employeeRepository).findById(1L);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testUpdateEmployee_whenAttemptingToUpdateEmail_thenThrowException(){
//        arrange
        when(employeeRepository.findById(mockEmployeeDto.getId())).thenReturn(Optional.of(mockEmployee));
        mockEmployeeDto.setName("Random");
        mockEmployeeDto.setEmail("random@gmail.com");

//        act and assert
        assertThatThrownBy(()-> employeeService.updateEmployee(mockEmployeeDto.getId(), mockEmployeeDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("The email of the employee cannot be updated");

        verify(employeeRepository).findById(mockEmployeeDto.getId());
        verify(employeeRepository, never()).save(any());

    }
    @Test
    void testUpdateEmployee_whenValidEmployee_thenUpdateEmployee(){
//        arrange
          when(employeeRepository.findById(mockEmployeeDto.getId())).thenReturn(Optional.of(mockEmployee));
          mockEmployeeDto.setName("Ramdom");
          mockEmployeeDto.setSalary(500L);

          Employee newEmployee = modelMapper.map(mockEmployeeDto, Employee.class);
          when(employeeRepository.save(any(Employee.class))).thenReturn(newEmployee);

//        act
          EmployeeDto updatedEmployeeDto = employeeService.updateEmployee(mockEmployeeDto.getId(), mockEmployeeDto);
//        assert

        assertThat(updatedEmployeeDto).isEqualTo(mockEmployeeDto);

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any());
    }

    @Test
    void testDeleteEmployee_WhenEmployeeDoestNotExists_ThenThrowException(){
//        arrange
        when(employeeRepository.existsById(1L)).thenReturn(false);

//        act and assert
        assertThatThrownBy(()->employeeService.deleteEmployee(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id: 1");

        verify(employeeRepository).existsById(1L);
        verify(employeeRepository, never()).deleteById(1L);
    }

    @Test
    void testDeleteEmployee_WhenEmployeeExists_ThenDeleteEmployee(){
           when(employeeRepository.existsById(1L)).thenReturn(true);

           assertThatCode(()->employeeService.deleteEmployee(1L)).doesNotThrowAnyException();

           verify(employeeRepository).deleteById(1L);
    }


}