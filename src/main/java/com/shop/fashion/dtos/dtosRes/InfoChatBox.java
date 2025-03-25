package com.shop.fashion.dtos.dtosRes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InfoChatBox {
    private Long idChatBox;
    private String idAdmin;
    private boolean isSeen;
}
