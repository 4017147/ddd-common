package cn.mljia.ddd.common.port.adapter.packagescan;

/**
 * 
 */
public class PreConditions {

	public static <T> T checkNotNull(T reference) {
		if (null == reference) {

			throw new NullPointerException();
		}
		return reference;
	}

	public static <T> T checkNotNull(T reference, String errorMessagesTemplate, Object... errorMessagesArgs) {
		if (null == reference) {

			throw new NullPointerException(String.format(errorMessagesTemplate, errorMessagesArgs));
		}
		return reference;
	}
}
