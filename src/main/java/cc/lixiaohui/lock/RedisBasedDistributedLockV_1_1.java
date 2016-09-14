package cc.lixiaohui.lock;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import cc.lixiaohui.lock.time.nio.client.TimeClient;

/**
 * <pre>
 * 基于Redis的SETNX操作实现的分布式锁
 * 
 * 获取锁时最好用lock(long time, TimeUnit unit), 以免网路问题而导致线程一直阻塞
 * 
 * <a href="http://redis.io/commands/setnx">SETNC操作参考资料</a>
 * </pre>
 * 
 * @author lixiaohui
 * @version V_1_1 增加时间协调服务, 仍有问题: non-reentrant
 *
 */
public class RedisBasedDistributedLockV_1_1 extends AbstractLock {
	
	private Jedis jedis;
	
	private TimeClient timeClient;
	
	// 锁的名字
	protected String lockKey;
	
	// 锁的有效时长(毫秒)
	protected long lockExpires;
	
	public RedisBasedDistributedLockV_1_1(Jedis jedis, String lockKey, long lockExpires, SocketAddress timeServerAddr) throws IOException {
		this.jedis = jedis;
		this.lockKey = lockKey;
		this.lockExpires = lockExpires;
		timeClient = new TimeClient(timeServerAddr);
	}
	
	// 阻塞式获取锁的实现
	protected boolean lock(boolean useTimeout, long time, TimeUnit unit, boolean interrupt) throws InterruptedException{
		if (interrupt) {
			checkInterruption();
		}
		
		// 超时控制 的时间可以从本地获取, 因为这个和锁超时没有关系, 只是一段时间区间的控制
		long start = localTimeMillis();
		long timeout = unit.toMillis(time); // if !useTimeout, then it's useless
		
		while (useTimeout ? isTimeout(start, timeout) : true) {
			if (interrupt) {
				checkInterruption();
			}
			
			long lockExpireTime = serverTimeMillis() + lockExpires + 1;//锁超时时间
			String stringOfLockExpireTime = String.valueOf(lockExpireTime);
			
			if (jedis.setnx(lockKey, stringOfLockExpireTime) == 1) { // 获取到锁
				// TODO 成功获取到锁, 设置相关标识
				locked = true;
				//setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			
			String value = jedis.get(lockKey);
			if (value != null && isTimeExpired(value)) { // lock is expired
				// 假设多个线程(非单jvm)同时走到这里
				String oldValue = jedis.getSet(lockKey, stringOfLockExpireTime); // getset is atomic
				// 但是走到这里时每个线程拿到的oldValue肯定不可能一样(因为getset是原子性的)
				// 加入拿到的oldValue依然是expired的，那么就说明拿到锁了
				if (oldValue != null && isTimeExpired(oldValue)) {
					// TODO 成功获取到锁, 设置相关标识
					locked = true;
					//setExclusiveOwnerThread(Thread.currentThread());
					return true;
				}
			} else { 
				// TODO lock is not expired, enter next loop retrying
			}
		}
		return false;
	}
	
	public boolean tryLock() {
		long lockExpireTime = serverTimeMillis() + lockExpires + 1;//锁超时时间
		String stringOfLockExpireTime = String.valueOf(lockExpireTime);
		
		if (jedis.setnx(lockKey, stringOfLockExpireTime) == 1) { // 获取到锁
			// TODO 成功获取到锁, 设置相关标识
			locked = true;
			//setExclusiveOwnerThread(Thread.currentThread());
			return true;
		}
		
		String value = jedis.get(lockKey);
		if (value != null && isTimeExpired(value)) { // lock is expired
			// 假设多个线程(非单jvm)同时走到这里
			String oldValue = jedis.getSet(lockKey, stringOfLockExpireTime); // getset is atomic
			// 但是走到这里时每个线程拿到的oldValue肯定不可能一样(因为getset是原子性的)
			// 假如拿到的oldValue依然是expired的，那么就说明拿到锁了
			if (oldValue != null && isTimeExpired(oldValue)) {
				// TODO 成功获取到锁, 设置相关标识
				locked = true;
				//setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
		} else { 
			// TODO lock is not expired, enter next loop retrying
		}
		
		return false;
	}
	
	/**
	 * Queries if this lock is held by any thread.
	 * 
	 * @return {@code true} if any thread holds this lock and
     *         {@code false} otherwise
	 */
	public boolean isLocked() {
		if (locked) {
			return true;
		} else {
			String value = jedis.get(lockKey);
			// TODO 这里其实是有问题的, 想:当get方法返回value后, 假设这个value已经是过期的了,
			// 而就在这瞬间, 另一个节点set了value, 这时锁是被别的线程(节点持有), 而接下来的判断
			// 是检测不出这种情况的.不过这个问题应该不会导致其它的问题出现, 因为这个方法的目的本来就
			// 不是同步控制, 它只是一种锁状态的报告.
			return !isTimeExpired(value);
		}
	}

	@Override
	protected void unlock0() {
		// TODO 判断锁是否过期
		String value = jedis.get(lockKey);
		if (!isTimeExpired(value)) {
			doUnlock();
		}
	}
	
	public void release() {
		jedis.close();
		timeClient.close();
	}
	
	private void doUnlock() {
		jedis.del(lockKey);
	}

	private void checkInterruption() throws InterruptedException {
		if(Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
	}
	
	private boolean isTimeExpired(String value) {
		// 这里拿服务器的时间来比较
		return Long.parseLong(value) < serverTimeMillis();
	}
	
	private boolean isTimeout(long start, long timeout) {
		// 这里拿本地的时间来比较
		return start + timeout > System.currentTimeMillis();
	}
	
	private long serverTimeMillis(){
		return timeClient.currentTimeMillis();
	}
	
	private long localTimeMillis() {
		return System.currentTimeMillis();
	}
	
}
