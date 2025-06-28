package com.popoworld.backend.webpush.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushSubscriptionRequest {
    private String endpoint;
    private String p256dh;
    private String auth;
}