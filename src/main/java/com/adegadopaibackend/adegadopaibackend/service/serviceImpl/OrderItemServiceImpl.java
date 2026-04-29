package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderItemRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateOrderItemRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderItemResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Order;
import com.adegadopaibackend.adegadopaibackend.entity.OrderItem;
import com.adegadopaibackend.adegadopaibackend.entity.Product;
import com.adegadopaibackend.adegadopaibackend.mapper.OrderItemMapper;
import com.adegadopaibackend.adegadopaibackend.repository.OrderItemRepository;
import com.adegadopaibackend.adegadopaibackend.repository.OrderRepository;
import com.adegadopaibackend.adegadopaibackend.repository.ProductRepository;
import com.adegadopaibackend.adegadopaibackend.service.OrderItemService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderItemResponse create(CreateOrderItemRequest req) {
        OrderItem orderItem = orderItemMapper.toEntity(req);

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        orderItem.setOrder(order);
        orderItem.setProduct(product);

        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        return orderItemMapper.toResponse(savedOrderItem);
    }

    @Override
    public List<OrderItemResponse> findAll() {
        List<OrderItem> orderItems = orderItemRepository.findAll();
        return orderItems.stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }

    @Override
    public OrderItemResponse findById(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OrderItem not found"));

        return orderItemMapper.toResponse(orderItem);
    }

    @Override
    public OrderItemResponse update(Long id, UpdateOrderItemRequest req) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OrderItem not found"));

        orderItemMapper.updateEntity(req, orderItem);

        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
        return orderItemMapper.toResponse(updatedOrderItem);
    }

    @Override
    public void delete(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OrderItem not found"));

        orderItemRepository.delete(orderItem);
    }
}
