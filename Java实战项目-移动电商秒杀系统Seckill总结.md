## 电商秒杀系统开发总结（一）
本文只是使用基本的ssm框架完成系统搭建，可以正常使用，后续在会有下一篇文章总结秒杀系统优化之后的内容。
项目地址：[项目地址](https://github.com/Lip0/skill.git)希望大家给个星星哦!
本项目是本人在学习慕课网视频教程的总结，如若侵权，请联系删除。视频课程地址：[课程地址](https://www.imooc.com/u/2145618/courses?sort=publish)

## 一、使用工具和技术栈
1. 开发工具：IntelliJ IDEA
2. jar包导入工具：maven
3. 所使用到的框架：
    （1）dao层：mybatis 5.1.35
    （2）数据库连接池：c3p0 0.9.1.1
    （3）service层：spring 4.1.7.RELEASE
    （4）web层：spring-web 4.1.7.RELEASE
    （5）单元测试工具：junit 4.11
    （6）日志工具：slf4j 1.7.12 
    3. 在IDEA中使用maven创建web-app项目，创建完成后项目骨架如下图。
    ![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608093138223.png)
其中Sql目录是工程创建完毕后自己手动创建的，可忽略。

# 二、dao层
## 1. 创建数据库
创建名为seckill的数据库，共包含两张表：seckill与user_login
（1）seckill表包含的字段以及字段的数据类型可通过如下建表语句看出：

```sql
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
```
（2）在seckill表中插入几条数据：

```sql
--初始化数据
insert into seckill(name,number,start_time,end_time)
values
   ('苹果手机6plus',100,'2019-06-01 00:00:00','2019-06-02 00:00:00'),
   ('ipad mini4',200,'2019-06-01 00:00:00','2019-06-02 00:00:00'),
   ('华为p30',150,'2019-06-01 00:00:00','2019-06-02 00:00:00'),
   ('索尼耳机',103,'2019-06-01 00:00:00','2019-06-02 00:00:00'),
   ('LG电视机',50,'2019-06-01 00:00:00','2019-06-02 00:00:00');
```
（3）创建user_login表

```sql
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
```
（4）seckill表各字段的属性
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608095340775.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4NTg0Nzkz,size_16,color_FFFFFF,t_70)
（5）seckill表的内容
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608095242836.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4NTg0Nzkz,size_16,color_FFFFFF,t_70)（6）user_login各字段的属性
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608095428507.png)
## 2. 配置mybatis
创建如下图所示的文件目录：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608095632347.png)
在mybatis-config.xml文件中进行配置

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <!--配置全局属性-->
    <settings>
        <!--使用jdbc的getGenerateKeys获取数据库自增主键值-->
        <setting name="useGeneratedKeys" value="true"/>
        <!--使用列别名替换列名，默认：true-->
        <setting name="useColumnLabel" value="true"/>
        <!--开启驼峰命名转换-->
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>
</configuration>
```

## 三、Spring与mybatis的整合

## 1. xml文件配置
在图示目录下创建文件spring-dao.xml
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608100154993.png)
   
配置内容如下：
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p" xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">
    <!--配置整合mytatis-->
    <!--1.配置数据库相关参数-->
    <context:property-placeholder location="classpath:jdbc.properties"/>
    <!--2.配置数据库连接池-->
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <!--配置连接池属性-->
        <property name="driverClass" value="${jdbc.diver}"/>
        <property name="jdbcUrl" value="${jdbc.url}"/>
        <property name="user" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>

        <!--配置连接池的私有属性-->
        <property name="maxPoolSize" value="30"/>
        <property name="minPoolSize" value="10"/>
        <property name="autoCommitOnClose" value="false"/>
        <!--获取连接超时时间-->
        <property name="checkoutTimeout" value="1000"/>
        <!--当获取连接失败重试次数-->
        <property name="acquireRetryAttempts" value="2"/>
    </bean>
    <!--3.配置SqlSessionFactory对象-->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <!--注入数据库连接池-->
        <property name="dataSource" ref="dataSource"/>
        <!--配置mybatis全局配置文件:mybatis-config.xml-->
        <property name="configLocation" value="classpath:mybatis-config.xml"/>
        <!--扫描entity包，使用别名-->
        <property name="typeAliasesPackage" value="com.unique.entity"/>
        <!--扫描SQL配置文件：mapper需要的xml文件-->
        <property name="mapperLocations" value="classpath:mapper/*.xml"/>
    </bean>
    <!--4.配置扫描Dao接口所在的包，动态实现Dao接口，并注入到spring容器中-->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!--注入sqlSessionFactory-->
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
        <!--给出扫描Dao接口的包-->
        <property name="basePackage" value="com.unique.dao"/>
    </bean>
</beans>
```
***关键点：***
（1）数据库本身相关的参数在图示目录下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608100432236.png)

