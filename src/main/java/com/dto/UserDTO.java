package com.dto;

import com.enums.Gender;
import com.enums.Role;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {
    private  String name;
    private String username;
    private String email;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private Role role;
}
