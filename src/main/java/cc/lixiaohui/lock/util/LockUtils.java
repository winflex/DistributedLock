package cc.lixiaohui.lock.util;

import redis.clients.jedis.Jedis;

public class LockUtils {
	
	public static final LockInfo getLockInfo(Jedis jedis, String key) {
		String lockMsg = jedis.get(key);
		if (lockMsg == null) {
			return null;
		}
		return LockInfo.fromString(lockMsg);
	}
	
	public static final void setLockInfo(Jedis jedis, String key, LockInfo lockInfo) {
		String lockMsg = LockInfo.toString(lockInfo);
		jedis.set(key, lockMsg);
	}
	
}
