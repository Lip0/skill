--数据库初始化脚本

--创建数据库
create database seckill;
--使用数据库
use seckill;
--创建秒杀库存表
create table seckill(
seckill_id bigint not null auto_increment comment '商品库存ID',
name varchar (120) not null comment '商品名称',
number int not null comment '库存数量',
create_time timestamp not null default current_timestamp comment '创建时间',
start_time timestamp not null comment '秒杀开启时间',
end_time timestamp not null comment '秒杀结束时间',
primary key(seckill_id),
key idx_start_time(start_time),
key idx_end_time(end_time),
key idx_create_time(create_time)
)engine = InnoDB auto_increment=1000 default charset=utf8 comment='秒杀库存表';

--初始化数据
insert into seckill(name,number,start_time,end_time)
values
   ('苹果手机6plus',100,'2019-06-01 00:00:00','2019-06-02 00:00:00'),
   ('ipad mini4',200,'2019-06-01 00:00:00','2019-06-02 00:00:00'),
   ('华为p30',150,'2019-06-01 00:00:00','2019-06-02 00:00:00'),
   ('索尼耳机',103,'2019-06-01 00:00:00','2019-06-02 00:00:00'),
   ('LG电视机',50,'2019-06-01 00:00:00','2019-06-02 00:00:00');

--秒杀成功明细表
--用户登陆认证相关的信息
create table user_login(
seckill_id bigint not null comment '秒杀商品id',
user_phone bigint not null comment '用户登录认证手机号',
user_state tinyint not null default -1 comment '用户状态标识，-1:无效 0：成功 1：已付款 2：已发货',
create_time timestamp not null default current_timestamp comment '创建时间',
primary key(seckill_id,user_phone),/*联合主键*/
key idx_create_time(create_time)
)engine = InnoDB default charset=utf8 comment='用户登录认证表';

--连接数据库控制台
mysql -uroot -punique
