package com.shop.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.shop.model.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
