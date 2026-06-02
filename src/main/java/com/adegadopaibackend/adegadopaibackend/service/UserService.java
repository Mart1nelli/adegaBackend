package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateUserRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateUserRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse create(CreateUserRequest req);

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse update(Long id, UpdateUserRequest req);

    void delete(Long id);

    UserResponse getAuthenticatedUser();
}
