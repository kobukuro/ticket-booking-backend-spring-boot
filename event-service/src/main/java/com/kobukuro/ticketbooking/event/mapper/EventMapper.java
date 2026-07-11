package com.kobukuro.ticketbooking.event.mapper;

import com.kobukuro.ticketbooking.event.dto.EventDto;
import com.kobukuro.ticketbooking.event.dto.UpdateEventRequest;
import com.kobukuro.ticketbooking.event.entity.Event;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {VenueMapper.class, PerformerMapper.class})
public interface EventMapper {

    EventDto toDto(Event event);

    Event toEntity(EventDto eventDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "performers", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEventFromRequest(UpdateEventRequest request, @MappingTarget Event event);
}
