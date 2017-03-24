package cn.mljia.ddd.common.port.adapter.packagescan;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 类路径
 */
public class ClassPath {

	private ArrayList<ResourceInfo> resources;

	private ClassPath(ArrayList<ResourceInfo> resources) {
		this.resources = resources;
	}

	/**
	 * 来自指定的类加载器的类路径
	 * 
	 * @param classLoader
	 * 
	 * @return
	 */
	public static ClassPath from(ClassLoader classLoader) {
		// 创建类扫描器
		Scanner scanner = new Scanner();
		for (Map.Entry<URI, ClassLoader> entry : getClassPathEntries(classLoader).entrySet()) {

			scanner.scan(entry.getKey(), entry.getValue());
		}
		return new ClassPath(scanner.getResources());
	}

	/**
	 * 得到所有类
	 * 
	 * @return
	 */
	public List<ClassInfo> getAllClasses() {
		List<ClassInfo> classInfos = new ArrayList<ClassInfo>();
		for (Iterator<ResourceInfo> iterator = resources.iterator(); iterator.hasNext();) {
			ResourceInfo resourceInfo = iterator.next();
			if (!(resourceInfo instanceof ClassInfo))
				continue;
			ClassInfo classInfo = (ClassInfo) resourceInfo;
			if (classInfo.getName().indexOf("$") != -1)
				continue;
			classInfos.add(classInfo);
		}
		return classInfos;
	}

	/**
	 * 根据包名得到顶层所有类
	 * 
	 * @param packageName
	 * @return
	 */
	public List<ClassInfo> getTopLevelAllClass(String packageName) {
		List<ClassInfo> classInfos = new ArrayList<ClassInfo>();
		for (Iterator<ResourceInfo> iterator = resources.iterator(); iterator.hasNext();) {
			ResourceInfo resourceInfo = iterator.next();
			if (!(resourceInfo instanceof ClassInfo))
				continue;
			ClassInfo classInfo = (ClassInfo) resourceInfo;
			if (classInfo.getName().indexOf("$") != -1)
				continue;
			if (!packageName.equals(classInfo.getPackageName()))
				continue;
			classInfos.add(classInfo);
		}
		return classInfos;
	}

	/**
	 * 根据包名递归得到顶层所有类
	 * 
	 * @param packageName
	 * @return
	 */
	public List<ClassInfo> getTopLevelRecursiveAllClass(String packageName) {
		List<ClassInfo> classInfos = new ArrayList<ClassInfo>();
		for (Iterator<ResourceInfo> iterator = resources.iterator(); iterator.hasNext();) {
			ResourceInfo resourceInfo = iterator.next();
			if (!(resourceInfo instanceof ClassInfo))
				continue;
			ClassInfo classInfo = (ClassInfo) resourceInfo;
			if (classInfo.getName().indexOf("$") != -1)
				continue;
			if (!classInfo.getPackageName().startsWith(packageName))
				continue;
			classInfos.add(classInfo);
		}
		return classInfos;
	}

	/**
	 * 得到类路径记录
	 * 
	 * @param classLoader
	 * 
	 * @return
	 */
	private static LinkedHashMap<URI, ClassLoader> getClassPathEntries(ClassLoader classLoader) {
		LinkedHashMap<URI, ClassLoader> entries = new LinkedHashMap<URI, ClassLoader>();
		ClassLoader parent = classLoader.getParent();
		if (null != parent) {

			entries.putAll(getClassPathEntries(parent));
		}
		if (classLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
			for (URL url : urlClassLoader.getURLs()) {
				URI uri;
				try {
					uri = url.toURI();
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException(e);
				}
				if (!entries.containsKey(uri)) {
					entries.put(uri, classLoader);
				}
			}
		}
		return entries;
	}
}
