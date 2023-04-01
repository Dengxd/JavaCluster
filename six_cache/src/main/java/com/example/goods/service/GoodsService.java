package com.example.goods.service;

import com.example.AjaxResult;
import com.example.goods.domain.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jjh
 * @since 2023-03-26
 */
public interface GoodsService extends IService<Goods> {


    public Goods create(Goods goods);


    public AjaxResult update(Goods goods);

    public Goods get( Long id) ;

    public AjaxResult del( Long id);
}
