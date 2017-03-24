package cn.mljia.ddd.common.port.adapter.packagescan;

public class ClassInfo extends ResourceInfo {

	/**
	 * class文件后缀
	 */
	public static final String CLASS_FILE_NAME_EXTENSION = ".class";

	private String className;

	public ClassInfo(String resourcesName, ClassLoader loader) {
		super(resourcesName, loader);
		this.className = getClassName(resourcesName);
	}

	public String getName() {

		return this.className;
	}

	public String getSimpleName() {
		int lastDollarSign = className.lastIndexOf("$");
		if (-1 != lastDollarSign) {
			String innerClassName = className.substring(lastDollarSign + 1);
			return innerClassName;
		}
		String packageName = getPackageName();
		if (packageName.isEmpty())
			return className;
		return className.substring(packageName.length() + 1);
	}

	/**
	 * 得到包名称
	 * 
	 * @return
	 */
	public String getPackageName() {
		int lastDot = className.lastIndexOf(".");
		return 0 > lastDot ? "" : className.substring(0, lastDot);
	}

	private String getClassName(String filename) {
		int classNameEnd = filename.length() - CLASS_FILE_NAME_EXTENSION.length();
		return filename.substring(0, classNameEnd).replace("/", ".");
	}

	public Class<?> load() {
		try {
			return loader.loadClass(this.className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

}
