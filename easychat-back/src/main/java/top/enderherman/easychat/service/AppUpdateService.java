package top.enderherman.easychat.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.entity.query.AppUpdateQuery;
import top.enderherman.easychat.entity.po.AppUpdate;
import top.enderherman.easychat.entity.vo.PaginationResultVO;


/**
 * app发布表 业务接口
 */
public interface AppUpdateService {

	/**
	 * 根据条件查询列表
	 */
	List<AppUpdate> findListByParam(AppUpdateQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(AppUpdateQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery param);

	/**
	 * 新增
	 */
	Integer add(AppUpdate bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<AppUpdate> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<AppUpdate> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(AppUpdate bean,AppUpdateQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(AppUpdateQuery param);

	/**
	 * 根据Id查询对象
	 */
	AppUpdate getAppUpdateById(Integer id);


	/**
	 * 根据Id修改
	 */
	Integer updateAppUpdateById(AppUpdate bean,Integer id);


	/**
	 * 根据Id删除
	 */
	Integer deleteAppUpdateById(Integer id);

	/**
	 * 发布或者修改更新
	 */
	void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException;

	/**
	 * 发布更新
	 */
	void postUpdate(Integer id, Integer status, String grayscaleUid);

	/**
	 * 获取最后更新版本
	 */
	void getLatestUpdate(String version, String uid);
}