```xml
<!--1.配置数据库相关参数-->
    <context:property-placeholder location="classpath:jdbc.properties"/>
```
这句代码去加载这个配置文件。
jdbc.properties内容如下：

```
jdbc.diver=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://127.0.0.1:3306/seckill?useUnicode=true&characterEncoding=utf8
jdbc.username=root
jdbc.password=你的数据库密码
```
***此处注意：***
一定要写成jdbc.driver/url这种形式，不能不加前缀jdbc.  否则在IDEA中打死都连接不成功数据库。两外连接不上数据库也可能是MySQL的权限问题，需要自己写SQL语句修改访问权限，具体方法自己百度。
（2）配置数据库连接池，这没什么好说的，自己修改了三处地方：
连接池中的连接数最大最小数量，
```xml
 <!--配置连接池的私有属性-->
        <property name="maxPoolSize" value="30"/>
        <property name="minPoolSize" value="10"/>
```
连接超时时间，1000毫秒

```xml
 <!--获取连接超时时间-->
        <property name="checkoutTimeout" value="1000"/>
```
失败后尝试重新连接次数为2次

```xml
<!--当获取连接失败重试次数-->
        <property name="acquireRetryAttempts" value="2"/>
```
（3）数据库连接池使用jdbc.properties中的参数连接到数据库后，注册一个为id是dataSource的spring中的bean，然后类org.mybatis.spring.SqlSessionFactoryBean使用这个bean注册一个id是sqlSessionFactory的bean，这个bean可以产生SQL语句，它还包括一下三条关键的属性配置语句。

```xml
<!--配置mybatis全局配置文件:mybatis-config.xml-->
        <property name="configLocation" value="classpath:mybatis-config.xml"/>
```
加载之前配置过的mybatis配置文件。

```xml
<!--扫描entity包，使用别名-->
        <property name="typeAliasesPackage" value="com.unique.entity"/>
```
这里是使用mybatis的核心思想之一，我们在建立了两个类如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608102017238.png)
在entity包下创建两个类Seckill与Userlogin，分别对应我们数据库中的两张表seckill与user_login，这两个类代表了表中的一行数据，它们类各自的私有属性值与各表字段相对应，我们想要读取数据库数据只需把数据读取到类的实例化对象中，再使用这个实例化对象即可。
Seckil类的私有属性：

```java
   private long seckillid;
    private String name;
    private int number;
    private Date startTime;
    private Date endTime;
    private Date createTime;
```
Userlogin的私有属性：

```java
private long seckillid;
    private String name;
    private int number;
    private Date startTime;
    private Date endTime;
    private Date createTime;
```
mapper目录下包含的dao层核心操作数据库的xml文件，这部分是核心内容，且与其他部分相关，我们在下节详细讲解，此处只提出来。

```xml
 <!--扫描SQL配置文件：mapper需要的xml文件-->
        <property name="mapperLocations" value="classpath:mapper/*.xml"/>
```

（4）配置扫描Dao接口所在的包，动态实现Dao接口，并注入到spring容器中。以sqlSessionFactory这个bean为属性值，并且配置出dao层接口所在包的位置，完成配置。接下来详细讲述Dao层接口以及mapper文件下xml文件的关系问题，也是mybatis部分的核心内容。

```xml
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!--注入sqlSessionFactory-->
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
        <!--给出扫描Dao接口的包-->
        <property name="basePackage" value="com.unique.dao"/>
    </bean>
```

## 2. Dao层的实现与spring的整合
上面已经提到过，Dao层的核心实现内容在于dao包下的接口以及mapper文件夹中的各个xml文件。先说明核心思想：dao包下给出一系列操作数据库的接口，每张表对应一个接口，各个接口有着各种的操作函数，这个自己定义，但是这个接口的实现并非是具体类而是mapper文件夹下的xml文件，xml文件写SQL语句实现接口中的各个函数，用户可自己随意编写，这也是mybatis相比于其他dao层框架的优势所在，用户可选择自己写SQL语句能够发挥出SQL编写的优势，当然mybatis也提供函数的形式操作数据库实现dao层接口。
（1）dao层接口
		***SeckillDao.java***
		文件位置：
		![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608104119907.png)
        接口包含的方法：
