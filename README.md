### OfferCat 多级缓存框架
**作者: `徐通` `myimpte@163.com`**

**简介：
<br/>1、本服务实现了通用的多级缓存策略，本地缓存、直接缓存、集群缓存、数据源。通用实现与架构解耦，自定义，可拓展。
<br/>2、服务内置了缓存的读取策略与回填策略：先读取本地缓存，本地缓存没有再读取集群缓存，如果集群缓存没有则读取数据源，
数据进行依次回填。
<br/>3、单级缓存热插拔，实现动态降级。
<br/>4、本框架支持集群本地缓存实时同步，当单个节点修改数据，缓存将广播到其他节点，并做时间戳校验，保证数据实时性，一致性。
下一次请求将穿透本地到达集群，以达到集群多节点数据一致性。
<br/>5、每一级缓存都是可配置的，使用者可以选择开箱即用的三级缓存，也可以自定义缓存。
<br/>6、同级缓存可以面向接口进行不同实现，适应不同的中间件环境。
<br/>7、动态日志开关，动态广播开关，动态缓存开关**

#### ------ 目录 ------
##### 1、maven 依赖
##### 2、可配置参数详解
##### 3、简单使用介绍
##### 4、API介绍
##### 5、单独使用集群缓存/单独使用本地缓存
##### 6、多数据源配置

#### 1、`maven` 依赖
```xml
<dependency>
    <groupId>com.github.offercat</groupId>
    <artifactId>multiple-cache</artifactId>
    <version>1.0.0.RELEASE</version>
</dependency>
```

#### 2、CacheProperties 可配置参数详解
##### （1）`properties`参数说明：
**配置方式：`multiple.cache.config.`开头，然后加上本级缓存的名称，比如下面是本地缓存，名称为`local`，所以配置前缀为`multiple.cache.config.local.`**
```properties
# 配置方式
multiple.cache.config.local.enable=true
multiple.cache.config.local.timeout=60
multiple.cache.config.local.timeunit=seconds
multiple.cache.config.local.max-size=2000
multiple.cache.config.local.broadcast-enable=true
```