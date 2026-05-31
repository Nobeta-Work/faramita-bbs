-- KEYS[1] = like:blog:{blogId}
-- KEYS[2] = like:changelog:blog
-- ARGV[1] = userId
-- ARGV[2] = blogId
-- ARGV[3] = timestamp
-- ARGV[4] = ttlSeconds

local liked = redis.call('SISMEMBER', KEYS[1], ARGV[1])
local action
local isLikeAction

if liked == 1 then
    redis.call('SREM', KEYS[1], ARGV[1])
    isLikeAction = false
else 
    redis.call('SADD', KEYS[1], ARGV[1])
    isLikeAction = true
end

local ttl = tonumber(ARGV[4])
if ttl and ttl > 0 then
    redis.call('EXPIRE', KEYS[1], ttl)
end

redis.call('XADD', KEYS[2], '~', 100000, '*',
    'blogId', ARGV[2],
    'userId', ARGV[1],
    'isLikeAction', tostring(isLikeAction),
    'timestamp', ARGV[3]
)

local count = redis.call('SCARD', KEYS[1])
return count;