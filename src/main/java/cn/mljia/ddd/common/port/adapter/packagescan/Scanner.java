package cn.mljia.ddd.common.port.adapter.packagescan;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 扫描类
 */
public class Scanner {

	/**
	 * 已扫描到的URI
	 */
	private Set<URI> scannedURIs = new HashSet<URI>();

	/**
	 * 存储已扫描的资源信息
	 */
	private ArrayList<ResourceInfo> resources = new ArrayList<ResourceInfo>();

	/**
	 * 扫描路径
	 * 
	 * @param uri
	 * @param classLoader
	 */
	public void scan(URI uri, ClassLoader classLoader) {
		// 判断当前uri是否为file资源和是否已经扫描路径
		if ("file".equals(uri.getScheme()) && scannedURIs.add(uri)) {

			scanFrom(new File(uri), classLoader);
		}
	}

	/**
	 * 扫描指定文件
	 * 
	 * @param file
	 * 
	 * @param classLoader
	 */
	public void scanFrom(File file, ClassLoader classLoader) {
		if (!file.exists())
			return;
		if (file.isDirectory()) {

			scanDirectory(file, classLoader);
		} else {

			scanJarFile(file, classLoader);
		}
	}

	/**
	 * 扫描目录
	 * 
	 * @param directory
	 * @param classLoader
	 */
	public void scanDirectory(File directory, ClassLoader classLoader) {

		scanDirectory(directory, "", classLoader);
	}

	/**
	 * 扫描目录
	 * 
	 * @param directory
	 * @param prefixPackage
	 * @param classLoader
	 */
	public void scanDirectory(File directory, String prefixPackage, ClassLoader classLoader) {
		File[] files = directory.listFiles();
		if (null == files)
			return;
		for (File file : files) {
			String name = file.getName();
			if (file.isDirectory()) {

				scanDirectory(file, prefixPackage + name + "/", classLoader);
			} else {
				String resourceName = prefixPackage + name;
				resources.add(ResourceInfo.of(resourceName, classLoader));
			}
		}
	}

	/**
	 * 扫描jar包文件
	 * 
	 * @param file
	 * @param classLoader
	 */
	public void scanJarFile(File file, ClassLoader classLoader) {
		JarFile jarFile;
		try {
			jarFile = new JarFile(file);
		} catch (IOException e) {
			return;
		}
		try {
			for (URI uri : getClassFromManifest(file, jarFile.getManifest())) {
				scan(uri, classLoader);
			}
			for (Enumeration<JarEntry> jarEntrys = jarFile.entries(); jarEntrys.hasMoreElements();) {
				JarEntry jarEntry = jarEntrys.nextElement();
				if (jarEntry.isDirectory() || JarFile.MANIFEST_NAME.equals(jarEntry.getName())) {
					continue;
				}
				resources.add(ResourceInfo.of(jarEntry.getName(), classLoader));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				jarFile.close();
			} catch (IOException igore) {
			}
		}
	}

	/**
	 * 得到类清单
	 * 
	 * @param jarFile
	 * @param manifest
	 * @return
	 */
	public List<URI> getClassFromManifest(File jarFile, Manifest manifest) {
		if (null == manifest)
			return Collections.emptyList();
		List<URI> uris = new ArrayList<URI>();
		String classPathAttribute = manifest.getMainAttributes().getValue(Name.CLASS_PATH);
		if (null != classPathAttribute) {
			String[] classPaths = classPathAttribute.split(" ");
			for (String classPath : classPaths) {
				URI uri;
				try {
					uri = getClassPathEntry(jarFile, classPath);
				} catch (URISyntaxException e) {
					e.printStackTrace();
					continue;
				}
				uris.add(uri);
			}
		}
		return uris;
	}

	/**
	 * 得到类路径记录
	 * 
	 * @param jarFile
	 * @param classPath
	 * @return
	 * @throws URISyntaxException
	 */
	public URI getClassPathEntry(File jarFile, String classPath) throws URISyntaxException {
		URI uri = new URI(classPath);
		// 判断是否为绝对路径 c:/jar.jar
		if (uri.isAbsolute()) {

			return uri;
		}
		// 相对路径处理
		String firstSign = classPath.substring(0, 1);
		// 相对路径 a.jar
		if (!"/".equals(firstSign))
			return new File(jarFile.getParentFile(), classPath).toURI();
		// 相对路径 /a.jar
		File[] listRoots = File.listRoots();
		for (File listRoot : listRoots) {
			if (!jarFile.getAbsolutePath().startsWith(listRoot.getPath()))
				continue;
			return new File(listRoot.getPath() + classPath.substring(1)).toURI();
		}
		return new URI("");
	}

	/**
	 * 得到资源
	 * 
	 * @return
	 */
	public ArrayList<ResourceInfo> getResources() {

		return this.resources;
	}
}
