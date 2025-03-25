package com.shop.fashion.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosRes.MessageDTO;
import com.shop.fashion.entities.Message;

@Mapper
public interface MessageMapper {
    MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);

    MessageDTO toMessageDTO(Message message);

    List<MessageDTO> toMessageDTOs(List<Message> messages);
}
