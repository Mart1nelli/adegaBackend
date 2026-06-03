package com.adegadopaibackend.adegadopaibackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long id;

    private String street;

    private String number;

    private String city;

    private String state;

    private String zipCode;

    private Boolean isDefault;

    private String complement;

    private String neighborhood;
}
