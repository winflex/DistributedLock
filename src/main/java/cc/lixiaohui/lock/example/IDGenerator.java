package cc.lixiaohui.lock.example;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import cc.lixiaohui.lock.Lock;
import cc.lixiaohui.lock.Releasable;

/**
 * 模拟分布式环境中的ID生成 
 * @author lixiaohui
 *
 */
public class IDGenerator implements Releasable {

	private static BigInteger id = BigInteger.valueOf(0);

	private final Lock lock;

	private static final BigInteger INCREMENT = BigInteger.valueOf(1);

	public IDGenerator(Lock lock) {
		this.lock = lock;
	}
	
	public String getAndIncrement() {
		if (lock.tryLock(3, TimeUnit.SECONDS)) {
			try {
				// TODO 这里获取到锁, 访问临界区资源
				/*try {
					lock.tryLock(3, TimeUnit.SECONDS);
				} finally {
					lock.unlock();
				}*/
				
				return getAndIncrement0();
			} finally {
				lock.unlock();
			}
		}
		return null;
		//return getAndIncrement0();
	}
	
	public void release() {
		lock.release();
	}

	private String getAndIncrement0() {
		String s = id.toString();
		id = id.add(INCREMENT);
		return s;
	}
}