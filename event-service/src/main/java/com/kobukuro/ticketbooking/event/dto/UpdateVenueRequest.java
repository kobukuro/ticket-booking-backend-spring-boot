package com.kobukuro.ticketbooking.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateVenueRequest {
    private String name;
    private String country;
    private String city;
    private String address;
}
