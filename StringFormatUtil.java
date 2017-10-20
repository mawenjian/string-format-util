package net.mawenjian.utils.text;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author mawenjian
 *
 */
public class StringFormatUtil {

	private static final Pattern pattern = Pattern.compile("\\{(.*?)\\}");

	/**
	 * 格式化字符串 字符串中使用{key}表示占位符
	 * @param format 格式化字符串
	 * @param param 对象或Map&lt;String, Object&gt;类型的集合
	 * @return
	 */
	public static String format(String format, Object param) {
		return format(format, param, PostHandleType.NULL_AS_EMPTY.getPostHandler());
	}
	
	/**
	 * 格式化字符串 字符串中使用{key}表示占位符
	 * @param format 格式化字符串
	 * @param param 对象或Map&lt;String, Object&gt;类型的集合
	 * @param postHandleType 数据后处理类型枚举，为NULL则将占位符变量中的所有NULL值替换为空字符串
	 * @return
	 */
	public static String format(String format, Object param, PostHandleType postHandleType) {
		if(postHandleType == null) {
			postHandleType = PostHandleType.NULL_AS_EMPTY;
		}
		
		return format(format, param, postHandleType.getPostHandler());
	}

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
	public static String format(String format, Object param, PostHandleIface postHandler) {
		String targetStr = format;

		// 参数校验
		if (param == null || isStringEmpty(format)) {
			return targetStr;
		}
		if(postHandler == null) {
			postHandler = PostHandleType.NULL_AS_EMPTY.getPostHandler();
		}

		// 存储结果
		Map<String, Object> varNameToValueMap = new HashMap<String, Object>();
		// 提取所有的占位符
		Set<String> varReplacementsSet = extractVarReplacements(format);
		// 抽取所有占位符中的变量，并建立变量到相应占位符之间的关联关系
		Map<String, String> varNameToReplacementMap = extractVariable(varReplacementsSet);
		// 提取所有变量名，并有序存储
		TreeSet<String> varNameSet = new TreeSet<String>();
		varNameSet.addAll(varNameToReplacementMap.keySet());
		// 根据变量名称，从目标对象抽取数据，并放到用于存储结果的Map中
		extractValues(param, "", varNameSet, postHandler, varNameToValueMap);

		// 将占位符替换为实际的值
		for (String varName : varNameSet) {
			String varReplacement = varNameToReplacementMap.get(varName);
			Object value = varNameToValueMap.get(varName);
			if (value != null)
				targetStr = targetStr.replace(varReplacement, value.toString());
		}

		return targetStr;
	}

	/**
	 * 从字符串中抽取出变量占位符
	 * 
	 * @param targetStr
	 * @return
	 */
	private static Set<String> extractVarReplacements(String targetStr) {
		if (targetStr == null) {
			return Collections.emptySet();
		}

		Set<String> varReplacementSet = new HashSet<String>();
		Matcher matcher = pattern.matcher(targetStr);
		while (matcher.find()) {
			String varReplacement = matcher.group();
			if (!varReplacementSet.contains(varReplacement)) {
				varReplacementSet.add(varReplacement);
			}
		}
		return varReplacementSet;
	}

	/**
	 * 对Set中的每个元素应用：从变量占位符（如“{variable}”）中抽取出变量（如“variable”）
	 * 
	 * @param targetVarReplacementSet
	 *            由变量占位符组成的集合
	 * @return
	 */
	private static Map<String, String> extractVariable(Set<String> targetVarReplacementSet) {
		Map<String, String> varNameToVarReplacementMap = new HashMap<String, String>();

		if (!(targetVarReplacementSet == null || targetVarReplacementSet.isEmpty())) {
			for (String varReplacementString : targetVarReplacementSet) {
				if (isStringEmpty(varReplacementString)) {
					continue;
				}

				String varName = extractVariable(varReplacementString);
				if (!(isStringEmpty(varName) || varNameToVarReplacementMap.containsKey(varName))) {
					varNameToVarReplacementMap.put(varName, varReplacementString);
				}
			}
		}

		return varNameToVarReplacementMap;
	}

	/**
	 * 从变量占位符（如“{variable}”）中抽取出变量（如“variable”）
	 * 
	 * @param varRepalcement
	 *            变量占位符,如“{variable}”
	 * @return
	 * @throws IllegalArgumentException
	 */
	private static String extractVariable(String varRepalcement) throws IllegalArgumentException {
		String varName = null;

		if (varRepalcement == null) {
			// variableString = null;
		} else if (varRepalcement.length() <= 2) {
			varName = "";
		} else {
			varName = varRepalcement.substring(1, varRepalcement.length() - 1).trim();
			validateVariableName(varName);
		}

		return varName;
	}

