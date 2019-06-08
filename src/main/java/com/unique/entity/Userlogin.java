package com.unique.entity;

import java.util.Date;

public class Userlogin {
    private long seckillid;
    private long userphone;
    private short state;
    private Date createTime;

    //多对一的复合属性，秒杀成功的商品实体类
    private Seckill seckill;

    public long getSeckill_id() {
        return seckillid;
    }

    public void setSeckill_id(long seckill_id) {
        this.seckillid = seckill_id;
    }

    public long getUser_phone() {
        return userphone;
    }

    public void setUser_phone(long user_phone) {
        this.userphone = user_phone;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Seckill getSeckill() {
        return seckill;
    }

    public void setSeckill(Seckill seckill) {
        this.seckill = seckill;
    }

    @Override
    public String toString() {
        return "Userlogin{" +
                "seckill_id=" + seckillid +
                ", user_phone=" + userphone +
                ", state=" + state +
                ", createTime=" + createTime +
                '}';
    }
}
