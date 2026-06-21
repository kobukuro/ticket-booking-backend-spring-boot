package com.kobukuro.ticketbooking.event.mapper;

import com.kobukuro.ticketbooking.event.dto.VenueDto;
import com.kobukuro.ticketbooking.event.entity.Venue;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VenueMapper {

    VenueDto toDto(Venue venue);

    Venue toEntity(VenueDto venueDto);
}
