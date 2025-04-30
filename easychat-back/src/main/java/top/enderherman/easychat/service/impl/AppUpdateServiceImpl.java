package top.enderherman.easychat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.common.ResponseCodeEnum;
import top.enderherman.easychat.config.AppConfig;
import top.enderherman.easychat.constants.Constants;
import top.enderherman.easychat.entity.enums.AppUpdateFileTypeEnum;
import top.enderherman.easychat.entity.enums.AppUpdateStatusEnum;
import top.enderherman.easychat.entity.enums.PageSize;
import top.enderherman.easychat.entity.query.AppUpdateQuery;
import top.enderherman.easychat.entity.po.AppUpdate;
import top.enderherman.easychat.entity.vo.AppUpdateVO;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.entity.query.SimplePage;
import top.enderherman.easychat.exception.BusinessException;
import top.enderherman.easychat.mappers.AppUpdateMapper;
import top.enderherman.easychat.service.AppUpdateService;
import top.enderherman.easychat.utils.CopyUtils;
import top.enderherman.easychat.utils.StringUtils;


/**
 * app发布表 业务接口实现
 */
@Service("appUpdateService")
public class AppUpdateServiceImpl implements AppUpdateService {

    @Resource
    private AppUpdateMapper<AppUpdate, AppUpdateQuery> appUpdateMapper;

    @Resource
    private AppConfig appConfig;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<AppUpdate> findListByParam(AppUpdateQuery param) {
        return this.appUpdateMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(AppUpdateQuery param) {
        return this.appUpdateMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<AppUpdate> list = this.findListByParam(param);
        PaginationResultVO<AppUpdate> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(AppUpdate bean) {
        return this.appUpdateMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<AppUpdate> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<AppUpdate> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(AppUpdate bean, AppUpdateQuery param) {
        StringUtils.checkParam(param);
        return this.appUpdateMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(AppUpdateQuery param) {
        StringUtils.checkParam(param);
        return this.appUpdateMapper.deleteByParam(param);
    }

    /**
     * 根据Id获取对象
     */
    @Override
    public AppUpdate getAppUpdateById(Integer id) {
        return this.appUpdateMapper.selectById(id);
    }

    /**
     * 根据Id修改
     */
    @Override
    public Integer updateAppUpdateById(AppUpdate bean, Integer id) {
        return this.appUpdateMapper.updateById(bean, id);
    }

    /**
     * 根据Id删除
     */
    @Override
    public Integer deleteAppUpdateById(Integer id) {

        AppUpdate dbInfo = appUpdateMapper.selectById(id);
        if (!AppUpdateStatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        return this.appUpdateMapper.deleteById(id);
    }

    /**
     * 发布或者修改更新
     */
    @Override
    public void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException {
        AppUpdateFileTypeEnum fileTypeEnum = AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
        if (null == fileTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }


        if (appUpdate.getId() != null) {
            AppUpdate dbInfo = appUpdateMapper.selectById(appUpdate.getId());
            if (!AppUpdateStatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        AppUpdateQuery query = new AppUpdateQuery();
        query.setOrderBy("version desc");
        query.setSimplePage(new SimplePage(0, 1));
        List<AppUpdate> list = appUpdateMapper.selectList(query);
        if (!list.isEmpty()) {
            AppUpdate latest = list.get(0);
            long dbVersion = Long.parseLong(latest.getVersion().replace(".", ""));
            long currentVersion = Long.parseLong(appUpdate.getVersion().replace(".", ""));
            //新增时
            if (appUpdate.getId() == null && currentVersion <= dbVersion) {
                throw new BusinessException("当前版本必须大于历史版本");
            }

            //修改时
            if (appUpdate.getId() != null && currentVersion <= dbVersion && !appUpdate.getId().equals(latest.getId())) {
                throw new BusinessException("当前版本必须大于历史版本");
            }
        }
        //更新数据库
        if (appUpdate.getId() == null) {
            appUpdate.setCreateTime(new Date());
            appUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
            appUpdateMapper.insert(appUpdate);
        } else {
            appUpdate.setStatus(null);
            appUpdate.setGrayscaleUid(null);
            appUpdateMapper.updateById(appUpdate, appUpdate.getId());
        }

        if (file != null) {
            File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.APP_UPDATE_FILE);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String path = folder.getPath();
            file.transferTo(new File(path + "/" + appUpdate.getId() + Constants.APP_EXE_SUFFIX));
        }
    }

    /**
     * 发布更新
     */
    @Override
    public void postUpdate(Integer id, Integer status, String grayscaleUid) {
        AppUpdateStatusEnum statusEnum = AppUpdateStatusEnum.getByStatus(status);
        if (null == statusEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (AppUpdateStatusEnum.GRAYSCALE.equals(statusEnum) && StringUtils.isEmpty(grayscaleUid)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!AppUpdateStatusEnum.GRAYSCALE.equals(statusEnum)) {
            grayscaleUid = "";
        }
        AppUpdate update = new AppUpdate();
        update.setStatus(status);
        update.setGrayscaleUid(grayscaleUid);
        appUpdateMapper.updateById(update, id);
    }

    /**
     * 获取最后更新版本
     */
    @Override
    public AppUpdateVO getLatestUpdate(String version, String uid) {
        AppUpdate update = appUpdateMapper.selectLatestUpdate(version, uid);
        if (update == null)
            return null;
        AppUpdateVO vo = CopyUtils.copy(update, AppUpdateVO.class);
        if (AppUpdateFileTypeEnum.LOCAL.getType().equals(update.getFileType())) {
            File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.APP_UPDATE_FILE + update.getId() + Constants.APP_EXE_SUFFIX);
            vo.setSize(file.length());
        } else {
            vo.setSize(0L);
        }
        vo.setUpdateList(Arrays.asList(update.getUpdateDescArray()));
        String fileName = Constants.APP_NAME + update.getVersion() + Constants.APP_EXE_SUFFIX;
        vo.setFileName(fileName);
        return vo;
    }


}