```java
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
```
可以看到我们在entity中定义的类Seckill使用到了，它作为一个查询方法的返回数据类型，这个印证了之前说的entity包下的类封装从数据库查询到的一条数据，封装为对象便于使用。
这个接口有三个函数，一个函数修改数据库数据，其他两个函数均是通过商品id来查询数据。函数的参数列表有注解@Param这与mapper中的xml对应，后面讲解。
函数int reduceNumber(@Param("seckillid") long seckillid,@Param("killTime") Date killTime);  是我们秒杀的时候的一个关键函数，它的作用是根据seckillid也就是商品id来秒杀，killTime是秒杀成功时用户设备的系统当前时间。
***UserloginDao.java***

```java
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
```
insertSuccessKilled函数是在用户秒杀成功后，根据用户的登录信息（userphone）和秒杀成功的商品id seckillid，将这一数据插入表user_login。
queryByIdWithSeckill函数是根据商品id seckillid和用户信息userphone去表user_login中查询某一条数据，这里查询为什么要用这两个数据呢？因为我们的秒杀系统只允许一个用户只能秒杀一种商品中的一个，但是一个用户可以秒杀几种商品，比如userphone为111111111的用户可以秒杀seckillid为1000和1001的商品各一件，那么user_login中只以userphone这一数据查询会获得两条数据；同一商品（此商品库存有N个）肯定可以被多个用户秒杀到，因而只根据seckillid查询也会查询到很多条数据，我们要查询用户和商品id唯一对应的数据，在本数据库的设计下就只能以seckillid 和userphone这两个数据去查询表。

## 3. Dao层接口的实现
在mapper目录下包含两个xml文件，如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608110037869.png)
这两个xml文件分别对应Dao层的两个接口函数SeckillDao.java、UserloginDao.java，是对这两个接口的实现。
先看SeckillDao.java的实现
***SeckillDao.xml***

```xml
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.unique.dao.SeckillDao">
    <!--目的:为dao接口方法提供sql语句配置
    即针对dao接口中的方法编写我们的sql语句-->

    <select id="queryByID" resultType="Seckill" parameterType="long">
        select *
        from seckill
        where seckill_id=#{seckillid}
    </select>

    <select id="queryAll" resultType="Seckill">
        select *
        from seckill
        order by create_time desc
        limit #{offset},#{limit}
    </select>
    <update id="reduceNumber">
        update seckill
        set number = number-1
        where seckill_id= #{seckillid}
        and start_time <![CDATA[ <= ]]> #{killTime}
        and end_time>= #{killTime}
        and number>0;
    </update>
</mapper>
```
注意： and start_time <![CDATA[ <= ]]> #{killTime}这一句中，[CDATA[ <= ]]中的[ <= ]方括号和<=之间有空格，不写空格执行SQL会报错！
mapper namespace="com.unique.dao.SeckillDao" 这条指明了当前xml对应哪个接口。
<mapper 标签下有三个标签，这三个标签对应SeckillDao.java接口中的三个函数，每一个标签有个属性id="reduceNumber"，这个id标识了对应哪个方法，注意这个id的值要和SeckillDao.java的方法名称一模一样，不能拼写有错误，这样才不会对应出错。
每个方法都有参数列表，那么各个标签中的SQL语句怎么和方法的参数对应起来呢？我们使用标识类似于： #{killTime}   #{offset},#{limit}  #{seckillid}  用#{方法的参数1} 这种方式对应方法的参数。
对于有返回值的方法，需要在xml的标签中标识出返回的数据类型，使用例如： resultType="Seckill"  。
另外在SeckillDao.java中的方法内，当参数多于一个的时候需要加上@Param注解，注解后面{}内的内容和xml中#{}内容对应，一般都把三者都写一样，便于管理。

```java
int reduceNumber(@Param("seckillid") long seckillid,@Param("killTime") Date killTime);
```
当某一个方法只有一个参数时，可不加@Param注解，但是需要在对于的xml文件的标签内加上参数类型，例如：

```java
Seckill queryByID(long seckillid);//只有一个参数
```

