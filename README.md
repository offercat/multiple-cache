## OfferCat 多级缓存框架
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

### ------ 目录 ------
##### 一、maven 依赖
##### 二、可配置参数详解
##### 三、核心方法使用介绍
##### 四、API 汇总
##### 五、单独使用集群缓存/单独使用本地缓存
##### 六、缓存重写
----
### 一、`maven` 依赖
```xml
<dependency>
    <groupId>com.github.offercat</groupId>
    <artifactId>multiple-cache</artifactId>
    <version>1.0.0.RELEASE</version>
</dependency>
```
----
### 二、CacheProperties 可配置参数详解
**配置方式：`multiple.cache.config.`开头，然后加上缓存的名称，如果是本地缓存，配置前缀即为`multiple.cache.config.local.`**
```properties
# 本地缓存动态开关
multiple.cache.config.local.enable=true
# 本地缓存统一过期时间
multiple.cache.config.local.timeout=60
# 本地缓存统一过期时间单位
multiple.cache.config.local.timeunit=seconds
# 本地缓存最大容量
multiple.cache.config.local.max-size=2000
# 本地缓存过期模式 tti/ttl
multiple.cache.config.local.expire-mode=tti
# 是否本地缓存的广播
multiple.cache.config.local.broadcast-enable=true

# 直接缓存动态开关
multiple.cache.config.direct.enable=false
# 直接缓存统一过期时间
multiple.cache.config.direct.timeout=120
# 直接缓存统一过期时间单位
multiple.cache.config.direct.timeunit=seconds
# 直接缓存最大容量
multiple.cache.config.direct.max-size=2000

# 集群缓存动态开关
multiple.cache.config.cluster.enable=true
# 集群缓存地址
multiple.cache.config.cluster.address=127.0.0.1
# 集群缓存端口
multiple.cache.config.cluster.port=6379
# 集群密码
multiple.cache.config.cluster.password=123456
# 集群统一过期时间
multiple.cache.config.cluster.timeout=30
# 集群缓存统一过期时间单位
multiple.cache.config.cluster.timeunit=seconds
```
----
### 三、核心API使用介绍
#### 1、注入`com.github.offercat.cache.MultipleCache`
```java
@Autowired
private MultipleCache multipleCache;
```
#### 2、简单通用的查询接口
**给定缓存key，依次从多级缓存获取，如果多级缓存没有指定值，则穿透缓存，调用回调函数并自动回填**
```java
User result = multipleCache.get(key, () -> userDao.findById(id));
```
#### 3、null 值回填，防止DoS攻击
**给定缓存key，依次从多级缓存获取，如果回调函数拿到null值，会将给定的值回填到缓存，防止暴露调用穿透缓存而打垮数据库**
```java
User result = multipleCache.get(key, new User(), () -> userDao.findById(id));
```
#### 4、批量获取，最小化IO次数
**批量接口就有意思了，假如你需要100个对象，获取方式是分级获取的，如果一次性从本地缓存中拿到是最好的情况了，如果本地缓存只拿到50个，那
接下来的50个从后面的缓存中去取。当然，有可能缓存中拿到了80个，还有20个就交给回填策略吧！是不是很给力呢！**
```java
Collection<User> users = multipleCache.getMul(
        ids,                                            // 主键集合
        id -> "cache_key_" + id,                        // 使用主键生成缓存 key 的策略
        otherIds -> userDao.findByIdList(otherIds)      // 回填策略
);
```
#### 5、批量获取，指定 null 值
```java
Collection<User> users = multipleCache.getMul(
        ids,                                            // 主键集合
        id -> "cache_key_" + id,                        // 使用主键生成缓存 key 的策略
        new User(),                                     // 给定一个回填的 null 值，如果回源策略去到null，会自动回填
        otherIds -> userDao.findByIdList(otherIds)      // 回填策略
);
```
----
### 四、API 汇总
```java
/**
 * 多级缓存核心接口
 * Multi level cache core interface
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 01:15:39
 */
public interface MultipleCache {

    /**
     * 逐级获取对象后回填
     * Backfill after getting objects level by level
     *
     * @param key      cache key
     * @param callback callback function
     * @return 对象
     */
    <T extends Serializable> T get(String key, Supplier<T> callback);


    /**
     * 逐级获取对象后回填，带null值回填
     * Backfill after getting objects level by level, contains null value
     *
     * @param key       cache key
     * @param nullValue null value, prevent duplicate null cache penetration
     * @param callback  callback function
     * @return 对象
     */
    <T extends Serializable> T get(String key, T nullValue, Supplier<T> callback);


    /**
     * 获逐级批量获取对象后回填
     * Backfill after obtaining objects in batch level by level
     *
     * @param objectIds        object unique ID list
     * @param cacheKeyGenerate cache key generation strategy
     * @param getMulFunction   callback function
     * @return 对象
     */
    <T extends CacheId<V>, V> Collection<T> getMul(Collection<V> objectIds,
                                                   CacheKeyGenerate<V> cacheKeyGenerate,
                                                   GetMulFunction<T, V> getMulFunction);


    /**
     * 获逐级批量获取对象后回填，带null值回填
     * Backfill after obtaining objects in batch level by level, contains null value
     *
     * @param objectIds        object unique ID list
     * @param cacheKeyGenerate cache key generation strategy
     * @param nullValue        null value, prevent duplicate null cache penetration
     * @param getMulFunction   callback function
     * @return 对象
     */
    <T extends CacheId<V>, V> Collection<T> getMul(Collection<V> objectIds,
                                                   CacheKeyGenerate<V> cacheKeyGenerate,
                                                   T nullValue,
                                                   GetMulFunction<T, V> getMulFunction);


    /**
     * 逐级设置缓存对象
     * Set cache object level by level
     *
     * @param key   cache key
     * @param value object
     */
    <T extends Serializable> void set(String key, T value);


    /**
     * 批量逐级设置缓存对象
     * Set multi cache object level by level
     *
     * @param keyValues key-value mapping
     */
    <T extends Serializable> void setMul(Map<String, T> keyValues);

    /**
     * 逐级删除缓存对象
     * Del multi cache object level by level
     *
     * @param key cache key
     */
    void del(String key);

    /**
     * 逐级批量删除缓存对象
     *
     * @param keys cache key collection
     */
    void delMul(Collection<String> keys);
}
```
----
### 五、单独使用集群缓存/直接缓存/本地缓存
**业务场景错综复杂，某些场景 `MultipleCache` 并不能满足，那就把缓存单独拿出来使用吧**
```java
@Autowired
private ClusterCache clusterCache;

@Autowired
private LocalCache localCache;

@Autowired
private DirectCache directCache;
```
----
### 六、缓存重写
#### 1、单级重写
**有时候，我们并不想使用框架中给定的默认实现，比如想自己做一些性能优化，可以集成相应的抽象类，重写实现接口即可，spring boot 会优先注入
自定义的缓存哦！比如，你想自己实现本地缓存，那就重写他并注册到IOC吧**
```java
// 将 CacheFactory 通过方法参数注入，然后用缓存工厂创建一个自定义的本地缓存，覆盖默认的本地缓存
@Bean
LocalCache localCache(CacheFactory cacheFactory, CacheProperties properties) {
    ItemProperties localProperties = properties.getConfig().get("local");
    localProperties.setTimeout(1);
    localProperties.setTimeunit(TimeUnit.SECONDS);
    return cacheFactory.getLocalCacheInstance();
}
```

#### 1、重写 `MultipleCache`
**默认的缓存实现次序是`localCache` -> `directCache` -> `clusterCache`。假如你觉得这个顺序不适用具体业务场景，那就自定义吧。
`MultipleCache` 是基础责任链模式实现的，与`netty`如出一辙。我们来试试，把次序修改为 `clusterCache` -> `localCache` -> `directCache`**
```java
/**
 * 多级缓存，默认集成 Caffeine EhCache Redis 三级缓存，可以重写它并返回自定义的多级缓存
 * Multilevel caching, which integrates Caffeine EhCache Redis three level cache by default, can rewrite it and return to a custom multilevel cache
 */
@Bean
@ConditionalOnMissingBean(MultipleCache.class)
MultipleCache multipleCache(LocalCache localCache, DirectCache directCache, ClusterCache clusterCache) {
    // 这一句是默认实现次序，我们只需要改变一下顺序即可
    // localCache.setNext(directCache).setNext(clusterCache);

    // 将 clusterCache 放在链表头结点，是不是很简单
    clusterCache.setNext(localCache).setNext(directCache);
    // 当然用户也可以不使用默认的 localCache，directCache 和 clusterCache，自定义，插入中间节点

    return new MultipleCacheImpl(localCache, cacheProperties);
}
```