package com.popoworld.backend.webpush.service;

import com.popoworld.backend.webpush.entity.WebPush;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import nl.martijndwars.webpush.Notification;

import java.security.Security;


@Service
@RequiredArgsConstructor
public class PushSubService {

    @Value("${push.public-key}")
    private String publicKey;

    @Value("${push.private-key}")
    private String privateKey;

    public void sendNotification(WebPush sub, String message) throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        PushService pushService = new PushService()
                        .setPublicKey(publicKey)
                        .setPrivateKey(privateKey)
                        .setSubject("mailto:ooinl77@naver.com");

        Notification notification = new Notification(
                sub.getEndpoint(),
                sub.getP256dh(),
                sub.getAuth(),
                message
        );

        pushService.send(notification);
    }
}