```xml
<!--多了一个参数parameterType-->
 <select id="queryByID" resultType="Seckill" parameterType="long">
        select *
        from seckill
        where seckill_id=#{seckillid}
    </select>
```
***UserloginDao.xml***
基本使用编写方法同上，不再赘述，贴出代码。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.unique.dao.UserloginDao">
    <!--主键冲突会报错-->
    <update id="insertSuccessKilled">
        insert ignore into user_login(seckill_id,user_phone,user_state)
        values (#{seckillid},#{userphone},0)
    </update>

    <select id="queryByIdWithSeckill" resultType="Userlogin">
        <!--如果告诉mybatis把结果映射到Userlogin同时映射seckill属性-->
        <!--可以自由控制SQL-->
        select
            sk.seckill_id,
            sk.user_phone,
            sk.create_time,
            sk.user_state,
            s.seckill_id "seckill.seckillid",
            s.name "seckill.name",
            s.number "seckill.number",
            s.start_time "seckill.start_time",
            s.end_time "seckill.end_time",
            s.create_time "seckill.create_time"
        from user_login sk
        inner join seckill s on sk.seckill_id=s.seckill_id
        where sk.seckill_id=#{seckillid} and sk.user_phone=#{userphone}
    </select>
</mapper>
```
关于mapper如何与dao包下的接口如何对应实现，xml为什么可以实现接口这是mybatis的作用。这两部分配置完成之后，spring容器中就有SeckillDao.java与Userlogin.java这两个类，以后使用直接声明实例化对象即可，不用自己再去new，比如想使用SeckillDao.java的功能，在某一个类中这样写：private SeckillDao  seckillDao; 就可使用对象seckillDao，spring会为我们自动注入，不用自己生产，这便是spring的作用之一IOC控制反转。

# 三、Service层
## 1. 配置
添加spring的配置文件，位置和文件名如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608140605143.png)
spring可以使用xml编写注册bean和注解两种方式配置，我们为了简化配置统一使用注解，配置文件spring-service.xml的内容如下：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context" xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
         http://www.springframework.org/schema/tx
         http://www.springframework.org/schema/tx/spring-tx.xsd">

    <!--扫描service包下所有使用注解的类-->
    <context:component-scan base-package="com.unique.service"/>
    <!--配置事务管理器-->
    <bean id="transactionManger" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <!--注入数据库连接池-->
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <!--配置基于注解的声明式事务
        默认使用注解来管理事务行为
    -->
    <tx:annotation-driven transaction-manager="transactionManger"/>
</beans>
```
关键点：
（1）包扫描器：

```xml
<!--扫描service包下所有使用注解的类-->
    <context:component-scan base-package="com.unique.service"/>
```
这里使用spring的IOC控制反转，通过注解的形式在spring容器中载入service包下的各个类，并且为各个类以注解标识实现自动注入。后续讲解service包下的代码会详细说明。
（2）事务管理：

```xml
<bean id="transactionManger" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <!--注入数据库连接池-->
        <property name="dataSource" ref="dataSource"/>
    </bean>
```
第二节中我们已经实现了spring与mybatis的整合，也就是spring容器中存在了数据库连接池这个bean，因此这里为事务管理注入了id为dataSource的这个数据库连接池。事务管理时spring的另外一个功能，简称AOP，事务管理是针对数据库而言的，如果service层中某一类的某一方法有增删改操作数据库的动作，那么要对这个方法加上事务控制的注解。
（3）开启注解，默认使用注解式声明事务管理

```xml
<!--配置基于注解的声明式事务
        默认使用注解来管理事务行为
    -->
    <tx:annotation-driven transaction-manager="transactionManger"/>
```

```xml
<!--这一句表示开启注解，上面配置语句后面的transaction-manager="transactionManger"表示默认使用注解来管理事务行为-->
<tx:annotation-driven />
```

## 2. service层代码

service包下包含一个接口和一个实现类，注意service层的代码都是一个接口一个实现类对应这样的编写规范，需要统一，实现类在Impl包下，实现类的类名是接口名+Impl，此后要遵守这种编写规范，这样的代码才是标准，便于团队协作和日后修改。service层代码位置和文件名如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608142615432.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4NTg0Nzkz,size_16,color_FFFFFF,t_70)
由于我们的系统比较简单，功能相对单一，因而只创建了一个接口和对应的实现类。这里说明下service的功能，service主要实现我们系统的核心业务功能，Dao层实现与数据库的交互，读写数据库，读写数据库的数据与service层交互，service的上层是web层，web层主要是与前端页面交互用作显示，因而service充当了中间角色，也是最核心的一层，它的代码主要是根据系统要求完成逻辑和数据处理等功能，下面我们就秒杀系统来看看service层都完成什么功能，以便帮助读者更好地理解service层在系统中扮演什么角色，实现什么功能。
（1）***接口***
service层怎么去设计都体现在这一层的接口如何设计，我们不能轻视这个接口，接口设计要站在“使用者”角度去设计，根据我们的系统是做什么的先搭建出功能接口，再去考虑实现，不要一开始就去考虑如何实现这个接口这一想法而限制接口的设计。
***SeckillService.java***

