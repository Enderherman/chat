package top.enderherman.easychat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.component.RedisComponent;
import top.enderherman.easychat.config.AppConfig;
import top.enderherman.easychat.constants.Constants;
import top.enderherman.easychat.entity.enums.GroupStatusEnum;
import top.enderherman.easychat.utils.StringUtils;
import top.enderherman.easychat.entity.dto.SysSettingDto;
import top.enderherman.easychat.entity.enums.PageSize;
import top.enderherman.easychat.common.ResponseCodeEnum;
import top.enderherman.easychat.entity.enums.UserContactStatusEnum;
import top.enderherman.easychat.entity.enums.UserContactTypeEnum;
import top.enderherman.easychat.entity.po.UserContact;
import top.enderherman.easychat.entity.query.GroupInfoQuery;
import top.enderherman.easychat.entity.po.GroupInfo;
import top.enderherman.easychat.entity.query.UserContactQuery;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.entity.query.SimplePage;
import top.enderherman.easychat.exception.BusinessException;
import top.enderherman.easychat.mappers.GroupInfoMapper;
import top.enderherman.easychat.mappers.UserContactMapper;
import top.enderherman.easychat.service.GroupInfoService;
import top.enderherman.easychat.utils.StringUtils;


/**
 * 群组信息表 业务接口实现
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private AppConfig appConfig;

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
        StringUtils.checkParam(param);
        return this.groupInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(GroupInfoQuery param) {
        StringUtils.checkParam(param);
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
        //1.新增
        if (StringUtils.isEmpty(groupInfo.getGroupId())) {

            //1.1查询已有群组数量
            GroupInfoQuery query = new GroupInfoQuery();
            query.setGroupOwnId(groupInfo.getGroupOwnId());
            Integer count = groupInfoMapper.selectCount(query);
            groupInfo.setGroupId(StringUtils.getGroupId());
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count >= sysSettingDto.getMaxGroupCount()) {
                throw new BusinessException("最多支持创建" + sysSettingDto.getMaxGroupCount() + "个群聊");
            }

            //TODO 新建群聊记得传头像喔
            if (avatarFile == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }

            groupInfo.setCreateTime(currentDate);
            groupInfo.setGroupId(StringUtils.getGroupId());
            groupInfoMapper.insert(groupInfo);

            //1.2将群组设置为联系人
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnId());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setCreateTime(currentDate);
            userContactMapper.insert(userContact);

            //TODO 创建会话
            //TODO 发送欢迎消息


        }
        //2.修改
        else {
            GroupInfo dbInfo = groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
            if (dbInfo.getGroupOwnId().equals(groupInfo.getGroupOwnId())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }

            groupInfoMapper.updateByGroupId(groupInfo, groupInfo.getGroupId());
            //TODO 更新相关表冗余信息

            //TODO 修改群昵称发送ws信息
        }

        //3.群头像处理
        if (avatarFile == null) {
            return;
        }
        //3.1头像目录
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER;
        File avatarFolder = new File(baseFolder + Constants.AVATAR_FOLDER);
        if (!avatarFolder.exists()) {
            avatarFolder.mkdirs();
        }
        //3.2头像路径
        String filePath = avatarFolder.getPath() + groupInfo.getGroupId() + Constants.IMAGE_SUFFIX;
        try {
            avatarFile.transferTo(new File(filePath));
            avatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        } catch (IOException e) {
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }


    }

    /**
     * 解散群聊
     */
    @Override
    public void dissolutionGroup(String groupOwnerId, String groupId) {
        GroupInfo dbInfo = groupInfoMapper.selectByGroupId(groupId);
        if (dbInfo == null || !dbInfo.getGroupOwnId().equals(groupOwnerId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //删除群组
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
        groupInfoMapper.updateByGroupId(groupInfo, groupId);

        //更新联系人信息
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());

        UserContact userContact = new UserContact();
        userContact.setStatus(UserContactStatusEnum.DEL.getStatus());
        userContactMapper.updateByParam(userContact, userContactQuery);

        //TODO 移除相关联系人缓存

        //TODO 更新会话消息 记录群消息 发送群解散消息

    }
}