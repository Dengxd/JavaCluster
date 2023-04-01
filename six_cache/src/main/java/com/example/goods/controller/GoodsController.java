package com.example.goods.controller;


import com.example.AjaxResult;
import com.example.goods.domain.Goods;
import com.example.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jjh
 * @since 2023-03-26
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @RequestMapping(value="/create",method = RequestMethod.POST)
    public Goods create(@RequestBody Goods goods){
        return goodsService.create(goods);
    }

    @RequestMapping(value="/update", method = RequestMethod.POST)
    public AjaxResult update(@RequestBody Goods goods){
        return goodsService.update(goods);
    }

    @RequestMapping(value="/get/{id}")
    public Goods get(@PathVariable Long id){
        return goodsService.get(id);
    }

    @RequestMapping(value="/del/{id}")
    public AjaxResult del(@PathVariable Long id){
        return goodsService.del(id);
    }

}
