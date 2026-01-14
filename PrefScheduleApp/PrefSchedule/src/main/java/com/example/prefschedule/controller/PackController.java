package com.example.prefschedule.controller;

import com.example.prefschedule.dto.PackRequestDTO;
import com.example.prefschedule.dto.PackResponseDTO;
import com.example.prefschedule.entity.Pack;
import com.example.prefschedule.repository.PackRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/packs")
@RequiredArgsConstructor
public class PackController {

    private final PackRepository packRepository;

    @Operation(summary = "Get all packs", description = "Retrieves a list of all packs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of packs retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<PackResponseDTO>> getAllPacks() {
        List<PackResponseDTO> packs = packRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(packs);
    }

    @Operation(summary = "Get pack by ID", description = "Retrieves a single pack by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pack retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Pack not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PackResponseDTO> getPackById(@PathVariable Long id) {
        return packRepository.findById(id)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new pack", description = "Creates a new pack with year, semester, and name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pack created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<PackResponseDTO> createPack(@Valid @RequestBody PackRequestDTO request) {
        Pack pack = new Pack(request.getYear(), request.getSemester(), request.getName());
        Pack saved = packRepository.save(pack);
        return ResponseEntity.ok(toResponseDto(saved));
    }

    @Operation(summary = "Update an existing pack", description = "Updates a pack by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pack updated successfully"),
            @ApiResponse(responseCode = "404", description = "Pack not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PackResponseDTO> updatePack(@PathVariable Long id,
                                                      @Valid @RequestBody PackRequestDTO request) {
        return packRepository.findById(id)
                .map(existing -> {
                    existing.setYear(request.getYear());
                    existing.setSemester(request.getSemester());
                    existing.setName(request.getName());
                    Pack updated = packRepository.save(existing);
                    return ResponseEntity.ok(toResponseDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a pack", description = "Deletes a pack by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pack deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Pack not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePack(@PathVariable Long id) {
        return packRepository.findById(id)
                .map(pack -> {
                    packRepository.delete(pack);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ----------------------
    // Mapping entity -> DTO
    // ----------------------
    private PackResponseDTO toResponseDto(Pack pack) {
        PackResponseDTO dto = new PackResponseDTO();
        dto.setId(pack.getId());
        dto.setYear(pack.getYear());
        dto.setSemester(pack.getSemester());
        dto.setName(pack.getName());
        dto.setCoursesCount(pack.getCourses() != null ? pack.getCourses().size() : 0);
        return dto;
    }
}
