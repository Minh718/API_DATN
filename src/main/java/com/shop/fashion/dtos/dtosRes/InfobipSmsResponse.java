package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfobipSmsResponse {
    private MessageStatus[] messages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageStatus {
        private String to;
        private Status status;
        private String messageId;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Status {
            private Integer Id;
            private String description;
            private Integer groupId;
            private String groupName;
            private String name;
        }
    }
}
