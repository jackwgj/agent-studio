local sessionId = KEYS[1]

return redis.call('GET', sessionId)