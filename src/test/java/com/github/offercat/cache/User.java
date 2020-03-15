package com.github.offercat.cache;

import com.github.offercat.cache.extra.CacheId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 20:42:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements CacheId<String> {

    private String id;
    private String username;

    @Override
    public String getObjectId() {
        return id;
    }
}
