package net.mawenjian.utils.text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.mawenjian.masf.utils.text.StringFormatUtil;
import net.mawenjian.masf.utils.text.StringFormatUtil.PostHandleIface;
import net.mawenjian.masf.utils.text.StringFormatUtil.PostHandleType;

public class StringFormatUtilTest {

	// Map类型使用的格式化字符串
	private final static String formatForMap = "{name}, {china.name}, {usa.name}, {china.beijing.name}, {china.beijing.xicheng.name}, {world.region1}, {world.child.region1}.";
	// 类对象使用的格式化字符串
	private final static String formatForBean = "{region1}, {region2}, {child.region1}, {child.region2}, {child.child.region1}, {child.child.region2}, {date}, {child.date}.";

	private Map<String, Object> paramMap = null;
	private StringFormatTestBean paramBean = null;

	@Before
	public void testPrepare() {
		// paramBean初始化
		paramBean = new StringFormatTestBean();
		paramBean.setRegion1("世界");
		paramBean.setRegion2(null);
		StringFormatTestBean subBean = new StringFormatTestBean();
		subBean.setRegion1("中国");
		subBean.setRegion2("美国");
		paramBean.setChild(subBean);
		StringFormatTestBean subSubBean = new StringFormatTestBean();
		subSubBean.setRegion1("北京");
		subSubBean.setRegion2("河北");
		subBean.setChild(subSubBean);

		// paramMap初始化
		paramMap = new HashMap<String, Object>();
		paramMap.put("name", "世界");
		Map<String, Object> subParam = new HashMap<String, Object>();
		subParam.put("name", "中国");
		paramMap.put("china", subParam);
		Map<String, Object> subParam2 = new HashMap<String, Object>();
		subParam2.put("name", "美国");
		paramMap.put("usa", subParam2);
		Map<String, Object> subSubParam = new HashMap<String, Object>();
		subSubParam.put("name", "北京");
		subParam.put("beijing", subSubParam);
		Map<String, Object> subSubSubParam = new HashMap<String, Object>();
		subSubSubParam.put("name", "西城");
		subSubParam.put("xicheng", subSubSubParam);
		
		//注意：Map与Bean可以混用
		paramMap.put("world", subBean);
	}

	/**
	 * 针对Map类型的简单用法
	 */
	@Test
	public void testFormatString1() {
		String result = StringFormatUtil.format(formatForMap, paramMap);
		System.out.print("TEST-101 => ");
		System.out.println(result);
	}

	/**
	 * 针对对象类型的简单用法
	 */
	@Test
	public void testFormatString2() {
		String result = StringFormatUtil.format(formatForBean, paramBean);
		System.out.print("TEST-201 => ");
		System.out.println(result);
	}

	/**
	 * 采用工具类自带的几种数据后处理方式
	 */
	@Test
	public void testFormatString3() {
		// NULL值不做替换
		String result = StringFormatUtil.format(formatForBean, paramBean, PostHandleType.NULL_IGNOREANCE);
		System.out.print("TEST-301 => ");
		System.out.println(result);

		// NULL值替换为空字符串
		result = StringFormatUtil.format(formatForBean, paramBean, PostHandleType.NULL_AS_EMPTY);
		System.out.print("TEST-302 => ");
		System.out.println(result);

		// NULL值替换为字符串“NULL”
		result = StringFormatUtil.format(formatForBean, paramBean, PostHandleType.NULL_AS_STRING);
		System.out.print("TEST-303 => ");
		System.out.println(result);
	}

	/**
	 * 高级用法 => 自己实现数据后处理接口
	 */
	@Test
	public void testFormatString4() {

		// 放在匿名类外是为了演示需要向匿名类传值的使用场景
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		// 自定义处理类
		StringFormatUtil.PostHandleIface postHandler = new PostHandleIface() {

			// private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			@Override
			public String handle(Object containerObject, String name, String fullVariableName, Object value) {
				/**
				 * 注意： 1）各个逻辑的处理顺序，否则很容易出错； 2）value==null时调用toString()方法会抛空指针错误
				 */
				String returnString = null;
				if ("region2".equals(name)) {
					returnString = String.format("region2: %s", value);
				} else if ("date".equals(name) && "date".equals(fullVariableName) && value != null
						&& value instanceof Date) {
					returnString = sdf.format(value);
				} else if (value == null) {
					returnString = null;
				} else {
					returnString = value.toString();
				}
				return returnString;
			}

		};
		String result = StringFormatUtil.format(formatForBean, paramBean, postHandler);
		System.out.print("TEST-401 => ");
		System.out.println(result);
	}

	public class StringFormatTestBean {
		private String region1;
		private String region2;
		private Date date = new Date();
		private StringFormatTestBean child;

		public String getRegion1() {
			return region1;
		}

		public void setRegion1(String region1) {
			this.region1 = region1;
		}

		public String getRegion2() {
			return region2;
		}

		public void setRegion2(String region2) {
			this.region2 = region2;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public StringFormatTestBean getChild() {
			return child;
		}

		public void setChild(StringFormatTestBean child) {
			this.child = child;
		}
	}
}
