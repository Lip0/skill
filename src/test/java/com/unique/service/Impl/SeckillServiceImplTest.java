package com.unique.service.Impl;

import com.unique.dto.Exposer;
import com.unique.dto.SeckillExecution;
import com.unique.entity.Seckill;
import com.unique.service.SeckillService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml",
                        "classpath:spring/spring-service.xml"})
public class SeckillServiceImplTest {
    private final Logger logger=LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> seckillList = seckillService.getSeckillList();
        logger.info("list={}",seckillList);
    }

    @Test
    public void getById() {
        Seckill byId = seckillService.getById(1000);
        logger.info("ID={}",byId);
    }

    @Test
    public void exportSeckillUrl() {
        Exposer exposer = seckillService.exportSeckillUrl(1000);
        logger.info("exposer={}",exposer);
    }

    @Test
    public void executeSeckill() {
        SeckillExecution seckillExecution = seckillService.executeSeckill(1000L,
                                            13989456654L, "de7590728d006dd752f9ec8457e84e6e");
        logger.info("seckillExecution={}",seckillExecution);
    }
}