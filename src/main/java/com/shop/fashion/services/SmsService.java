package com.shop.fashion.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.InfobipSmsRequest;
import com.shop.fashion.dtos.dtosRes.InfobipSmsResponse;
import com.shop.fashion.repositories.httpClient.InfobipFeignClient;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final InfobipFeignClient infobipFeignClient;

    @Value("${infobip.from}")
    @NonFinal
    String from;

    public InfobipSmsResponse sendSms(String to, String text) {
        InfobipSmsRequest request = new InfobipSmsRequest(new InfobipSmsRequest.Message[] {
                new InfobipSmsRequest.Message(from,
                        new InfobipSmsRequest.Message.Destination[] { new InfobipSmsRequest.Message.Destination(to) },
                        text)
        });

        return infobipFeignClient.sendSms(request);
    }
}