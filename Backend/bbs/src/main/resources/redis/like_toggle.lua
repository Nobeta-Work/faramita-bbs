-- 通用点赞切换脚本：原子修改点赞状态并写入 Redis Outbox。
-- KEYS[1] = 点赞状态 Set，例如 like:blog/comment:{blogId}/{commentId}
-- KEYS[2] = like:outbox:pending
-- KEYS[3] = like:outbox:event:{eventId}
-- ARGV[1] = userId
-- ARGV[2] = aggregateId
-- ARGV[3] = createTime
-- ARGV[4] = ttlSeconds
-- ARGV[5] = eventId
-- ARGV[6] = eventType
-- ARGV[7] = aggregateType
-- ARGV[8] = nowEpochMillis

-- 用户是否已点赞
local liked = redis.call('SISMEMBER', KEYS[1], ARGV[1])
local isLikeAction

-- 切换点赞状态 true:点赞, false:取消点赞
if liked == 1 then
    -- 已点赞：取消点赞。
    redis.call('SREM', KEYS[1], ARGV[1])
    isLikeAction = false
else
    -- 未点赞：点赞
    redis.call('SADD', KEYS[1], ARGV[1])
    isLikeAction = true
end
-- 为点赞缓存更新过期时间
local ttl = tonumber(ARGV[4])
if ttl and ttl > 0 then
    redis.call('EXPIRE', KEYS[1], ttl)
end

-- 写入点赞事件详情
redis.call('HSET', KEYS[3],
    'eventId', ARGV[5],
    'eventType', ARGV[6],
    'aggregateType', ARGV[7],
    'aggregateId', ARGV[2],
    'userId', ARGV[1],
    'liked', tostring(isLikeAction),
    'createTime', ARGV[3],
    'status', '0',
    'retryCount', '0',
    'nextRetryTime', ARGV[8]
)

redis.call('ZADD', KEYS[2], ARGV[8], ARGV[5])

local count = redis.call('SCARD', KEYS[1])
return count;
