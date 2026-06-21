package com.kobukuro.ticketbooking.event.mapper;

import com.kobukuro.ticketbooking.event.dto.TicketDto;
import com.kobukuro.ticketbooking.event.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TicketMapper {

    @Mapping(source = "event.id", target = "eventId")
    TicketDto toDto(Ticket ticket);

    @Mapping(source = "eventId", target = "event.id")
    Ticket toEntity(TicketDto ticketDto);
}
