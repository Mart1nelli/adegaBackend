package com.adegadopaibackend.adegadopaibackend.dto.response;

import com.adegadopaibackend.adegadopaibackend.entity.enums.ReportType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportResponse {

    private Long id;

    private String title;

    private ReportType type;

    private String data;
}
