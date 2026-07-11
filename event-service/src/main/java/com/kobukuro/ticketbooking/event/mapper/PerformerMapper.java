package com.kobukuro.ticketbooking.event.mapper;

import com.kobukuro.ticketbooking.event.dto.PerformerDto;
import com.kobukuro.ticketbooking.event.dto.UpdatePerformerRequest;
import com.kobukuro.ticketbooking.event.entity.Performer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PerformerMapper {

    PerformerDto toDto(Performer performer);

    Performer toEntity(PerformerDto performerDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePerformerFromRequest(UpdatePerformerRequest request, @MappingTarget Performer performer);
}
