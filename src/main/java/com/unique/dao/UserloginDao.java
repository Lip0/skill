package com.unique.dao;

import com.unique.entity.Userlogin;
import org.apache.ibatis.annotations.Param;

public interface UserloginDao {
    /**
     * 插入用户秒杀成功后的购买明细数据，可过滤重复
     * @param seckillid
     * @param userphone
     * @return 插入的行数，返回0表示插入失败
     */
    int insertSuccessKilled(@Param("seckillid") long seckillid,@Param("userphone") long userphone);

    /**
     * 根据id查询Userlogin并携带秒杀产品对象实体
     * @param seckillid
     * @return
     */
    Userlogin queryByIdWithSeckill(@Param("seckillid") long seckillid,@Param("userphone") long userphone);

}
