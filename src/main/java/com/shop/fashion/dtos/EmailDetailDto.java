package com.shop.fashion.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetailDto {

    private String recipient;
    private String emailBody;
    private String emailSubject;
}