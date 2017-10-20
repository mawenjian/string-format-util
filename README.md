# StringFormatUtil

----------------------------------------
`StringFormatUtil`是一个功能强大的Java字符串/占位符替换工具.

`StringFormatUtil` is a powerful Java-based string / placeholder replacement tool.

## 程序演示

----------------------------------------

### 1. `StringFormatUtil`函数定义

<pre><code>
public class StringFormatUtil {
    /**
     * 格式化字符串 字符串中使用{key}表示占位符
     * @param format 格式化字符串
     * @param param 对象或Map&lt;String, Object&gt;类型的集合
     * @return
     */
    public static String format(String format, Object param);
    
    /**
     * 格式化字符串 字符串中使用{key}表示占位符
     * @param format 格式化字符串
     * @param param 对象或Map&lt;String, Object&gt;类型的集合
     * @param postHandleType 数据后处理类型枚举，为NULL则将占位符变量中的所有NULL值替换为空字符串
     * @return
     */
    public static String format(String format, Object param, PostHandleType postHandleType);
    
    /**
     * 格式化字符串 字符串中使用{key}表示占位符
     * 
     * @param format
     *            格式化字符串
     * @param param
     *            对象或Map&lt;String, Object&gt;类型的集合
     * @param postHandler
     *            数据后处理接口，为NULL则将占位符变量中的所有NULL值替换为空字符串
     * @return
     */
    public static String format(String format, Object param, PostHandleIface postHandler);
}
</code></pre>

### 2. 传入参数`param`是`Map<String, Object>`类型的简单用法

<pre><code>
@Test
public void testFormatString1() {

    /* 
     * formatForMap和paramMap的定义详见“数据准备”部分
     *
     * String formatForMap;
     * Map&lt;String, Object&gt; paramMap;
     *
     */
     
    String result = StringFormatUtil.format(formatForMap, paramMap);
    System.out.println(result);
}
</code></pre>

### 3. 传入参数`param`是类对象类型的简单用法

<pre><code>
@Test
public void testFormatString2() {

    /* 
     * formatForBean和paramBean的定义详见“数据准备部分”
     *
     * String formatForBean;
     * Map&lt;String, Object&gt; paramBean;
     *
     */
     
    String result = StringFormatUtil.format(formatForBean, paramBean);
    System.out.println(result);
}
</code></pre>

### 4. `StringFormatUtil`自带的几种数据处理方式

<pre><code>
@Test
public void testFormatString3() {

    /* 
    * formatForBean和paramBean的定义详见“数据准备部分”
    *
    * String formatForBean;
    * Map&lt;String, Object&gt; paramBean;
    *
    */
    
    // NULL值不做替换
    String result = StringFormatUtil.format(formatForBean, paramBean, PostHandleType.NULL_IGNOREANCE);
    System.out.println(result);

    // NULL值替换为空字符串
    result = StringFormatUtil.format(formatForBean, paramBean, PostHandleType.NULL_AS_EMPTY);
    System.out.println(result);

    // NULL值替换为字符串“NULL”
    result = StringFormatUtil.format(formatForBean, paramBean, PostHandleType.NULL_AS_STRING);
    System.out.println(result);
}
</code></pre>

### 5. 自定义数据处理接口，按你需要的格式输出

<pre><code>
@Test
public void testFormatString4() {

    /* 
     * formatForBean和paramBean的定义详见“数据准备部分”
     *
     * String formatForBean;
     * Map&lt;String, Object&gt; paramBean;
     *
     */
     
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
    System.out.println(result);
}
</code></pre>

### 6. 数据准备

<pre><code>
// Map&lt;String, Object&gt;类型使用的格式化字符串
private final static String formatForMap = "{name}, {china.name}, {usa.name}, {china.beijing.name}, {china.beijing.xicheng.name}.";;
// 类对象使用的格式化字符串
private final static String formatForBean = "{region1}, {region2}, {child.region1}, {child.region2}, {child.child.region1}, {child.child.region2}, {date}, {child.date}.";

private Map&lt;String, Object&gt; paramMap = null;
private StringFormatTestBean paramBean = null;

@Before
public void testPrepare() {
    // paramMap初始化
    paramMap = new HashMap&lt;String, Object&gt;();
    paramMap.put("name", "世界");
    
    Map&lt;String, Object&gt; subParam = new HashMap<String, Object>();
    subParam.put("name", "中国");
    paramMap.put("china", subParam);
    
    Map&lt;String, Object&gt; subParam2 = new HashMap<String, Object>();
    subParam2.put("name", "美国");
    paramMap.put("usa", subParam2);
    
    Map&lt;String, Object&gt; subSubParam = new HashMap<String, Object>();
    subSubParam.put("name", "北京");
    subParam.put("beijing", subSubParam);
    
    Map&lt;String, Object&gt; subSubSubParam = new HashMap<String, Object>();
    subSubSubParam.put("name", "西城");
    subSubParam.put("xicheng", subSubSubParam);


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
}
</code></pre>

### 7. 例子中用到的StringFormatTestBean类

<pre><code>
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
</code></pre>

## 联系方式

----------------------------------------
* 邮箱：mawenjian#gmail.com

* 博客：[https://mawenjian.net/](https://mawenjian.net "马文建的博客")