	/**
	 * 判断一个（占位符中的变量）是否合法
	 * 
	 * @param varName
	 * @throws IllegalArgumentException
	 */
	private static void validateVariableName(String varName) throws IllegalArgumentException {
		if (isStringEmpty(varName) || varName.startsWith(".") || varName.endsWith(".")
				|| varName.matches("[.][ ]*[.]")) {
			throw new IllegalArgumentException(
					"some replacement variable is invalid, maybe \"{}\" or  \"{.variable}\" or \"{variable.}\" is appeared in the target string.");
		}
	}

	/**
	 * 从对象中取值
	 * 
	 * @param targerObject
	 *            目标对象
	 * @param variablePrefix
	 *            变量前缀，比如：对于"aaa.bbb.ccc"中的"ccc"来说，它的变量前缀就是"aaa.bbb"（不带"."）
	 * @param noneExtractKeySet
	 *            还没有没取值的键名集合
	 * @param postHandler
	 *            数据读取后的处理接口，可自定义
	 * @param returnData
	 *            待返回的数据
	 */
	private static void extractValues(final Object targerObject, final String variablePrefix,
			final TreeSet<String> noneExtractKeySet, PostHandleIface postHandler, Map<String, Object> returnData) {
		if (targerObject == null || noneExtractKeySet == null || noneExtractKeySet.isEmpty()) {
			return;
		}

		@SuppressWarnings("unchecked")
		TreeSet<String> pendingExtractKeySet = (TreeSet<String>) noneExtractKeySet.clone();

		Map<String, TreeSet<String>> locatedVarToSuccessorVarSetMap = new HashMap<String, TreeSet<String>>();

		// 第一个键的键名，如"aaa.bbb.cc"或"ddd"注意：Set<String>中值的合法性由其他函数保证
		String firstKey = pendingExtractKeySet.pollFirst();

		boolean isPreKeyFinalVar = isFinalVariable(firstKey);
		// 键值所在变量，如如"aaa.bbb.cc"中的"aaa"
		String preKeyLocatedVar = null;
		// 键值所在变量，如如"aaa.bbb.cc"中的"bbb.cc"
		String preKeySuccessorVar = null;
		if (isPreKeyFinalVar) {
			String preFullVariableName = getFullVariableName(variablePrefix, firstKey);
			Object rawObject = extractValueByPropName(targerObject, firstKey);
			String postHandledData = postHandler.handle(targerObject, firstKey, preFullVariableName, rawObject);
			returnData.put(preFullVariableName, postHandledData);
		} else {
			int seperatorIndex = firstKey.indexOf('.');
			preKeyLocatedVar = getLocatedVariableFromFull(firstKey, seperatorIndex);
			preKeySuccessorVar = getSuccessorVariableFromFull(firstKey, seperatorIndex);

			TreeSet<String> preValueSet = new TreeSet<String>();
			preValueSet.add(preKeySuccessorVar);
			locatedVarToSuccessorVarSetMap.put(preKeyLocatedVar, preValueSet);
		}

		for (String thisKey : pendingExtractKeySet) {
			boolean isThisKeyFinalVar = isFinalVariable(thisKey);
			// 键值所在变量
			String thisKeyLocatedVar = null;
			String thisKeySuccessorVar = null;
			if (isThisKeyFinalVar) {
				String fullVariableName = getFullVariableName(variablePrefix, thisKey);
				Object rawObject = extractValueByPropName(targerObject, thisKey);
				String postHandledData = postHandler.handle(targerObject, thisKey, fullVariableName, rawObject);
				returnData.put(fullVariableName, postHandledData);
			} else {
				int seperatorIndex = thisKey.indexOf('.');
				thisKeyLocatedVar = getLocatedVariableFromFull(thisKey, seperatorIndex);
				thisKeySuccessorVar = getSuccessorVariableFromFull(thisKey, seperatorIndex);

				TreeSet<String> successorVarSet = null;
				if (thisKeyLocatedVar.equals(preKeyLocatedVar)) {
					successorVarSet = locatedVarToSuccessorVarSetMap.get(thisKeyLocatedVar);
				} else {
					successorVarSet = new TreeSet<String>();
					locatedVarToSuccessorVarSetMap.put(thisKeyLocatedVar, successorVarSet);
				}
				successorVarSet.add(thisKeySuccessorVar);
			}

			isPreKeyFinalVar = isThisKeyFinalVar;
			preKeyLocatedVar = thisKeyLocatedVar;
			preKeySuccessorVar = thisKeySuccessorVar;
		}

		for (Entry<String, TreeSet<String>> entry : locatedVarToSuccessorVarSetMap.entrySet()) {
			String propName = entry.getKey();
			TreeSet<String> successorVarSet = entry.getValue();
			Object thisObject = extractValueByPropName(targerObject, propName);
			// 递归调用，千万注意
			if (thisObject != null) {
				String fullVariableName = getFullVariableName(variablePrefix, propName);
				extractValues(thisObject, fullVariableName, successorVarSet, postHandler, returnData);
			}
		}
	}

