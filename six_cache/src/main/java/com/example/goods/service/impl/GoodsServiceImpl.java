package com.example.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.AjaxResult;

import com.example.goods.domain.Goods;
import com.example.goods.mapper.GoodsMapper;
import com.example.goods.service.GoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jjh
 * @since 2023-03-26
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    @Autowired
    private  RedissonClient redissonClient;
    @Autowired
    private  GoodsMapper goodsMapper;

    public static Integer  REDIS_EXP=36000;
    public static Integer  REDIS_NODATA_EXP=30;
    public static final String PREFIX_CACHE="GOODS:";
    public static final String NODATA="{}";
    public static final String PREFIX_LOCK_CREATE_CACHE="LOCK:CREATE:CACHE:";
    public static final String PREFIX_LOCK_UPDATE="LOCK:UPDATE:";

    /**
     *
     * @return 0-3600之间的随机数，和REDIS_EXP相加，做为redis过期时间
     */
    public static Integer getExp(){
        Random r =new Random();
        return  REDIS_EXP+r.nextInt(3600);
    }
    public static Integer getNoDataExp(){
        Random r =new Random();
        return  REDIS_NODATA_EXP+r.nextInt(30);
    }




    @Transactional
    public Goods create(Goods goods) {
        //插入数据库，用写锁
        RReadWriteLock updateLock=redissonClient.getReadWriteLock(PREFIX_LOCK_UPDATE+goods.getId());
        RLock wLock=updateLock.writeLock();
        wLock.lock();
        try {
            if (this.save(goods)) {
                RBucket<Object> bucket = redissonClient.getBucket(PREFIX_CACHE + goods.getId());
                bucket.set(JSON.toJSONString(goods), getExp(), TimeUnit.SECONDS);
                return goods;
            } else {
                return null;
            }
        }finally {
            wLock.unlock();
        }

    }

    @Transactional
    public AjaxResult update(Goods goods) {
        if(goods.getId()==null){
            return AjaxResult.error("id不能为空");
        }
        //修改数据库，用写锁
        RReadWriteLock updateLock=redissonClient.getReadWriteLock(PREFIX_LOCK_UPDATE+goods.getId());
        RLock wLock=updateLock.writeLock();
        wLock.lock();
        try {
            if (this.updateById(goods)) {
                RBucket<Object> bucket = redissonClient.getBucket(PREFIX_CACHE + goods.getId());
                bucket.set(JSON.toJSONString(goods), getExp(), TimeUnit.SECONDS);
                return AjaxResult.success();
            } else {
                return AjaxResult.error();
            }
        }finally {
            wLock.unlock();
        }


    }

    public Goods getFromRedis(RBucket<Object> bucket,String key){
        Goods goods=null;
        String strGood = (String)bucket.get();
        if(strGood!=null && !strGood.isEmpty()){
            if(NODATA.equals(strGood)){  //缓存中的数据是“{}”
                bucket.expire(getNoDataExp(),TimeUnit.SECONDS);//更新有效期
                return new Goods();//返回一个新对象，所有字段都是NULL
            }
            bucket.expire(getExp(),TimeUnit.SECONDS);//更新有效期
            return JSON.parseObject(strGood,Goods.class);
        }
        return goods;
    }
    public Goods get( Long id)  {
        Goods goods=null;
        String key=PREFIX_CACHE+id;
        RBucket<Object> bucket = redissonClient.getBucket(key);

        goods=getFromRedis(bucket,key);
        if(goods!=null){  //缓存中有数据
            return goods;  //直接返回了
        }
        //缓存中没数据
        //加一个分布式锁，只让第一个用户查数据库，写缓存
        RLock createCacheLock=redissonClient.getLock(PREFIX_LOCK_CREATE_CACHE+id);
        createCacheLock.lock();
        //createCacheLock.tryLock(3,TimeUnit.SECONDS);
        try{
            //因为第一个用户已经把数据加到缓存中了，
            // 所以第二个用户，第三个用户,……,第N个用户，
            // 得到锁之后，再到缓存中找一次
            goods=getFromRedis(bucket,key);
            if(goods!=null){  //缓存中已经有数据了
                return goods; //直接返回
            }
            //缓存中没找到数据，恭喜你，你是第一个用户，
            //下面的代码，查数据库，写缓存

            //接下来，还有一个读写锁在等着你
            //读取数据库，用读锁
            RReadWriteLock updateLock=redissonClient.getReadWriteLock(PREFIX_LOCK_UPDATE+id);
            RLock rLock=updateLock.readLock();
            rLock.lock();
            try{
                goods = this.getById(id); //到数据库中查找这个id
                if (goods != null) {//数据库找到数据
                    //加入缓存
                    bucket.set(JSON.toJSONString(goods), getExp(), TimeUnit.SECONDS);
                } else { //数据库中找不到数据
                    //对这个ID，在缓存中加入空对象“{}”
                    bucket.set(NODATA, getNoDataExp(), TimeUnit.SECONDS);
                }
            }finally {

                rLock.unlock();

            }

        }finally {
            createCacheLock.unlock();


        }

        return goods;

    }

    @Transactional
    public AjaxResult del( Long id) {
        //删除数据库，用写锁
        RReadWriteLock updateLock=redissonClient.getReadWriteLock(PREFIX_LOCK_UPDATE+id);
        RLock wLock=updateLock.writeLock();
        wLock.lock();
        try {
            int rows = goodsMapper.deleteById(id);//删除数据库中商品
            if (rows == 0) {//删除失败
                return AjaxResult.error();
            } else {//删除成功
                String key = PREFIX_CACHE + id;
                RBucket<Object> bucket = redissonClient.getBucket(key);
                bucket.delete(); //把缓存中的数据也删除了
                return AjaxResult.success();
            }
        }finally {
            wLock.unlock();
        }

    }

}
