package top.enderherman.easychat.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 联系人申请表 数据库操作接口
 */
public interface UserContactApplyMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ApplyId更新
	 */
	 Integer updateByApplyId(@Param("bean") T t,@Param("applyId") Integer applyId);


	/**
	 * 根据ApplyId删除
	 */
	 Integer deleteByApplyId(@Param("applyId") Integer applyId);


	/**
	 * 根据ApplyId获取对象
	 */
	 T selectByApplyId(@Param("applyId") Integer applyId);


	/**
	 * 根据ApplyIdAndReceiveUserIdAndContactId更新
	 */
	 Integer updateByApplyIdAndReceiveUserIdAndContactId(@Param("bean") T t,@Param("applyId") Integer applyId,@Param("receiveUserId") String receiveUserId,@Param("contactId") String contactId);


	/**
	 * 根据ApplyIdAndReceiveUserIdAndContactId删除
	 */
	 Integer deleteByApplyIdAndReceiveUserIdAndContactId(@Param("applyId") Integer applyId,@Param("receiveUserId") String receiveUserId,@Param("contactId") String contactId);


	/**
	 * 根据ApplyIdAndReceiveUserIdAndContactId获取对象
	 */
	 T selectByApplyIdAndReceiveUserIdAndContactId(@Param("applyId") Integer applyId,@Param("receiveUserId") String receiveUserId,@Param("contactId") String contactId);


}
