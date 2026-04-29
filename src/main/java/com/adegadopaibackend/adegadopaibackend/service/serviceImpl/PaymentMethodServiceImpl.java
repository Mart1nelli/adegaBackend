package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentMethodRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdatePaymentMethodRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentMethodResponse;
import com.adegadopaibackend.adegadopaibackend.entity.PaymentMethod;
import com.adegadopaibackend.adegadopaibackend.mapper.PaymentMethodMapper;
import com.adegadopaibackend.adegadopaibackend.repository.PaymentMethodRepository;
import com.adegadopaibackend.adegadopaibackend.service.PaymentMethodService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;

    @Override
    public PaymentMethodResponse create(CreatePaymentMethodRequest req) {
        PaymentMethod paymentMethod = paymentMethodMapper.toEntity(req);
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        return paymentMethodMapper.toResponse(saved);
    }

    @Override
    public List<PaymentMethodResponse> findAll() {
        return paymentMethodMapper.toResponseList(paymentMethodRepository.findAll());
    }

    @Override
    public PaymentMethodResponse findById(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment method with ID: " + id + " not found"));
        return paymentMethodMapper.toResponse(paymentMethod);
    }

    @Override
    public PaymentMethodResponse update(Long id, UpdatePaymentMethodRequest req) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment method with ID: " + id + " not found"));

        paymentMethodMapper.updateEntity(req, paymentMethod);
        PaymentMethod updated = paymentMethodRepository.save(paymentMethod);
        return paymentMethodMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment method with ID: " + id + " not found"));
        paymentMethodRepository.delete(paymentMethod);
    }
}
