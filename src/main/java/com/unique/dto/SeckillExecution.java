package com.unique.dto;

import com.unique.entity.Userlogin;
import com.unique.enums.SeckillStateEnum;

/**
 * 封装秒杀执行后的结果
 */
public class SeckillExecution {
    private  long seckillId;
    //秒杀执行状态
    private int state;
    //状态表示
    private String stateInfo;
    //秒杀成功对象
    private Userlogin userlogin;

    public SeckillExecution(long seckillId, SeckillStateEnum seckillStateEnum,Userlogin userlogin) {
        this.seckillId = seckillId;
        this.state = seckillStateEnum.getState();
        this.stateInfo = seckillStateEnum.getStateInfo();
        this.userlogin = userlogin;
    }

    public SeckillExecution(long seckillId, SeckillStateEnum seckillStateEnum) {
        this.seckillId = seckillId;
        this.state = seckillStateEnum.getState();
        this.stateInfo = seckillStateEnum.getStateInfo();
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public Userlogin getUserlogin() {
        return userlogin;
    }

    public void setUserlogin(Userlogin userlogin) {
        this.userlogin = userlogin;
    }

    @Override
    public String toString() {
        return "SeckillExecution{" +
                "seckillId=" + seckillId +
                ", state=" + state +
                ", stataInfo='" + stateInfo + '\'' +
                ", userlogin=" + userlogin +
                '}';
    }
}
