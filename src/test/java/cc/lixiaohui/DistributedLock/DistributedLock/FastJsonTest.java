/**
 * 
 */
package cc.lixiaohui.DistributedLock.DistributedLock;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

/**
 * @author lixiaohui
 * @date 2016年9月14日 上午10:25:46
 * 
 */
public class FastJsonTest {
	
	@Test
	public void test() {
		User user = new User();
		user.setName("lixiaohui");
		String json = JSON.toJSONString(user);
		System.out.println(json);
	}
}
class User {
	private String name;
	
	public static final String DEFAULT_NAME = "name";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static String getDefaultName() {
		return DEFAULT_NAME;
	}

}
