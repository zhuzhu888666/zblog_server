package cc.ztzhome.zblog.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 提供常用的 Redis 数据操作方法
 */
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // =============================common============================

    /**
     * 查找所有匹配 pattern 的 key
     *
     * @param pattern 匹配模式 (e.g. "token:user:*")
     * @return Set 键集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间（秒）
     * @return Boolean 是否成功
     */
    public Boolean expire(String key, long time) {
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 根据键获取过期时间
     *
     * @param key 键
     * @return Long 剩余时间（秒），-1 表示永不过期，-2 表示 key 不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return Boolean 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除缓存
     *
     * @param key 可变参数，支持多个 key
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }

    // ============================String============================

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间（秒）
     */
    public void set(String key, Object value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存值
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 递增
     *
     * @param key 键
     * @param delta 要增加的数值（必须大于 0）
     * @return Long 递增后的值
     */
    public Long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于 0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key 键
     * @param delta 要减少的数值（必须大于 0）
     * @return Long 递减后的值
     */
    public Long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于 0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键，不能为 null
     * @param item 项，不能为 null
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取 hashKey 对应的所有键值
     *
     * @param key 键
     * @return Map 对象
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     */
    public void hmset(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间（秒）
     */
    public void hmset(String key, Map<String, Object> map, long time) {
        redisTemplate.opsForHash().putAll(key, map);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * 向一张 hash 表中放入数据
     *
     * @param key   键
     * @param item  项
     * @param value 值
     */
    public void hset(String key, String item, Object value) {
        redisTemplate.opsForHash().put(key, item, value);
    }

    /**
     * 向一张 hash 表中放入数据并设置时间
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间（秒）
     */
    public void hset(String key, String item, Object value, long time) {
        redisTemplate.opsForHash().put(key, item, value);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * 删除 hash 表中的值
     *
     * @param key  键
     * @param items 项，可多个
     */
    public void hdel(String key, Object... items) {
        redisTemplate.opsForHash().delete(key, items);
    }

    // ============================set=============================

    /**
     * 根据 key 获取 Set 中的所有值
     *
     * @param key 键
     * @return Set 值集合
     */
    public Set<Object> sGet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 根据 value 判断是否是 Set 中的元素
     *
     * @param key   键
     * @param value 值
     * @return Boolean 是否包含
     */
    public Boolean sHasKey(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 将数据放入 Set 缓存
     *
     * @param key    键
     * @param values 值，可以多个
     * @return Long 成功插入的数量
     */
    public Long sSet(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 将 Set 数据放入缓存并设置时间
     *
     * @param key    键
     * @param time   时间（秒）
     * @param values 值，可以多个
     * @return Boolean 是否成功
     */
    public Boolean sSetEx(String key, long time, Object... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        return expire(key, time) && count > 0;
    }

    /**
     * 移除 Set 中的值
     *
     * @param key    键
     * @param values 值，可以多个
     * @return Long 移除数量
     */
    public Long setRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    // ===============================List=================================

    /**
     * 获取 list 缓存的内容
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置（如 0 到 -1 表示所有）
     * @return List 值列表
     */
    public List<Object> lGet(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取 list 缓存的长度
     *
     * @param key 键
     * @return Long 长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 通过索引获取 list 中的某个值
     *
     * @param key   键
     * @param index 索引（从 0 开始）
     * @return 值
     */
    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 将值插入到 list 的头部
     *
     * @param key   键
     * @param value 值
     * @return Boolean 是否成功
     */
    public Boolean lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
        return true;
    }

    /**
     * 将值插入到 list 的尾部
     *
     * @param key   键
     * @param value 值
     * @return Boolean 是否成功
     */
    public Boolean rPush(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
        return true;
    }

    /**
     * 将 list 插入到缓存中并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间（秒）
     * @return Boolean 是否成功
     */
    public Boolean lSet(String key, Object value, long time) {
        redisTemplate.opsForList().rightPush(key, value);
        return expire(key, time);
    }

    /**
     * 根据索引修改 list 中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void lUpdateIndex(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 移除 N 个值为 value 的元素
     *
     * @param key    键
     * @param count  移除多少个
     * @param value  值
     * @return Long 移除数量
     */
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }
}