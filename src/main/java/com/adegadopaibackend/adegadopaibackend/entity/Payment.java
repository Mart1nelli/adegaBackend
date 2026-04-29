package com.adegadopaibackend.adegadopaibackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.adegadopaibackend.adegadopaibackend.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode(exclude = {"order", "user", "method"})
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "methodId")
    @JsonBackReference
    private PaymentMethod method;
}
