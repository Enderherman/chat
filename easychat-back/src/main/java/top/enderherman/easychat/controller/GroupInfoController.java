package top.enderherman.easychat.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.annotation.GlobalInterceptor;
import top.enderherman.easychat.entity.dto.TokenUserInfoDto;
import top.enderherman.easychat.entity.enums.GroupStatusEnum;
import top.enderherman.easychat.entity.enums.UserContactStatusEnum;
import top.enderherman.easychat.entity.po.GroupInfo;
import top.enderherman.easychat.common.BaseResponse;
import top.enderherman.easychat.entity.po.UserContact;
import top.enderherman.easychat.entity.query.GroupInfoQuery;
import top.enderherman.easychat.entity.query.UserContactQuery;
import top.enderherman.easychat.entity.vo.GroupInfoVO;
import top.enderherman.easychat.exception.BusinessException;
import top.enderherman.easychat.service.GroupInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.enderherman.easychat.service.UserContactService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 群组信息表 Controller
 */
@RequestMapping("/group")
@RestController("groupInfoController")

public class GroupInfoController extends ABaseController {

    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private UserContactService userContactService;

    /**
     * 新增群组 或 修改群组信息
     */
    @PostMapping("/saveGroup")
    @GlobalInterceptor
    public BaseResponse<?> saveGroup(HttpServletRequest request,
                                     String groupId,
                                     @NotEmpty String groupName,
                                     String groupNotice,
                                     @NotNull Integer joinType,
                                     MultipartFile avatarFile,
                                     MultipartFile avatarCover) {
        TokenUserInfoDto tokenUserDto = getTokenUserDto(request);
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setGroupOwnId(tokenUserDto.getUserId());
        groupInfo.setJoinType(joinType);
        groupInfoService.saveGroup(groupInfo, avatarFile, avatarCover);
        return getSuccessResponseVO(null);
    }

    /**
     * 查看我的群组
     */
    @PostMapping("/loadMyGroup")
    @GlobalInterceptor
    public BaseResponse<?> loadMyGroup(HttpServletRequest request) {
        TokenUserInfoDto userDto = getTokenUserDto(request);
        GroupInfoQuery query = new GroupInfoQuery();
        query.setStatus(GroupStatusEnum.NORMAL.getStatus());
        query.setQueryMemberCount(true);
        query.setGroupOwnId(userDto.getUserId());
        query.setOrderBy("create_time desc");
        List<GroupInfo> groupInfoList = groupInfoService.findListByParam(query);
        return getSuccessResponseVO(groupInfoList);
    }

    /**
     * 获取群组信息详情
     */
    @RequestMapping("/getGroupInfo")
    @GlobalInterceptor
    public BaseResponse<?> getGroupInfo(HttpServletRequest request,
                                        @NotEmpty String groupId) {
        TokenUserInfoDto userDto = getTokenUserDto(request);
        GroupInfo groupInfo = getGroupDetailInfo(userDto.getUserId(), groupId);

        UserContactQuery query = new UserContactQuery();
        query.setContactId(groupId);
        Integer memberCount = userContactService.findCountByParam(query);
        groupInfo.setMemberCount(memberCount);
        return getSuccessResponseVO(groupInfo);

    }

    /**
     * 聊天中获取群组信息详情
     */
    @RequestMapping("/getGroupInfo4Chat")
    @GlobalInterceptor
    public BaseResponse<?> getGroupInfo4Chat(HttpServletRequest request,
                                             @NotEmpty String groupId) {
        TokenUserInfoDto userDto = getTokenUserDto(request);
        GroupInfo groupInfo = getGroupDetailInfo(userDto.getUserId(), groupId);
        UserContactQuery query = new UserContactQuery();
        query.setContactId(groupId);
        query.setQueryUserInfo(true);

        query.setOrderBy("create_time desc");
        query.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList = userContactService.findListByParam(query);
        GroupInfoVO groupInfoVO = new GroupInfoVO(groupInfo, userContactList);
        return getSuccessResponseVO(groupInfoVO);

    }


    /**
     * 获取群聊信息
     */
    private GroupInfo getGroupDetailInfo(String userId, @NotEmpty String groupId) {
        //1.先校验是不是这个群的
        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(userId, groupId);
        if (userContact == null || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException("您已退出群聊或群聊不存在");
        }

        GroupInfo groupInfo = groupInfoService.getGroupInfoByGroupId(groupId);
        if (groupInfo == null || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException("群聊不存在或已解散");
        }
        return groupInfo;
    }

}