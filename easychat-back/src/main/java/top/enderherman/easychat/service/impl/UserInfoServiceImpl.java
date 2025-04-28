package top.enderherman.easychat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.common.ResponseCodeEnum;
import top.enderherman.easychat.component.RedisComponent;
import top.enderherman.easychat.config.AppConfig;
import top.enderherman.easychat.constants.Constants;
import top.enderherman.easychat.entity.dto.MessageSendDTO;
import top.enderherman.easychat.entity.po.UserContact;
import top.enderherman.easychat.entity.query.UserContactQuery;
import top.enderherman.easychat.mappers.UserContactMapper;
import top.enderherman.easychat.service.ChatSessionUserService;
import top.enderherman.easychat.service.UserContactService;
import top.enderherman.easychat.utils.CopyUtils;
import top.enderherman.easychat.entity.dto.TokenUserInfoDto;
import top.enderherman.easychat.entity.enums.*;
import top.enderherman.easychat.entity.po.UserInfoBeauty;
import top.enderherman.easychat.entity.query.UserInfoBeautyQuery;
import top.enderherman.easychat.entity.query.UserInfoQuery;
import top.enderherman.easychat.entity.po.UserInfo;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.entity.query.SimplePage;
import top.enderherman.easychat.entity.vo.UserInfoVO;
import top.enderherman.easychat.exception.BusinessException;
import top.enderherman.easychat.mappers.UserInfoBeautyMapper;
import top.enderherman.easychat.mappers.UserInfoMapper;
import top.enderherman.easychat.service.UserInfoService;
import top.enderherman.easychat.utils.StringUtils;
import top.enderherman.easychat.webSocket.MessageHandler;


/**
 * 用户表 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactService userContactService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private UserInfoBeautyMapper<UserInfoBeauty, UserInfoBeautyQuery> userInfoBeautyMapper;

    @Resource
    private UserContactMapper<UserContact,UserContactQuery> userContactMapper;



    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(param);
        PaginationResultVO<UserInfo> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
        StringUtils.checkParam(param);
        return this.userInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserInfoQuery param) {
        StringUtils.checkParam(param);
        return this.userInfoMapper.deleteByParam(param);
    }

    /**
     * 根据UserId获取对象
     */
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId修改
     */
    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email获取对象
     */
    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email修改
     */
    @Override
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }


    /**
     * 注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (userInfo != null) {
            throw new BusinessException("邮箱已存在");
        }

        String userId = StringUtils.getUserId();
        UserInfoBeauty beautyAccount = userInfoBeautyMapper.selectByEmail(email);
        //靓号功能 首先不为空 且未使用
        Boolean useBeautyAccount = beautyAccount != null && BeautyAccountStatusEnum.NO_USE.getStatus().equals(beautyAccount.getStatus());
        if (useBeautyAccount) {
            userId = UserContactTypeEnum.USER.getPrefix() + beautyAccount.getUserId();
            //将靓号设置为已使用
            UserInfoBeauty updateUserInfoBeauty = new UserInfoBeauty();
            updateUserInfoBeauty.setStatus(BeautyAccountStatusEnum.USEED.getStatus());
            userInfoBeautyMapper.updateByUserId(updateUserInfoBeauty, beautyAccount.getUserId());
        }

        Date current = new Date();
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        userInfo.setPassword(StringUtils.encodingByMd5(password));
        userInfo.setCreateTime(current);
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setLastOffTime(current.getTime()-1000);
        userInfo.setJoinType(JoinTypeEnum.APPLY.getType());
        userInfoMapper.insert(userInfo);

        //添加机器人好友
        userContactService.addRobotContact(userInfo.getUserId());

    }

    /**
     * 登录
     */
    @Override
    public UserInfoVO login(String email, String password) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);

        if (userInfo == null || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或密码错误");
        }

        if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        //心跳检测
        Long lastHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if (lastHeartBeat != null) {
            throw new BusinessException("此账号已在别处登录");
        }

        //查询联系人
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setUserId(userInfo.getUserId());
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> list = userContactMapper.selectList(userContactQuery);
        List<String> contactIdList=list.stream().map(UserContact::getContactId).collect(Collectors.toList());
        //保存联系人信息到redis中
        redisComponent.deleteContactBatch(userInfo.getUserId());
        if(!contactIdList.isEmpty()){
            redisComponent.saveContactBatch(userInfo.getUserId(), contactIdList);
        }

        TokenUserInfoDto dto = getTokenUserInfoDto(userInfo);
        //存储登录信息到redis中
        redisComponent.saveTokenUserInfoDto(dto);

        UserInfoVO userInfoVO = CopyUtils.copy(userInfo, UserInfoVO.class);
        userInfoVO.setToken(dto.getToken());
        userInfoVO.setAdmin(dto.isAdmin());
        return userInfoVO;
    }

    /**
     * 更新用户信息
     */
    @Override
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCoverFile) throws IOException {
        //防止注入
        userInfo.setEmail(null);
        userInfo.setPassword(null);
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setLastLoginTime(null);
        userInfo.setLastOffTime(null);
        if (avatarFile != null) {
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER;
            File targetFileFolder = new File(baseFolder + Constants.AVATAR_FOLDER);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + userInfo.getUserId() + Constants.IMAGE_SUFFIX;
            avatarFile.transferTo(new File(filePath));
            avatarCoverFile.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        }

        UserInfo dbInfo = userInfoMapper.selectByUserId(userInfo.getUserId());
        userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());
        //更新会话昵称
        String contactNameUpdate = null;
        if (!dbInfo.getNickName().equals(userInfo.getNickName())) {
            contactNameUpdate = userInfo.getNickName();
        }
        if(contactNameUpdate == null){
            return;
        }

        //更新redis信息
        TokenUserInfoDto tokenUserInfo = redisComponent.getTokenUserInfoDtoByUserId(userInfo.getUserId());
        tokenUserInfo.setNickName(contactNameUpdate);
        redisComponent.saveTokenUserInfoDto(tokenUserInfo);

        chatSessionUserService.updateRedundancyInfo(contactNameUpdate, userInfo.getUserId());

    }

    /**
     * 更新用户状态
     */
    @Override
    public void updateUserStatus(String userId, Integer status) {
        UserStatusEnum statusEnum = UserStatusEnum.getByStatus(status);
        if (statusEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(status);
        userInfoMapper.updateByUserId(userInfo, userId);
    }

    /**
     * 强制用户下线
     */
    @Override
    public void forcedOffOnline(String userId) {
        MessageSendDTO<?> messageSendDTO = new MessageSendDTO<>();
        messageSendDTO.setContactType(UserContactTypeEnum.USER.getType());
        messageSendDTO.setMessageType(MessageTypeEnum.FORCE_OFF_LINE.getType());
        messageSendDTO.setContactId(userId);
        messageHandler.sendMessage(messageSendDTO);

    }

    private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
        return TokenUserInfoDto.builder()
                .token(StringUtils.encodingByMd5(userInfo.getUserId()) + StringUtils.getRandomString(Constants.LENGTH_20))
                .userId(userInfo.getUserId())
                .nickName(userInfo.getNickName())
                .admin(!StringUtils.isEmpty(appConfig.getAdminEmails()) && ArrayUtils.contains(appConfig.getAdminEmails().split(","), userInfo.getEmail()))
                .build();
    }
}