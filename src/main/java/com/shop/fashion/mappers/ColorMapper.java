package com.shop.fashion.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.shop.fashion.dtos.dtosReq.ColorReq;
import com.shop.fashion.dtos.dtosRes.ColorRes;
import com.shop.fashion.dtos.dtosRes.ColorTable;
import com.shop.fashion.entities.Color;

@Mapper
public interface ColorMapper {
    ColorMapper INSTANCE = Mappers.getMapper(ColorMapper.class);

    ColorTable toColorTable(Color color);

    ColorRes toColorRes(Color color);

    List<ColorRes> toColorRess(List<Color> colors);

    Color toColor(ColorReq color);
}
