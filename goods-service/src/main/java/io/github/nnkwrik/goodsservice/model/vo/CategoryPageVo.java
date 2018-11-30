package io.github.nnkwrik.goodsservice.model.vo;

import io.github.nnkwrik.goodsservice.model.po.Category;
import io.github.nnkwrik.goodsservice.model.po.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author nnkwrik
 * @date 18/11/17 20:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPageVo {
    private List<Category> brotherCategory; //同一个父分类下的兄弟分类
    private List<Goods> goodsList;    //当前分类的商品列表
}


