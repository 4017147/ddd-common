package cn.mljia.ddd.common.event;


public interface ConsumedEventStore {

	/**
	 * 
	 * @Title: isExist
	 * @Description: TODO 是否处理了该事件
	 * @param eventId
	 * @param typeName
	 * @return
	 * @throws Exception
	 * @return: boolean
	 */
	public boolean isDealWithEvent(long eventId, String typeName) throws Exception;

	/**
	 * 
	 * @Title: append
	 * @Description: TODO 添加处理事件
	 * @param eventId
	 * @param typeName
	 * @return
	 * @throws Exception
	 * @return: boolean
	 */
	public ConsumedEvent append(long eventId, String typeName) throws Exception;
}
