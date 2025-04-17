package top.enderherman.easychat.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.annotation.GlobalInterceptor;
import top.enderherman.easychat.common.BaseResponse;
import top.enderherman.easychat.component.RedisComponent;
import top.enderherman.easychat.config.AppConfig;
import top.enderherman.easychat.constants.Constants;
import top.enderherman.easychat.entity.dto.SysSettingDto;
import top.enderherman.easychat.entity.po.GroupInfo;
import top.enderherman.easychat.entity.po.UserInfo;
import top.enderherman.easychat.entity.query.GroupInfoQuery;
import top.enderherman.easychat.entity.query.UserInfoQuery;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.service.GroupInfoService;
import top.enderherman.easychat.service.UserInfoService;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/account")
public class ManageController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    /**
     * 获取用户信息
     */
    @RequestMapping("/loadUser")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> loadUser(UserInfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("create_time desc");
        PaginationResultVO<UserInfo> list = userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(list);
    }

    /**
     * 更改用户账户状态
     */
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> updateUserStatus(@NotEmpty String userId, @NotNull Integer status) {
        userInfoService.updateUserStatus(userId, status);
        return getSuccessResponseVO(null);
    }

    /**
     * 强制下线
     */
    @RequestMapping("/forcedOffOnline")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> forcedOffOnline(@NotEmpty String userId) {
        userInfoService.forcedOffOnline(userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取所有群组信息
     */
    @RequestMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<PaginationResultVO<GroupInfo>> loadGroup(GroupInfoQuery query) {
        query.setOrderBy("create_time desc");
        query.setQueryGroupOwnerName(true);
        query.setQueryMemberCount(true);
        PaginationResultVO<GroupInfo> listByPage = groupInfoService.findListByPage(query);
        return BaseResponse.success(listByPage);
    }

    /**
     * 直接解散群组
     */
    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> dissolutionGroup(@NotNull String groupOwnerId, @NotNull String groupId) {
        groupInfoService.dissolutionGroup(groupOwnerId, groupId);
        return BaseResponse.success();
    }

    /**
     * 获取系统设置
     */
    @RequestMapping("/getSystemSetting")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> getSystemSetting() {
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        return BaseResponse.success(sysSettingDto);
    }


    /**
     * 保存系统设置
     */
    @RequestMapping("/saveSystemSetting")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> saveSystemSetting(SysSettingDto sysSettingDto,
                                             MultipartFile robotAvatarFile,
                                             MultipartFile robotAvatarCoverFile) throws IOException {
        if (robotAvatarFile != null) {
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER;
            File targetFileFolder = new File(baseFolder + Constants.AVATAR_FOLDER);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + Constants.ROBOT_UID + Constants.IMAGE_SUFFIX;
            robotAvatarFile.transferTo(new File(filePath));
            robotAvatarCoverFile.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        }
        redisComponent.saveSysSetting(sysSettingDto);
        return BaseResponse.success();
    }


}
