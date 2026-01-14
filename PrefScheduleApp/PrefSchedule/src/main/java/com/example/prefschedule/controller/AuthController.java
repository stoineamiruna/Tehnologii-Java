package com.example.prefschedule.controller;

import com.example.prefschedule.dto.*;
import com.example.prefschedule.entity.*;
import com.example.prefschedule.repository.*;
import com.example.prefschedule.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final StudentRepository studentRepo;
    private final InstructorRepository instructorRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final com.example.prefschedule.security.AppUserDetailsService uds;

    public AuthController(AuthenticationManager authManager, UserRepository userRepo, RoleRepository roleRepo, StudentRepository studentRepo,
                          InstructorRepository instructorRepo,
                          PasswordEncoder passwordEncoder, JwtUtils jwtUtils, com.example.prefschedule.security.AppUserDetailsService uds) {
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.studentRepo = studentRepo;
        this.instructorRepo = instructorRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.uds = uds;
    }

    @Operation(summary = "Register a new user", description = "Registers a new user with a given role (student or instructor). For students, 'year' is required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or username already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO dto) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());

        Role role = roleRepo.findByName(dto.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Set.of(role));

        if (dto.getRole().equals("ROLE_STUDENT")) {
            if (dto.getYear() == null) {
                return ResponseEntity.badRequest().body("Year is required for student");
            }
            Student student = new Student();
            student.setName(dto.getFullName());
            student.setYear(dto.getYear());
            student.setCode(UUID.randomUUID().toString());
            student.setEmail(dto.getEmail());
            studentRepo.save(student);
            user.setStudentProfile(student);
        } else if (dto.getRole().equals("ROLE_INSTRUCTOR")) {
            Instructor instructor = new Instructor();
            instructor.setName(dto.getFullName());
            instructor.setEmail(dto.getEmail());
            instructorRepo.save(instructor);
            user.setInstructorProfile(instructor);
        }


        userRepo.save(user);

        UserResponseDTO response = new UserResponseDTO(
                user.getUsername(),
                user.getFullName(),
                user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
                user.getStudentProfile() != null ? user.getStudentProfile().getId() : null,
                user.getInstructorProfile() != null ? user.getInstructorProfile().getId() : null
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token along with user details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            User userDetails = (User) auth.getPrincipal();
            String token = jwtUtils.generateToken(userDetails, 1000L * 60 * 60 * 6); // 6h

            AppUser user = userRepo.findByUsername(userDetails.getUsername()).get();
            UserResponseDTO response = new UserResponseDTO(
                    user.getUsername(),
                    user.getFullName(),
                    user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
                    user.getStudentProfile() != null ? user.getStudentProfile().getId() : null,
                    user.getInstructorProfile() != null ? user.getInstructorProfile().getId() : null
            );

            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("user", response);

            return ResponseEntity.ok(resp);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
