-- KEYS[1] = like:outbox:pending
-- KEYS[2] = like:outbox:event:{eventId}
-- ARGV[1] = eventId
-- ARGV[2] = expectedLeaseExpireEpochMillis
-- ARGV[3] = publishedEpochMillis
-- ARGV[4] = publishedRetentionSeconds

if redis.call('HGET', KEYS[2], 'status') ~= '1'
    or redis.call('HGET', KEYS[2], 'nextRetryTime') ~= ARGV[2] then
    return 0
end

redis.call('ZREM', KEYS[1], ARGV[1])
redis.call('HSET', KEYS[2],
    'status', '2',
    'publishedTime', ARGV[3]
)
redis.call('EXPIRE', KEYS[2], ARGV[4])
return 1
