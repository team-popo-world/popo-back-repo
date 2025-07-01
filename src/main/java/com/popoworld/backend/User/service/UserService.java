package com.popoworld.backend.User.service;

import com.popoworld.backend.User.dto.Request.*;
import com.popoworld.backend.User.dto.Response.ChildLoginResponseDTO;
import com.popoworld.backend.User.dto.Response.LoginResponseDTO;
import com.popoworld.backend.User.dto.Response.RefreshTokenResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface UserService {
    void signup(SignupRequestDTO requestDto) throws Exception;
    LoginResponseDTO login(LoginRequestDTO requestDto, HttpServletRequest request);
    void logout(LogoutRequestDTO requestDto);
    RefreshTokenResponseDTO refreshToken(String requestDto);

    LoginResponseDTO getUserInfo(UUID userId);
}
