package top.enderherman.easychat.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.common.constants.Constants;
import top.enderherman.easychat.common.utils.StringUtils;
import top.enderherman.easychat.entity.enums.PageSize;
import top.enderherman.easychat.entity.enums.UserContactTypeEnum;
import top.enderherman.easychat.entity.query.GroupInfoQuery;
import top.enderherman.easychat.entity.po.GroupInfo;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.entity.query.SimplePage;
import top.enderherman.easychat.mappers.GroupInfoMapper;
import top.enderherman.easychat.service.GroupInfoService;
import top.enderherman.easychat.utils.StringTools;


/**
 * 群组信息表 业务接口实现
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<GroupInfo> findListByParam(GroupInfoQuery param) {
        return this.groupInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(GroupInfoQuery param) {
        return this.groupInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<GroupInfo> list = this.findListByParam(param);
        PaginationResultVO<GroupInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(GroupInfo bean) {
        return this.groupInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(GroupInfo bean, GroupInfoQuery param) {
        StringTools.checkParam(param);
        return this.groupInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(GroupInfoQuery param) {
        StringTools.checkParam(param);
        return this.groupInfoMapper.deleteByParam(param);
    }

    /**
     * 根据GroupId获取对象
     */
    @Override
    public GroupInfo getGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.selectByGroupId(groupId);
    }

    /**
     * 根据GroupId修改
     */
    @Override
    public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
        return this.groupInfoMapper.updateByGroupId(bean, groupId);
    }

    /**
     * 根据GroupId删除
     */
    @Override
    public Integer deleteGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.deleteByGroupId(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) {
        Date currentDate = new Date();

        //1.新增用户id
        if (StringUtils.isEmpty(groupInfo.getGroupId())) {
            groupInfo.setGroupId(StringUtils.getGroupId());
        }
    }
}