package com.adegadopaibackend.adegadopaibackend.entity;

import com.adegadopaibackend.adegadopaibackend.entity.enums.ReportType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReportType type;

    @Column(columnDefinition = "TEXT")
    private String data;

    @CreationTimestamp
    private LocalDateTime generatedAt;
}
