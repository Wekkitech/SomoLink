package com.owuor.somolink.users.controller;


import com.owuor.somolink.users.dto.RegisterSchoolUserRequest;
import com.owuor.somolink.users.dto.SchoolUserResponse;
import com.owuor.somolink.users.service.SchoolUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/")
@RequiredArgsConstructor
public class SchoolUserController {

    private final SchoolUserService schoolUserService;


    @PostMapping("/add/{schoolId}")
    public ResponseEntity<SchoolUserResponse> addUserToSchool(
            @PathVariable Long schoolId,
            @Valid @RequestBody RegisterSchoolUserRequest request
    ) {
        return ResponseEntity.ok(schoolUserService.addUserToSchool(schoolId, request));
    }

    @GetMapping("/{schoolId}")
    public ResponseEntity<List<SchoolUserResponse>> getUsersInSchool(
            @PathVariable Long schoolId
    ) {
        return ResponseEntity.ok(schoolUserService.getUsersInSchool(schoolId));
    }


}
