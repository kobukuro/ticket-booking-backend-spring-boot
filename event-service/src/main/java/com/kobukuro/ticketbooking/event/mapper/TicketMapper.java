package com.kobukuro.ticketbooking.event.mapper;

import com.kobukuro.ticketbooking.event.dto.TicketDto;
import com.kobukuro.ticketbooking.event.dto.UpdateTicketRequest;
import com.kobukuro.ticketbooking.event.entity.Ticket;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TicketMapper {

    @Mapping(source = "event.id", target = "eventId")
    TicketDto toDto(Ticket ticket);

    @Mapping(source = "eventId", target = "event.id")
    Ticket toEntity(TicketDto ticketDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTicketFromRequest(UpdateTicketRequest request, @MappingTarget Ticket ticket);
}
