# StringFormatUtil

----------------------------------------
`StringFormatUtil`是一个基于Java开发的功能强大的多层级字符串/占位符替换工具.

`StringFormatUtil` is a powerful Java-based multi-level string / placeholder replacement tool.

## 简介

----------------------------------------
### 其它类似替换工具的不足

尽管网络上很早就有类似的字符串/占位符替换工具，但是它们只能实现类似`{replacement}`形式的单层字符串/占位符替换，在使用过程中具有很大的局限性，不能满足我们的业务需要。这也是我造这个轮子的直接原因。

### StringFormatUtil的优势

* 便捷性更高。不同于网络上的这些工具类，`StringFormatUtil`基于Java的反射技术，实现了字符串/占位符的多层级替换，可以实现诸如`{china.beijing.haidian}`形式的替换，使用上更加方便。

* 可定制性更强。如果我们提供的几种默认方式不能满足开发需要，开发者可以通过实现类中的`PostHandleIface`接口，实现对输出数据格式的个性化定制（例如：日期的格式化显示，小数精确位数的控制，等等）。

* 算法进行了优化。在算法实现上，我们也进行了很多的优化，从而可以保证较好的执行效率（当然，与单层替换相比，效率难免还是要低一些的，所以如果程序对时间非常敏感、而且需求简单的话，也可以考虑单层替换方案；不过一般需求根本不用考虑这么多，我们的程序在效率上完全没问题，尽管放心用）。

## Introduction

----------------------------------------
### Shortcomings of other similar tools

Although there are similar string / placeholder replacement tools on the Internet, they can only be implemented with a single-level string/placeholder replacement like `{replacement}`, with great limitations in actual use , can not meet our business needs. It is also the direct cause of my development of this tool. 

### Advantage of StringFormatUtil

* More convenient. Different from the tools on the Internet, `StringFormatUtil` based on Java's reflection, achieves a string/placeholder multi-level replacement. So you can achieve such as `{china.beijing.haidian}` form of replacement, with more Convenience in actual use.

* More customizable. If the several methods we provide can not meet your development needs, your can achieve your customization of output data formatting(eg: the date of formatted display, decimal precision bit control, etc.) by an implementation of `PostHandleIface` interface.

* Optimized algorithm.In the implementation of the algorithm, we also carried out a lot of optimization, which can ensure better execution efficiency (Of course, compared with single-level replacement, the efficiency is still inevitable. So if your program is extrame sensitive to time and has simple needs only , You can also consider a single-level replacement program. However, the general do not need to consider so much, since our program is completely okey in efficiency, despite the rest assured).

----------------------------------------

## 公共函数定义

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

----------------------------------------
## 使用演示

----------------------------------------

### 1. 数据准备

<pre><code>
// Map&lt;String, Object&gt;类型使用的格式化字符串
private final static String formatForMap = "{name}, {china.name}, {usa.name}, {china.beijing.name}, {china.beijing.xicheng.name}, {world.region1}, {world.child.region1}.";
// 类对象使用的格式化字符串
private final static String formatForBean = "{region1}, {region2}, {child.region1}, {child.region2}, {child.child.region1}, {child.child.region2}, {date}, {child.date}.";

private Map&lt;String, Object&gt; paramMap = null;
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
    paramMap = new HashMap&lt;String, Object&gt;();
    paramMap.put("name", "世界");
    
    Map&lt;String, Object&gt; subParam = new HashMap&lt;String, Object&gt;();
    subParam.put("name", "中国");
    paramMap.put("china", subParam);
    
    Map&lt;String, Object&gt; subParam2 = new HashMap&lt;String, Object&gt;();
    subParam2.put("name", "美国");
    paramMap.put("usa", subParam2);
    
    Map&lt;String, Object&gt; subSubParam = new HashMap&lt;String, Object&gt;();
    subSubParam.put("name", "北京");
    subParam.put("beijing", subSubParam);
    
    Map&lt;String, Object&gt; subSubSubParam = new HashMap&lt;String, Object&gt;();
    subSubSubParam.put("name", "西城");
    subSubParam.put("xicheng", subSubSubParam);
    
    
    // <font color="red"><b>注意：Map&lt;String, Object&gt;与Bean可以混用</b></font>
    paramMap.put("world", subBean);
}
</code></pre>

### 2. 使用`Map<String, Object>`作为传入参数

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

### 3. 使用`Bean`作为传入参数

<pre><code>
@Test
public void testFormatString2() {

    /* 
     * formatForBean和paramBean的定义详见“数据准备部分”
     *
     * String formatForBean;
     * StringFormatTestBean paramBean;
     *
     */
     
    String result = StringFormatUtil.format(formatForBean, paramBean);
    System.out.println(result);
}
</code></pre>

### 4. 几种可选的NULL值处理方式

<pre><code>
@Test
public void testFormatString3() {

    /* 
    * formatForBean和paramBean的定义详见“数据准备部分”
    *
    * String formatForBean;
    * StringFormatTestBean paramBean;
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

### 5. 高级用法：自定义数据处理接口，按你需要的格式输出

<pre><code>
@Test
public void testFormatString4() {

    /* 
     * formatForBean和paramBean的定义详见“数据准备部分”
     *
     * String formatForBean;
     * StringFormatTestBean paramBean;
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

### 6. 例子中用到的StringFormatTestBean类

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
