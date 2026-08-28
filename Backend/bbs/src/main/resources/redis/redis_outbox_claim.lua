-- KEYS[1] = like:outbox:pending
-- KEYS[2] = like:outbox:event:{eventId}
-- ARGV[1] = eventId
-- ARGV[2] = nowEpochMillis
-- ARGV[3] = leaseExpireEpochMillis

local score = redis.call('ZSCORE', KEYS[1], ARGV[1])
if not score or tonumber(score) > tonumber(ARGV[2]) then
    return 0
end

if redis.call('EXISTS', KEYS[2]) == 0 then
    redis.call('ZREM', KEYS[1], ARGV[1])
    return 0
end

local status = redis.call('HGET', KEYS[2], 'status')
if status ~= '0' and status ~= '1' then
    return 0
end

redis.call('HSET', KEYS[2],
    'status', '1',
    'nextRetryTime', ARGV[3]
)
redis.call('ZADD', KEYS[1], ARGV[3], ARGV[1])
return 1
