package az.ilham.ecommerceauth.auth.service;

import az.ilham.ecommerceauth.dto.auth.AuthResponse;

public record AuthLoginResult(AuthResponse response, String refreshToken) {
}
