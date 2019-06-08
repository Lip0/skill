package com.unique.dao;

import com.unique.entity.Userlogin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class UserloginDaoTest {

    @Resource
    private UserloginDao userloginDao;

    @Test
    public void testInsertSuccessKilled(){
        int insertCount=userloginDao.insertSuccessKilled(1000L,13086693569L);
        System.out.println(insertCount);
    }

    @Test
    public void tsetQueryByIdWithSeckill(){
        Userlogin userlogin = userloginDao.queryByIdWithSeckill(1000L, 13086693569L);
        System.out.println(userlogin);
        System.out.println(userlogin.getSeckill());
    }
}