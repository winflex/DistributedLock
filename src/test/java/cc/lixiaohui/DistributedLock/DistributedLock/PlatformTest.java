package cc.lixiaohui.DistributedLock.DistributedLock;

import org.junit.Test;

import cc.lixiaohui.lock.util.PlatformUtils;

public class PlatformTest {
	
	@Test
	public void test() {
		System.out.println(PlatformUtils.MACAddress());
	}
	
}
