package com.shop.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.shop.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("<script>" +
            "SELECT * FROM products p " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND LOWER(p.name) LIKE CONCAT('%', #{keyword}, '%')" +
            "</if>" +
            "<if test='categoryId != null'>" +
            "AND p.category_id = #{categoryId}" +
            "</if>" +
            "<if test='minPrice != null'>" +
            "AND p.price &gt;= #{minPrice}" +
            "</if>" +
            "<if test='maxPrice != null'>" +
            "AND p.price &lt;= #{maxPrice}" +
            "</if>" +
            "<if test='inStock == true'>" +
            "AND p.stock &gt; 0" +
            "</if>" +
            "</where>" +
            "ORDER BY p.created_at DESC" +
            "</script>")
    List<Product> searchProducts(@Param("keyword") String keyword,
                                @Param("categoryId") Long categoryId,
                                @Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                @Param("inStock") Boolean inStock);
}
