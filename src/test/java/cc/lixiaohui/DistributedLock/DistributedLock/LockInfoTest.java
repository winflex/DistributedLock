package cc.lixiaohui.DistributedLock.DistributedLock;

import org.junit.Test;

import cc.lixiaohui.lock.util.LockInfo;

public class LockInfoTest {
	
	@Test
	public void test() {
		LockInfo li = new LockInfo();
		
		li.setCount(1);
		li.setExpires(Long.MAX_VALUE);
		li.setMac("127.0.0.1");
		li.setJvmPid(11);
		li.setThreadId(Thread.currentThread().getId());
		
		System.out.println(li.toString());
	}
	
}