	/**
	 * 是否为最终变量（即变量中是否有“.”）
	 * 
	 * @param variableString
	 */
	private static boolean isFinalVariable(String variableString) {
		if (isStringEmpty(variableString)) {
			return true;
		}
		return !(variableString.contains("."));
	}

	/**
	 * 获得变量的全命名
	 * 
	 * @param variablePrefix
	 *            变量的前缀（即包含这个变量的父变量）
	 * @param relativeVariableName
	 * @return
	 */
	private static String getFullVariableName(String variablePrefix, String relativeVariableName) {
		String fullVariableName = null;
		if (isStringEmpty(variablePrefix)) {
			fullVariableName = relativeVariableName;
		} else {
			fullVariableName = String.format("%s.%s", variablePrefix, relativeVariableName);
		}
		return fullVariableName;
	}

	/**
	 * 获取"aaa.bbb.ccc"中的"aaa"
	 * 
	 * @param fullVariable
	 *            "aaa.bbb.ccc"
	 * @param seperatorIndex
	 *            分隔符位置
	 * @return
	 */
	private static String getLocatedVariableFromFull(String fullVariable, int seperatorIndex) {
		return fullVariable.substring(0, seperatorIndex);
	}

	/**
	 * 获取"aaa.bbb.ccc"中的"bbb.ccc"
	 * 
	 * @param fullVariable
	 *            "aaa.bbb.ccc"
	 * @param seperatorIndex
	 *            分隔符位置
	 * @return
	 */
	private static String getSuccessorVariableFromFull(String fullVariable, int seperatorIndex) {
		return fullVariable.substring(seperatorIndex + 1);
	}

	/**
	 * 从对象或Map<String, Object>类型的Map中取值
	 * 
	 * @param obj
	 * @param propName
	 *            属性名称，或Map中的键名
	 * @return
	 */
	private static Object extractValueByPropName(Object obj, String propName) {
		Object valObject = null;
		if (obj == null) {
			// valObject = null;
		} else if (obj instanceof Map<?, ?>) { // 从Map取值
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) obj;
			if (map.containsKey(propName)) {
				valObject = map.get(propName);
			}
		} else { // 从对象取值
			PropertyDescriptor pd;
			Method getMethod;
			try {
				pd = new PropertyDescriptor(propName, obj.getClass());
				getMethod = pd.getReadMethod();// 获得get方法
				valObject = getMethod.invoke(obj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}

		return valObject;
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isStringEmpty(String str) {
		return (str == null) || ("".equals(str));
	}

	/* ==========以下是与该工具类相配套的一些枚举和接口========== */

	/**
	 * 取值后的处理方式
	 * 
	 * @author mawenjian
	 *
	 */
	public enum PostHandleType {
		/**
		 * 什么都不做，不做任何替换，保留原值
		 */
		NULL_IGNOREANCE(new NullAsNullPostHandler()),

		/**
		 * 将所有值为NULL的占位符替换为空字符串
		 */
		NULL_AS_EMPTY(new NullAsEmptyPostHandler()),
		
		/**
		 * 将所有值为NUL的占位符替换为字符串“NULL”
		 */
		NULL_AS_STRING(new NullAsStringPostHandler());

		private PostHandleIface postHandler;

		PostHandleType(PostHandleIface postHandler) {
			this.postHandler = postHandler;
		}

		public PostHandleIface getPostHandler() {
			return postHandler;
		}

	}

	public interface PostHandleIface {
		/**
		 * 从目标对象取出值后，进行后处理操作
		 * 
		 * @param containerObject
		 *            取值的容器，即包含该值的对象
		 * @param name
		 *            该值对应的属性名称，如“ccc”
		 * @param fullVariableName
		 *            该值在占位符中的变量名称，如“aaa.bbb.ccc”
		 * @param value
		 *            实际取值
		 * @return
		 */
		String handle(Object containerObject, String name, String fullVariableName, Object value);
	}

	/**
	 * 什么都不做，原封返回
	 * 
	 * @author mawenjian
	 *
	 */
	public static class NullAsNullPostHandler implements PostHandleIface {

		@Override
		public String handle(Object containerObject, String name, String fullVariableName, Object value) {
			return (value != null) ? value.toString() : null;
		}

	}

	/**
	 * 将NULL值转换为空字符串
	 * 
	 * @author mawenjian
	 *
	 */
	public static class NullAsEmptyPostHandler implements PostHandleIface {

		@Override
		public String handle(Object containerObject, String name, String fullVariableName, Object value) {
			return (value != null) ? value.toString() : "";
		}

	}
	
	/**
	 * 将NULL值转换为字符串“NULL”
	 * 
	 * @author mawenjian
	 *
	 */
	public static class NullAsStringPostHandler implements PostHandleIface {
		
		@Override
		public String handle(Object containerObject, String name, String fullVariableName, Object value) {
			return (value != null) ? value.toString() : "NULL";
		}
		
	}
}
