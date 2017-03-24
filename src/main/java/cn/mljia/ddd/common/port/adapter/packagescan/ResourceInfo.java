package cn.mljia.ddd.common.port.adapter.packagescan;

import static cn.mljia.ddd.common.port.adapter.packagescan.PreConditions.*;

import java.net.URL;

/**
 * 资源信息
 */
public class ResourceInfo {

	/**
	 * 资源名称
	 */
	private final String resourceName;

	public static ResourceInfo of(String resourceName, ClassLoader loader) {
		if (resourceName.endsWith(ClassInfo.CLASS_FILE_NAME_EXTENSION)) {

			return new ClassInfo(resourceName, loader);

		}
		return new ResourceInfo(resourceName, loader);
	}

	/**
	 * 类加载器
	 */
	protected ClassLoader loader;

	public ResourceInfo(String resourceName, ClassLoader loader) {
		this.resourceName = checkNotNull(resourceName);
		this.loader = checkNotNull(loader);
	}

	/**
	 * 得到资源路径
	 * 
	 * @return
	 */
	public final URL getURL() {

		return checkNotNull(loader.getResource(resourceName));
	}

	/**
	 * 得到资源名称
	 * 
	 * @return
	 */
	public final String getResourceName() {

		return this.resourceName;
	}
}
