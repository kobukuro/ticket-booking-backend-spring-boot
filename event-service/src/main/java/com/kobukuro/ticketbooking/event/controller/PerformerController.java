package com.kobukuro.ticketbooking.event.controller;

import com.kobukuro.ticketbooking.event.dto.CreatePerformerRequest;
import com.kobukuro.ticketbooking.event.dto.PagedResponse;
import com.kobukuro.ticketbooking.event.dto.PerformerDto;
import com.kobukuro.ticketbooking.event.dto.UpdatePerformerRequest;
import com.kobukuro.ticketbooking.event.service.PerformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/performers")
@RequiredArgsConstructor
public class PerformerController {

    private final PerformerService performerService;

    @PostMapping
    public ResponseEntity<PerformerDto> createPerformer(@RequestBody CreatePerformerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(performerService.create(request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<PerformerDto>> listPerformers(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(performerService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformerDto> getPerformer(@PathVariable UUID id) {
        return ResponseEntity.ok(performerService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PerformerDto> updatePerformer(@PathVariable UUID id,
                                                        @RequestBody UpdatePerformerRequest request) {
        return ResponseEntity.ok(performerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerformer(@PathVariable UUID id) {
        performerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