```java
public interface SeckillService {
    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单条秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
        throws SeckillException, RepeatKillException, SeckillCloseException;

}
```
这个接口共有四个函数，接下来逐删除线格式 一说明设计的意图：
***getSeckillList***
```java
 /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();
```
查询出表seckill下所有的数据，我们会有一个商品列表展示页面，这个页面需要所有商品的数据信息，这个函数实现。
***getById***
```java
 /**
     * 查询单条秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);
```
根据商品id去查询这个商品的信息，这一需求在我们的系统中必不可少，因而设计这一方法。
***exportSeckillUrl***
```java
/**
     * 秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);
```
比较核心，也是有特色的方式之一。首先每一个商品有一个秒杀开启时间和结束时间，我们需要判断当前系统时间是否处于开启与结束时间之内，如果在则给出一个秒杀商品的url地址，如果不在则输出系统时间和秒杀时间共三个数据。这个函数的返回类型为Exposer，这是我们专门定义用于接收这一函数返回数据而定制的，这个类的位置在：
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019060814482825.png)
我们有时候自己在一个包下创建几个类封装某些方法返回的数据，这样做符合Java语言编程的思想。
下面看看exportSeckillUrl的返回类型Exposer包含什么内容。

```java
    //是否开启秒杀
    private boolean exposed;
    //一种加密措施
    private String md5;

    private long seckillId;
    //系统当前时间(毫秒)
    private long now;

    private  long start;

    private long end;
```
以上是这个类的私有属性，布尔类型的exposed标识秒杀是否开启，还有其他字段，这些字段怎么得到，就不是Exposer这一个类的功能了，在SeckillService.java的实现类中得到，到那里我能具体讲解为啥要有这些属性。注意Exposer只是一个封装数据的类，与entity包下的类功能类似，只是用于封装数据，类中除了定义了私有属性后就是get/set方法，此外再无方法。
另外，dto包下的类还有一个重要的思想，dto包下的类是封装方法返回数据的，而方法中返回的数据有时候可能只包含一部分属性一部分不包含，因此dto包下的类要设计多种构造函数方面使用，以Exposer为例，就有两个构造函数，这些构造函数的区别在于参数列表不一样。

```java
public Exposer(boolean exposed, String md5, long seckillId) {
        this.exposed = exposed;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    public Exposer(boolean exposed,long seckillId, long now, long start, long end) {
        this.exposed = exposed;
        this.seckillId = seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }
```
***executeSeckill***

```java
 /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
        throws SeckillException, RepeatKillException, SeckillCloseException;
```
这个方法是执行秒杀的函数，输入参数有三个，为什么有这三个下一节讲解，这里说明下返回类型和抛出的异常。
返回类型为SeckillExecution在dto包下，包以下私有属性：

```java
    private  long seckillId;
    //秒杀执行状态
    private int state;
    //状态表示
    private String stateInfo;
    //秒杀成功对象
    private Userlogin userlogin;
```
各个属性的含义注释已经说明清楚了，目前只需知道返回的数据类型中包含啥数据就可以了，具体为啥有这些数据，下节讲解。
抛出的异常也是代码设计的一大亮点之一，我们秒杀的过程中肯定存在重复秒杀、秒杀关闭等情况，我们把这些都作为异常抛出，处理的很得当。
（2）***接口的实现类SeckillServiceImpl.java***
注入的依赖，都是Dao层的，我们使用这两个类的对象在service层操作数据库。

```java
    //需要注入Service依赖
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private UserloginDao userloginDao;
```
这句使用slf4j作为日志管理工具，用工厂类实例化一个对象logger方便在代码中使用日志功能。
```java
private Logger logger=LoggerFactory.getLogger(this.getClass());
```
sl4j需要配置，配置文件位置和文件名如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608151600207.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4NTg0Nzkz,size_16,color_FFFFFF,t_70)文件内容如下：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type
             ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="debug">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```
其中，class="ch.qos.logback.core.ConsoleAppender"表示日志内容输出到控制台，这里可以修改把日志输出到指定的本地文件。

```xml
<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
```
格式化日志输出的格式。

***getSeckillList***

```java
 @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,10);
    }
```
查询多条商品数据，装到一个List，查询限制为10条，真正的查询使用自动注入的Dao层对象seckillDao实现。
***getById***

```java
@Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryByID(seckillId);
    }
