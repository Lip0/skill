package com.unique.service.Impl;

import com.unique.dao.SeckillDao;
import com.unique.dao.UserloginDao;
import com.unique.dto.Exposer;
import com.unique.dto.SeckillExecution;
import com.unique.entity.Seckill;
import com.unique.entity.Userlogin;
import com.unique.enums.SeckillStateEnum;
import com.unique.exception.RepeatKillException;
import com.unique.exception.SeckillCloseException;
import com.unique.exception.SeckillException;
import com.unique.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;
@Service
public class SeckillServiceImpl implements SeckillService {
    //需要注入Service依赖
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private UserloginDao userloginDao;

    private Logger logger=LoggerFactory.getLogger(this.getClass());
    //MD5盐值字符串，用于混淆MD5
    private final String slat="shnvossosjiow324#$##$#%#FVSVWW@@!!^^^H";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,10);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryByID(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill=seckillDao.queryByID(seckillId);
        if(seckill==null){
            return new Exposer(false,seckillId);
        }
        Date startTime =seckill.getStartTime();
        Date endTime=seckill.getEndTime();
        Date nowTime=new Date();
        if(nowTime.getTime()<startTime.getTime()
                || nowTime.getTime()>endTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),
                    endTime.getTime());
        }
        //转化特定字符串的过程，不可逆
        String md5=getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMD5(long seckillID){
        String base=seckillID+"/"+slat;
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1. 开发团队达成一致约定，明确标注事务方法的风格。
     * 2. 保证事务方法的执行时间尽可能短，不要穿插其他的网络操作，RPC/http请求或者剥离到事务方法外
     * 3. 不是所有的方法都需要事务，如只有一条修改操作，只读操作等不需要事务控制
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if(md5==null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("秒杀数据被篡改！");
        }
        //执行秒杀逻辑
        //1.减库存
        //2.记录秒杀行为
        Date nowTime=new Date();
        try{
            int updateCount=seckillDao.reduceNumber(seckillId,nowTime);
            if(updateCount<=0){
                //没有更新记录到数据库,秒杀结束
                throw  new SeckillCloseException("秒杀已经关闭了！");
            }else {
                //秒杀成功，购买记录行为，减库存
                int insertCount=userloginDao.insertSuccessKilled(seckillId,userPhone);
                //唯一：seckillId,userPhone
                if(insertCount<=0){
                    //重复秒杀
                    throw new RepeatKillException("重复秒杀");
                }else {
                    //秒杀成功
                    Userlogin userlogin=userloginDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS,userlogin);
                }
            }
        }catch (SeckillCloseException e1){
            throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            //所有编译期异常转换为运行期异常
            throw new SeckillException("seckill内部错误"+e.getMessage());
        }
    }
}
