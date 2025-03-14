package top.enderherman.easychat.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.annotation.GlobalInterceptor;
import top.enderherman.easychat.entity.dto.TokenUserInfoDto;
import top.enderherman.easychat.entity.query.GroupInfoQuery;
import top.enderherman.easychat.entity.po.GroupInfo;
import top.enderherman.easychat.entity.vo.BaseResponse;
import top.enderherman.easychat.service.GroupInfoService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 群组信息表 Controller
 */
@RequestMapping("/group")
@RestController("groupInfoController")

public class GroupInfoController extends ABaseController {

    @Resource
    private GroupInfoService groupInfoService;

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

}