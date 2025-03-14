package top.enderherman.easychat.service;

import java.util.List;

import top.enderherman.easychat.entity.query.UserContactApplyQuery;
import top.enderherman.easychat.entity.po.UserContactApply;
import top.enderherman.easychat.entity.vo.PaginationResultVO;


/**
 * 联系人申请表 业务接口
 */
public interface UserContactApplyService {

	/**
	 * 根据条件查询列表
	 */
	List<UserContactApply> findListByParam(UserContactApplyQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserContactApplyQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserContactApply> findListByPage(UserContactApplyQuery param);

	/**
	 * 新增
	 */
	Integer add(UserContactApply bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserContactApply> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserContactApply> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(UserContactApply bean,UserContactApplyQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(UserContactApplyQuery param);

	/**
	 * 根据ApplyId查询对象
	 */
	UserContactApply getUserContactApplyByApplyId(Integer applyId);


	/**
	 * 根据ApplyId修改
	 */
	Integer updateUserContactApplyByApplyId(UserContactApply bean,Integer applyId);


	/**
	 * 根据ApplyId删除
	 */
	Integer deleteUserContactApplyByApplyId(Integer applyId);


	/**
	 * 根据ApplyIdAndReceiveUserIdAndContactId查询对象
	 */
	UserContactApply getUserContactApplyByApplyIdAndReceiveUserIdAndContactId(Integer applyId,String receiveUserId,String contactId);


	/**
	 * 根据ApplyIdAndReceiveUserIdAndContactId修改
	 */
	Integer updateUserContactApplyByApplyIdAndReceiveUserIdAndContactId(UserContactApply bean,Integer applyId,String receiveUserId,String contactId);


	/**
	 * 根据ApplyIdAndReceiveUserIdAndContactId删除
	 */
	Integer deleteUserContactApplyByApplyIdAndReceiveUserIdAndContactId(Integer applyId,String receiveUserId,String contactId);

}