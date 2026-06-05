local sessionId = KEYS[1]
local message = KEYS[2]
local expireTime = KEYS[3]

redis.call('SET', sessionId, message)
redis.call('EXPIRE', sessionId, expireTime)