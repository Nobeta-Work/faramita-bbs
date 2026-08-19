-- 令牌桶限流脚本
-- KEYS[1] = rate_limit:{接口}:{用户/IP}
-- ARGV[1] = capacity       容量
-- ARGV[2] = refill         每秒补充令牌数
-- ARGV[3] = now            当前时间戳(s)
-- ARGV[4] = ttl

local capacity = tonumber(ARGV[1])
local refill = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- 读取当前令牌与上次更新记录

local data = redis.call('HMGET', KEYS[1], 'tokens', 'last_time')
local current_tokens = tonumber(data[1]) or capacity
local last_time = tonumber(data[2]) or now

-- 计算这段时间补充的令牌
local add_token = (now - last_time) * refill
current_tokens = math.min(capacity, current_tokens + add_token)

-- 扣令牌放行或限流
if current_tokens >= 1 then
    redis.call('HMSET', KEYS[1], 'tokens', current_tokens - 1, 'last_time', now)
    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[4]))
    return 1
else
    return 0
end