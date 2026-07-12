package com.kobukuro.ticketbooking.event.service;

import com.kobukuro.ticketbooking.event.dto.CreatePerformerRequest;
import com.kobukuro.ticketbooking.event.dto.PagedResponse;
import com.kobukuro.ticketbooking.event.dto.PerformerDto;
import com.kobukuro.ticketbooking.event.dto.UpdatePerformerRequest;
import com.kobukuro.ticketbooking.event.entity.Performer;
import com.kobukuro.ticketbooking.event.mapper.PerformerMapper;
import com.kobukuro.ticketbooking.event.repository.PerformerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformerService {

    private final PerformerRepository performerRepository;
    private final PerformerMapper performerMapper;

    @Transactional
    public PerformerDto create(CreatePerformerRequest request) {
        Performer performer = Performer.builder()
                .name(request.name())
                .build();
        return performerMapper.toDto(performerRepository.save(performer));
    }

    public PagedResponse<PerformerDto> findAll(Pageable pageable) {
        return PagedResponse.of(performerRepository.findAll(pageable).map(performerMapper::toDto));
    }

    public PerformerDto findById(UUID id) {
        Performer performer = performerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found: " + id));
        return performerMapper.toDto(performer);
    }

    @Transactional
    public PerformerDto update(UUID id, UpdatePerformerRequest request) {
        Performer performer = performerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found: " + id));
        performerMapper.updatePerformerFromRequest(request, performer);
        return performerMapper.toDto(performerRepository.save(performer));
    }

    @Transactional
    public void delete(UUID id) {
        Performer performer = performerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found: " + id));
        performerRepository.delete(performer);
    }
}
