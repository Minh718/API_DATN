package com.shop.fashion.repositories.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import com.shop.fashion.dtos.dtosReq.GetTokenGoogleReq;
import com.shop.fashion.dtos.dtosRes.GetTokenGoogleRes;

import feign.QueryMap;

@FeignClient(name = "outbound-google", url = "https://oauth2.googleapis.com")
public interface OutboundIdentityClientGoogle {

    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public GetTokenGoogleRes getToken(@QueryMap GetTokenGoogleReq getTokenGoogleReq);

}