```
同理，不再赘述。
***exportSeckillUrl***

```java
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
```
3行使用Dao层的对象根据商品id查询数据库，如果查询结果为空直接返回，设置Exposer的秒杀开启属性为false，传入查询的商品id，其他属性不管。
7-9行得到三个时间：startTime 、endTime、nowTime其中前两个时间是查询数据库得到的，后面的是系统当前时间。
10-11行判断当前时间是否处于秒杀开启时间与结束时间之内，如果不在返回设置Exposer的秒杀开启属性为false，一并返回商品id和三个时间。
16行到这里的时候，说明前面的判断都没进去，系统时间处于秒杀时间段内，这时要暴露秒杀地址，为了防止用户使用第三方软件工具秒杀，我们在返回的数据中包含字段md5，md5根据seckillId生成，getMD5函数实现。

```java
private String getMD5(long seckillID){
        String base=seckillID+"/"+slat;
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
```
slat称为盐值，在类中我们已经定义为私有属性了，字符串的内容随便敲写，越复杂越好。

```java
//MD5盐值字符串，用于混淆MD5
    private final String slat="shnvossosjiow324#$##$#%#FVSVWW@@!!^^^H";
```
17行返回Exposer类的秒杀开启属性为true，返回md5字符串，返回商品id。

***executeSeckill***

```java
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
```
由于这个方法对数据库中的两个表都做了增、改操作，因此加上了事务支持的注解。
10行判断md5值，输入的md5值与getMD5(seckillId)比较，这里重点说明一下为什么要使用md5这个东西。在web层会自动注入一个SeckillService对象，姑且命名为seckillService，调用这个对象的exportSeckillUrl方法，返回一个包含md5的数据，后面使用md5拼接出一个URL，这个URL就是秒杀商品的地址，这个地址中包含md5字符串，用户在浏览器点击秒杀按钮后在web层会有一个函数接收这个点击事件的响应，这个函数可以读取到提交的秒杀地址的URL，这个响应函数中调用executeSeckill函数，便会解析出URL中的字符串md5作为函数参数，至此我们知道了md5这个字符串的传递过程。那么加入这个有什么用呢？想象如果没有md5，那么秒杀商品的URL就可以根据这个浏览器地址栏的地址观察猜测出来，这时候可以使用第三方工具无限暴力发送这个URL的请求，最终要么导致系统崩溃，要么对其他用户不公平。md5值与getMD5(seckillId)比较都是同一对象的，因而可以比较通过。
18行调用Dao层对象操作数据库，如果操作未成功，抛出秒杀已经关闭的异常。
24行，在减库存成功后，在user_login表中插一条秒杀成功数据，如果插入失败，说明用户重复秒杀了。
31行，所有的操作都成功了，就返回数据，31得到插入成功的user_login表中的数据。
32行，最终返回的数据类型封装到SeckillExecution中，返回。这里秒杀的状态表示我们使用了枚举：SeckillStateEnum.SUCCESS。它在枚举类SeckillStateEnum中定义，文件位置如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608162002736.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4NTg0Nzkz,size_16,color_FFFFFF,t_70)内容如下：

```java
/**
 * 使用枚举表述常量数据字段
 */
public enum  SeckillStateEnum {
    SUCCESS(1,"秒杀成功"),
    END(0,"秒杀结束"),
    REPEAT_KILL(-1,"重复秒杀"),
    INNER_ERROR(-2,"系统异常"),
    DATA_REWRITE(-3,"数据篡改");
    private int state;
    private  String stateInfo;

    SeckillStateEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }
    public static SeckillStateEnum stateOf(int index){
        for (SeckillStateEnum state: values()) {
            if(state.getState()==index){
                return state;
            }
        }
        return null;
    }
}
```
注意：我们在这里把重复秒杀、秒杀关闭等不能秒杀的情况都封装为异常抛出，这样做符合编程规范，这几个自己定义的异常类位置和文件如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608162329699.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4NTg0Nzkz,size_16,color_FFFFFF,t_70)至此，service层讲解完毕，接下来讲解web层。

# 四、web层

## 1. 配置
配置文件位置及文件名：
![在这里插入图片描述](https://img-blog.csdnimg.cn/201906081630107.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4NTg0Nzkz,size_16,color_FFFFFF,t_70)内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/mvc
       http://www.springframework.org/schema/mvc/spring-mvc.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">
    <!--配置springMVC-->
    <!--开启springMVC注解模式-->
    <!--简化配置：
        1.自动注册，可以使用注解映射url对应的handler方法
        2.默认提供一系列：数据绑定，数字和日期的format，xml和json的默认读写支持
     -->
    <mvc:annotation-driven/>

    <!--静态资源默认servlet配置
        1.加入对静态资源的处理，js/图片
        2.允许使用"/"做整体映射
    -->
    <mvc:default-servlet-handler/>
    <!--配置视图解析器-->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
        <property name="prefix" value="/WEB-INF/jsp/"/>
        <property name="suffix" value=".jsp"/>
    </bean>

    <!--包扫描器-->
    <context:component-scan base-package="com.unique.web"/>
</beans>
```
关键点：
（1）开启注解模式

```xml
 <mvc:annotation-driven/>
```
（2）加入对静态资源的处理

