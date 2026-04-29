package com.adegadopaibackend.adegadopaibackend.dto.request;

import com.adegadopaibackend.adegadopaibackend.entity.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {

    @NotBlank
    private String title;

    @NotNull
    private ReportType type;

    private String data;
}
