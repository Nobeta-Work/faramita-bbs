-- KEYS[1] = like:outbox:pending
-- KEYS[2] = like:outbox:event:{eventId}
-- KEYS[3] = like:outbox:failed
-- ARGV[1] = eventId
-- ARGV[2] = expectedLeaseExpireEpochMillis
-- ARGV[3] = nowEpochMillis
-- ARGV[4] = maxRetry
-- ARGV[5] = maxRetryDelayMillis

if redis.call('HGET', KEYS[2], 'status') ~= '1'
    or redis.call('HGET', KEYS[2], 'nextRetryTime') ~= ARGV[2] then
    return -1
end

local retryCount = redis.call('HINCRBY', KEYS[2], 'retryCount', 1)
if retryCount >= tonumber(ARGV[4]) then
    redis.call('HSET', KEYS[2],
        'status', '3',
        'failedTime', ARGV[3]
    )
    redis.call('ZREM', KEYS[1], ARGV[1])
    redis.call('ZADD', KEYS[3], ARGV[3], ARGV[1])
    return retryCount
end

local delay = (2 ^ (retryCount - 1)) * 1000
if delay > tonumber(ARGV[5]) then
    delay = tonumber(ARGV[5])
end
local nextRetryTime = tonumber(ARGV[3]) + delay

redis.call('HSET', KEYS[2],
    'status', '0',
    'nextRetryTime', tostring(nextRetryTime)
)
redis.call('ZADD', KEYS[1], nextRetryTime, ARGV[1])
return retryCount
