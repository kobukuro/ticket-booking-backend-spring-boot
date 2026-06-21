package com.kobukuro.ticketbooking.event.mapper;

import com.kobukuro.ticketbooking.event.dto.PerformerDto;
import com.kobukuro.ticketbooking.event.entity.Performer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PerformerMapper {

    PerformerDto toDto(Performer performer);

    Performer toEntity(PerformerDto performerDto);
}
