package com.dto;


import com.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SignUpRequestDto {

    @NotNull(message = "Name cannot be null")
    @NotBlank(message = "Name cannot be blank")
    private  String name;

    @Pattern(
            regexp = "^\\w{3,}$",
            message = "Username must be at least 3 characters long and contain only letters, numbers, and underscores"
    )
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{12,20}$",
            message = "Password must be 12-20 characters long, contain at least one digit, one lowercase letter, one uppercase letter, one special character, and have no whitespace"
    )
    @NotNull(message = "Password cannot be null")
    private String password;

    private Gender gender;

    @Past(message = "Date of birth must be in the past and in the format yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    @NotNull
    private LocalDate dob;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

}
