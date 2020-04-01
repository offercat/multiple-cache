package com.github.offercat.cache;

import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.inte.ClusterCache;
import com.github.offercat.cache.inte.DirectCache;
import com.github.offercat.cache.inte.LocalCache;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
class MultipleCacheTest {

    @Autowired
    private MultipleCache multipleCache;

    @Autowired
    private CacheProperties properties;

    @Autowired
    private ClusterCache clusterCache;

    @Autowired
    private LocalCache localCache;

    @Autowired
    private DirectCache directCache;

    @BeforeEach
    void setUp() {
        ItemProperties clusterProperties = properties.getConfig().get("cluster");
        clusterProperties.setTimeout(2);
        clusterProperties.setTimeunit(TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("回调函数拿到 | 本地缓存拿到 | 集群缓存拿到")
    void get() throws InterruptedException {
        String key = UUID.randomUUID().toString();
        User user = new User(key, "xutong");
        User result = multipleCache.get(key, () -> user);
        Assertions.assertEquals(user, result);


        User result2 = multipleCache.get(key, () -> null);
        Assertions.assertEquals(user, result2);

        Thread.sleep(1000);

        User result3 = multipleCache.get(key, () -> null);
        Assertions.assertEquals(user, result3);

        Thread.sleep(1000);
        User result4 = multipleCache.get(key, () -> null);
        Assertions.assertNull(result4);
    }

    @Test
    @DisplayName("回填null | 本地缓存拿到 | 集群缓存拿到")
    void get2() throws InterruptedException {
        String key = UUID.randomUUID().toString();
        User result = multipleCache.get(key, new User(), () -> null);
        Assertions.assertNull(result);

        User result2 = multipleCache.get(key, new User(), () -> null);
        Assertions.assertNull(result2);

        Thread.sleep(1000);

        User result3 = multipleCache.get(key, new User(), () -> null);
        Assertions.assertNull(result3);
    }


    @Test
    @DisplayName("回调函数拿到3个 | 本地缓存拿到3个 | 集群缓存拿到3个")
    void getMul() throws InterruptedException {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        User user1 = new User(id1, "user1");
        User user2 = new User(id2, "user2");
        User user3 = new User(id3, "user3");

        List<String> ids = Arrays.asList(id1, id2, id3);
        Collection<User> users = multipleCache.getMul(
                ids,
                id -> "test_key_" + id,
                otherIds -> Arrays.asList(user1, user2, user3)
        );
        Assertions.assertTrue(users.contains(user1));
        Assertions.assertTrue(users.contains(user2));
        Assertions.assertTrue(users.contains(user3));

        Collection<User> users2 = multipleCache.getMul(
                ids,
                id -> "test_key_" + id,
                otherIds -> Collections.emptyList()
        );
        Assertions.assertTrue(users2.contains(user1));
        Assertions.assertTrue(users2.contains(user2));
        Assertions.assertTrue(users2.contains(user3));

        Thread.sleep(1000);

        Collection<User> users3 = multipleCache.getMul(
                ids,
                id -> "test_key_" + id,
                otherIds -> Collections.emptyList()
        );
        Assertions.assertTrue(users3.contains(user1));
        Assertions.assertTrue(users3.contains(user2));
        Assertions.assertTrue(users3.contains(user3));
    }


    @Test
    @DisplayName("回调函数拿到3个 | 本地2个 集群1个 | otherIds = [XXX] 各一个 | 本地3个")
    void getMul2() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        User user1 = new User(id1, "user1");
        User user2 = new User(id2, "user2");
        User user3 = new User(id3, "user3");

        List<String> ids = Arrays.asList(id1, id2, id3);
        Collection<User> users = multipleCache.getMul(
                ids,
                id -> "test_key_" + id,
                otherIds -> Arrays.asList(user1, user2, user3)
        );
        Assertions.assertTrue(users.contains(user1));
        Assertions.assertTrue(users.contains(user2));
        Assertions.assertTrue(users.contains(user3));

        System.out.println("------------------------------------------------------------------------");
        localCache.del("test_key_" + id1);
        Collection<User> users2 = multipleCache.getMul(
                ids,
                id -> "test_key_" + id,
                otherIds -> Collections.emptyList()
        );
        Assertions.assertTrue(users2.contains(user1));
        Assertions.assertTrue(users2.contains(user2));
        Assertions.assertTrue(users2.contains(user3));

        System.out.println("------------------------------------------------------------------------");
        localCache.del("test_key_" + id1);
        localCache.del("test_key_" + id2);
        clusterCache.del("test_key_" + id1);
        Collection<User> users3 = multipleCache.getMul(
                ids,
                id -> "test_key_" + id,
                otherIds -> {
                    log.info("otherIds = {}", otherIds);
                    return Collections.singletonList(user1);
                }
        );
        Assertions.assertTrue(users3.contains(user1));
        Assertions.assertTrue(users3.contains(user2));
        Assertions.assertTrue(users3.contains(user3));


        System.out.println("------------------------------------------------------------------------");
        Collection<User> users4 = multipleCache.getMul(
                ids,
                id -> "test_key_" + id,
                otherIds -> Collections.emptyList()
        );
        Assertions.assertTrue(users4.contains(user1));
        Assertions.assertTrue(users4.contains(user2));
        Assertions.assertTrue(users4.contains(user3));
    }

    @Test
    @DisplayName("回调1个, 回填2个 | 本地3个")
    void getMul3() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        User user1 = new User(id1, "user1");
        User user2 = new User(id2, "user2");
        User user3 = new User(id3, "user3");

        List<String> ids = Arrays.asList(id1, id2, id3);
        Collection<User> users = multipleCache.getMul(
                ids,
                id -> "test_key_" + id,
                new User(),
                otherIds -> Collections.singletonList(user1)
        );
        Assertions.assertTrue(users.contains(user1));
        Assertions.assertFalse(users.contains(user2));
        Assertions.assertFalse(users.contains(user3));

        Collection<User> users2 = multipleCache.getMul(
                ids,
                id -> "test_key_" + id,
                new User(),
                otherIds -> Collections.emptyList()
        );
        Assertions.assertTrue(users2.contains(user1));
        Assertions.assertFalse(users2.contains(user2));
        Assertions.assertFalse(users2.contains(user3));
    }
}