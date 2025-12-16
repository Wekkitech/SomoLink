package com.owuor.somolink.auth.controller;


import com.owuor.somolink.auth.dto.LoginRequest;
import com.owuor.somolink.auth.dto.TokenPair;
import com.owuor.somolink.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenPair> login( @Valid  @RequestBody LoginRequest loginRequest) {
        TokenPair tokenPair = authService.login(loginRequest);
        return ResponseEntity.ok(tokenPair);
    }

    @GetMapping("/home")
    public String home() {
        return "Hello World";
    }

}
