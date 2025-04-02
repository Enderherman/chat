package top.enderherman.easychat.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.enderherman.easychat.annotation.GlobalInterceptor;
import top.enderherman.easychat.common.BaseResponse;
import top.enderherman.easychat.entity.po.UserInfoBeauty;
import top.enderherman.easychat.entity.query.UserInfoBeautyQuery;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.service.UserInfoBeautyService;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/userInfoBeauty")
public class UserInfoBeautyController {

    @Resource
    private UserInfoBeautyService userInfoBeautyService;

    /**
     * 加载靓号列表
     */
    @PostMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<PaginationResultVO<UserInfoBeauty>> loadBeautyAccountList(UserInfoBeautyQuery query) {
        query.setOrderBy("id desc");
        PaginationResultVO<UserInfoBeauty> resultVO = userInfoBeautyService.findListByPage(query);
        return BaseResponse.success(resultVO);
    }


    /**
     * 新增/修改靓号
     */
    @PostMapping("/saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> saveBeautyAccount(UserInfoBeauty userInfoBeauty) {
        userInfoBeautyService.saveBeautyAccount(userInfoBeauty);
        return BaseResponse.success();
    }

    /**
     * 删除靓号
     */
    @PostMapping("/deleteBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> deleteBeautyAccount(@NotNull Integer id) {
        userInfoBeautyService.deleteUserInfoBeautyById(id);
        return BaseResponse.success();
    }
}
