package com.owuor.somolink.users.dto;

import com.owuor.somolink.auth.enums.Role;
import lombok.Data;

@Data
public class RegisterSchoolUserRequest {
    private Long schoolId;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
