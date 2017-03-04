package cc.lixiaohui.DistributedLock.DistributedLock;

import io.lock.util.PlatformUtils;

import org.junit.Test;

public class PlatformTest {
	
	@Test
	public void test() {
		System.out.println(PlatformUtils.MACAddress());
	}
	
}
