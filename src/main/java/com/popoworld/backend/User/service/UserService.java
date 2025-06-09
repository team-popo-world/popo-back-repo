package com.popoworld.backend.User.service;

import com.popoworld.backend.User.dto.Request.*;
import com.popoworld.backend.User.dto.Response.LoginResponseDTO;
import com.popoworld.backend.User.dto.Response.RefreshTokenResponseDTO;

public interface UserService {
    void signup(SignupRequestDTO requestDto);
    LoginResponseDTO login(LoginRequestDTO requestDto);
    void logout(LogoutRequestDTO requestDto);
    RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO requestDto);
}
