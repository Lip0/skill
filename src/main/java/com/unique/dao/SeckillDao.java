package com.unique.dao;

import com.unique.entity.Seckill;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SeckillDao {
    /**
     * 减库存
     * @param seckillid
     * @param killTime 执行减库存的时间，对应数据库的createtime字段
     * @return 如果返回结果>1，表示更新的记录行数
     */
    int reduceNumber(@Param("seckillid") long seckillid,@Param("killTime") Date killTime);

    /**
     * 根据id查询秒杀对象
     * @param seckillid
     * @return
     */
    Seckill queryByID(long seckillid);

    /**
     * 根据便宜量查询秒杀商品列表
     * @param offset
     * @param limit
     * @return
     */
    List<Seckill> queryAll(@Param("offset") int offset,@Param("limit") int limit);
}
