package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.*;

public interface UserService {
    ApiResponse<UserResponseDTO> registerUser(UserRegistrationRequest user);
    ApiResponse<AuthResponseDTO> login(UserLoginRequest request);
}
