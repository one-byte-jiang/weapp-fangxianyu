package io.github.nnkwrik.goodsservice.controller;

import io.github.nnkwrik.common.dto.JWTUser;
import io.github.nnkwrik.common.dto.Response;
import io.github.nnkwrik.common.token.injection.JWT;
import io.github.nnkwrik.goodsservice.cache.BrowseCache;
import io.github.nnkwrik.goodsservice.model.po.Goods;
import io.github.nnkwrik.goodsservice.model.vo.CategoryPageVo;
import io.github.nnkwrik.goodsservice.model.vo.GoodsDetailPageVo;
import io.github.nnkwrik.goodsservice.model.vo.GoodsRelatedVo;
import io.github.nnkwrik.goodsservice.model.vo.inner.CategoryVo;
import io.github.nnkwrik.goodsservice.model.vo.inner.CommentVo;
import io.github.nnkwrik.goodsservice.model.vo.inner.GalleryVo;
import io.github.nnkwrik.goodsservice.model.vo.inner.GoodsDetailVo;
import io.github.nnkwrik.goodsservice.service.GoodsService;
import io.github.nnkwrik.goodsservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author nnkwrik
 * @date 18/11/14 18:42
 */
@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private UserService userService;

    @Autowired
    private BrowseCache browseCache;

    /**
     * 获取选定的子目录下的商品列表和同一个父目录下的兄弟目录
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/category/{categoryId}")

    public Response<CategoryVo> getCategoryPage(@PathVariable("categoryId") int categoryId,
                                                @RequestParam(value = "page", defaultValue = "1") int page,
                                                @RequestParam(value = "limit", defaultValue = "10") int size) {


        CategoryPageVo vo = goodsService.getGoodsAndBrotherCateById(categoryId, page, size);
        log.debug("通过分类浏览商品 : 商品={}", vo.getGoodsList());

        return Response.ok(vo);
    }

    @GetMapping("/list/{categoryId}")
    public Response<CategoryVo> getGoodsByCategory(@PathVariable("categoryId") int categoryId,
                                                   @RequestParam(value = "page", defaultValue = "1") int page,
                                                   @RequestParam(value = "limit", defaultValue = "10") int size) {
        CategoryPageVo vo = goodsService.getGoodsByCateId(categoryId, page, size);
        log.debug("通过分类浏览商品 : 商品={}", vo.getGoodsList());
        return Response.ok(vo);

    }

    @GetMapping("/detail/{goodsId}")
    public Response<GoodsDetailPageVo> getGoodsDetail(@PathVariable("goodsId") int goodsId,
                                                      @JWT JWTUser jwtUser) {
        //更新浏览次数
        browseCache.add(goodsId);
        GoodsDetailVo goodsDetail = goodsService.getGoodsDetail(goodsId);
        if (goodsDetail == null) {
            log.info("搜索goodsId = 【{}】的详情时出错", goodsId);
            return Response.fail(Response.USER_IS_NOT_EXIST, "无法搜索到商品卖家的信息");
        }
        List<GalleryVo> goodsGallery = goodsService.getGoodsGallery(goodsId);
        List<CommentVo> comment = goodsService.getGoodsComment(goodsId);

        boolean userHasCollect = false;
        if (jwtUser != null)
            userHasCollect = userService.userHasCollect(jwtUser.getOpenId(), goodsId);
        GoodsDetailPageVo vo = new GoodsDetailPageVo(goodsDetail, goodsGallery, comment, userHasCollect);
        log.debug("浏览商品详情 : 商品id={}，商品名={}", vo.getInfo().getId(), vo.getInfo().getName());

        return Response.ok(vo);
    }

    @GetMapping("/related/{goodsId}")
    public Response<GoodsRelatedVo> getGoodsRelated(@PathVariable("goodsId") int goodsId) {
        GoodsRelatedVo vo = goodsService.getGoodsRelated(goodsId);
        log.debug("与 goodsId=[] 相关的商品 : {}", goodsId, vo);

        return Response.ok(vo);
    }


    @PostMapping("/post")
    public Response PostGoods(@RequestBody Goods goods,
                              @JWT(required = true) JWTUser user) {

        if (StringUtils.isEmpty(goods.getName()) ||
                StringUtils.isEmpty(goods.getDesc()) ||
                StringUtils.isEmpty(goods.getRegion()) ||
                goods.getCategoryId() == null ||
//                goods.getPrimaryPicUrl() == null ||
                goods.getRegionId() == null ||
                goods.getPrice() == null) {
            String msg = "用户发布商品失败，信息不完整";
            log.info(msg);
            return Response.fail(Response.POST_INFO_INCOMPLETE, msg);
        }
        goods.setSellerId(user.getOpenId());
        goodsService.postGoods(goods);
        log.info("用户发布商品：用户昵称=【{}】，商品名=【{}】，详情=【{}】", user.getNickName(),goods.getName(),goods);

        return Response.ok();
    }


}
