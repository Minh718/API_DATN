package com.shop.fashion.dtos.dtosReq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfobipSmsRequest {
    private Message[] messages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String from;
        private Destination[] destinations;
        private String text;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Destination {
            private String to;
        }
    }
}