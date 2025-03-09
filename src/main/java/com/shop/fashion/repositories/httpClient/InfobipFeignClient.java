package com.shop.fashion.repositories.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.shop.fashion.configurations.InfobipFeignConfig;
import com.shop.fashion.dtos.dtosReq.InfobipSmsRequest;
import com.shop.fashion.dtos.dtosRes.InfobipSmsResponse;

@FeignClient(name = "infobipSmsClient", url = "${infobip.base-url}", configuration = InfobipFeignConfig.class)
public interface InfobipFeignClient {

    @PostMapping("/sms/2/text/advanced")
    InfobipSmsResponse sendSms(@RequestBody InfobipSmsRequest request);
}