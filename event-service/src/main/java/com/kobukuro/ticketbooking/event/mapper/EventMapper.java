package com.kobukuro.ticketbooking.event.mapper;

import com.kobukuro.ticketbooking.event.dto.EventDto;
import com.kobukuro.ticketbooking.event.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {VenueMapper.class, PerformerMapper.class})
public interface EventMapper {

    EventDto toDto(Event event);

    Event toEntity(EventDto eventDto);
}