```xml
 <!--静态资源默认servlet配置
        1.加入对静态资源的处理，js/图片
        2.允许使用"/"做整体映射
    -->
    <mvc:default-servlet-handler/>
```
（4）视图解析器和包扫描器便不说了，常见的操作。
***web.xml配置***
配置的含义看注释即可。
```xml
<!DOCTYPE web-app PUBLIC
 "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
 "http://java.sun.com/dtd/web-app_2_3.dtd" >

<web-app>
  <display-name>Archetype Created Web Application</display-name>
  <!--配置DispatcherServlet-->
  <servlet>
    <servlet-name>seckill_dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!--配置SpringMVC需要加载的配置文件-->
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring/spring-*.xml</param-value>
    </init-param>
  </servlet>
  <servlet-mapping>
    <servlet-name>seckill_dispatcher</servlet-name>
    <!--默认匹配所有的请求-->
    <url-pattern>/</url-pattern>
  </servlet-mapping>
</web-app>

```

前端页面内容的文件位置和文件名如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608165810778.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4NTg0Nzkz,size_16,color_FFFFFF,t_70)
## 2.  具体类SeckillController
文件位置和文件名：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190608164726499.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4NTg0Nzkz,size_16,color_FFFFFF,t_70)文件内容：

```java
@Controller
@RequestMapping("/seckill")//模块     url:模块/资源/{id}/细分
public class SeckillController {
    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "list",method = RequestMethod.GET)
    public String list(Model model){
        //获取列表页
        List<Seckill> seckillList = seckillService.getSeckillList();
        model.addAttribute("list",seckillList);
        //list.jsp+ model= ModelAndView
        return  "list";
    }

    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model){

        if(seckillId==null){
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if(seckill==null){
            return  "forward:/seckill/list";
        }
        model.addAttribute("seckill",seckill);

        return "detail";
    }

    @RequestMapping(value = "/{seckillId}/exposer",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer>  exposer(@PathVariable("seckillId") Long seckillId){
        SeckillResult<Exposer> result;
        try{
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result=new SeckillResult<Exposer>(true,exposer);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            result=new SeckillResult<Exposer>(false,e.getMessage());
        }
        return result;
    }
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "killPhone",required = false) Long userPhone){
        if(userPhone==null){
            return new  SeckillResult<SeckillExecution>(false,"用户未注册");
        }
        SeckillResult<SeckillExecution> result;
        try {
            SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone,md5);
            return new SeckillResult<SeckillExecution>(true,seckillExecution);
        }catch (RepeatKillException e){
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(false,execution);
        }catch (SeckillCloseException e){
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStateEnum.END);
            return new SeckillResult<SeckillExecution>(false,execution);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(false,execution);
        }
    }

    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time(){
        Date now=new Date();
        return new SeckillResult<Long>(true,now.getTime());
    }
}
```
1行注解这个类是个@Controller
2行注解这个类的映射地址@RequestMapping("/seckill")，以后要访问这个类就使用

```
http://ip:port/seckill/
```
7行自动注入SeckillService对象seckillService。
***list***
10行为一个方法，它是url为http://ip:port/seckill/list地址请求的响应函数，请求方式为get，在注解@RequestMapping中都写清楚了。13行比较关键，13行是方法返回给前端页面的数据，前端页面要接收这个数据并做显示。

```java
model.addAttribute("list",seckillList);
```
15行的返回值表示请求这个URLhttp://ip:port/seckill/list后跳转到页面list.jsp。那么13行返回数据到前端jsp页面，前端页面如何接收这个数据呢？当前系统中，我们使用了两中web层返回给前端数据的方式，一种是13行的方式，一种是返回json数据，这种方式在37和52行的方法上用到，后面讲解。
13行返回的数据为seckillList，起了一个别名list，在jsp页面中使用如下语句获取：

```js
var items="${list}"
```
而我们的list是个列表，怎么获取遍历列表元素呢？使用如下方式：

```js
<c:forEach items="${list}" var="sk">
    <tr>
        <td>${sk.name}</td>
        <td>${sk.number}</td>
        <td>
            <fmt:formatDate value="${sk.startTime}" pattern= "yyyy-MM-dd HH:mm:ss" />
        </td>
        <td>
            <fmt:formatDate value="${sk.endTime}" pattern= "yyyy-MM-dd HH:mm:ss" />
        </td>
        <td>
            <fmt:formatDate value="${sk.createTime}" pattern= "yyyy-MM-dd HH:mm:ss" />
        </td>
        <td>
            <a class="bth btn-info" href="/seckill/${sk.seckillid}/detail" target="_blank">详情</a>
        </td>
    </tr>
</c:forEach>
```
***detail***
19行的方法映射的URL为http://ip:port/seckill/{seckillId}/detail ，url中的{}表示这是一个参数，方法中使用注解获取这个url中的参数，作为方法的输入参数之一。

```java
@PathVariable("seckillId") Long seckillId
```
22与26行如果根据seckillId查询商品失败，就将页面重定向到商品列表页。
28行返回数据到前端页面，前端页面使用如下的方式接收数据：

```js
 <h1>${seckill.name}</h1>
```
***exposer***
57行方法的URL为http://ip:port/seckill//{seckillId}/exposer，请求方式为post。返回给前端页面的是json数据，返回json数据要加上注解@ResponseBody，我们使用类SeckillResult做了封装。这个类包含三个私有属性：

```java
    private  boolean success;
    private  T data;
    private String error;
```
success表示当前操作成功与否，如果成功就要初始data属性，如果失败初始化error，给出错误信息，因而这个类有两个构造方法。

```java
public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
```
在seckill.js文件的53行，我们使用js的方法去请求http://ip:port/seckill//{seckillId}/exposer这个地址，如下所示：

```js
 $.post(seckill.URL.exposer(seckillId),{},function (result) {
            //在回调函数中执行，执行交互流程
            if(result && result['success']){
                var exposer=result['data'];
                if(exposer['exposed']){
                    //开启秒杀
                    //获取秒杀地址
                    var md5=exposer['md5'];
                    var killUrl=seckill.URL.execution(seckillId,md5);
                    //绑定一次点击事件
                    $('#killBtn').one('click',function () {
                        //执行秒杀请求的操作
                        //1.先禁用按钮
                        $(this).addClass('disabled');
                        //2.发送秒杀请求,执行秒杀
                        $.post(killUrl,{},function (result) {
                            if(result && result['success']){
                                var killResult=result['data'];
                                var state=killResult['state'];
                                var stateInfo=killResult['stateInfo'];
                                console.log("========="+stateInfo);//TODO
                                //显示秒杀结果
                                node.html('<span class="label label-success">'+stateInfo+'</span>');
                            }
                        });
                    });
                    node.show();
                }else{
                    //未开启秒杀
                    var now=exposer['now'];
                    var start=exposer['start'];
                    var end=exposer['end'];
                    //重新进入倒计时逻辑
                    seckill.countDown(seckillId,now,start,end);
                }
            }else {
                console.log('result:'+result);
            }
        });
```
1行是使用$.post去请求，参数有seckill.URL.exposer(seckillId)是请求的url地址，相当于http://ip:port/seckill/{seckillId}/exposer。
function (result)是请求响应后的回调函数，result就是前端页面接收到的后端响应来的json数据`SeckillResult<Exposer>`
使用这个json响应数据的各个字段的方式读者可通过看3-8行以及17-23行明白，不再赘述。
***execute***
请求的URL地址为：http://ip:port/seckill/{seckillId}/{md5}/execution

```java
 @RequestMapping(value = "/{seckillId}/{md5}/execution",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "killPhone",required = false) Long userPhone)
```
请求方式为post，响应的json数据为 `SeckillResult<SeckillExecution>`
在seckill.js中的68行请求，

```js
 $.post(killUrl,{},function (result) {
                            if(result && result['success']){
                                var killResult=result['data'];
                                var state=killResult['state'];
                                var stateInfo=killResult['stateInfo'];
                                console.log("========="+stateInfo);//TODO
                                //显示秒杀结果
                                node.html('<span class="label label-success">'+stateInfo+'</span>');
                            }
                        });
```
***time***
请求的url为http://ip:port/seckill//time/now
返回给前端页面的也是json，在seckill.js文件的133行

```js
 $.get(seckill.URL.now(),{},function (result) {
                if(result && result['success']){
                    var nowTime=result['data'];
                    //时间判断,计时交互
                    seckill.countDown(seckillId,nowTime,startTime,endTime);
                }else{
                    console.log('result='+result);//TODO
                }
            });
```

# 五、总结
1. 本文主要讲解了Dao层、service层的设计，以及这两层之间的关系，设计架构理念。
2. 讲解了系统涉及到的各种xml配置文件的意义，以及含义。
3. 对于web层和前端页面讲解的比较粗糙还望读者见谅。
4. 后续会讲解针对这一系统的优化，以便系统可以抵御一定的高并发访问量。
5. seckill秒杀系统是本人根据慕课网学习的，此文是阶段性的总结文章，在此感谢慕课网，感谢慕课网用户 yijun zhang的讲解。贴出慕课网课程视频地址：[慕课网seckill课程地址](https://www.imooc.com/u/2145618/courses?sort=publish)

