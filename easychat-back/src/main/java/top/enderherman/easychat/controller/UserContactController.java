package top.enderherman.easychat.controller;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.enderherman.easychat.annotation.GlobalInterceptor;
import top.enderherman.easychat.common.BaseResponse;
import top.enderherman.easychat.common.ResponseCodeEnum;
import top.enderherman.easychat.entity.dto.TokenUserInfoDto;
import top.enderherman.easychat.entity.enums.PageSize;
import top.enderherman.easychat.entity.enums.UserContactStatusEnum;
import top.enderherman.easychat.entity.po.UserContact;
import top.enderherman.easychat.entity.po.UserContactApply;
import top.enderherman.easychat.entity.po.UserInfo;
import top.enderherman.easychat.entity.query.UserContactApplyQuery;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.entity.vo.UserContactSearchResultVO;
import top.enderherman.easychat.entity.vo.UserInfoVO;
import top.enderherman.easychat.exception.BusinessException;
import top.enderherman.easychat.service.UserContactApplyService;
import top.enderherman.easychat.service.UserContactService;
import top.enderherman.easychat.service.UserInfoService;
import top.enderherman.easychat.utils.CopyUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/contact")
public class UserContactController extends ABaseController {

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserContactApplyService userContactApplyService;

    /**
     * 搜索
     */
    @RequestMapping("/search")
    @GlobalInterceptor
    public BaseResponse<?> search(HttpServletRequest request, @NotEmpty String contactId) {
        TokenUserInfoDto userDto = getTokenUserDto(request);
        UserContactSearchResultVO resultVO = userContactService.searchContact(userDto.getUserId(), contactId);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 申请加好友/群
     */
    @RequestMapping("/applyAdd")
    @GlobalInterceptor
    public BaseResponse<?> applyAdd(HttpServletRequest request, @NotEmpty String contactId, String applyInfo) {
        TokenUserInfoDto userDto = getTokenUserDto(request);
        Integer joinType = userContactService.apply(userDto, contactId, applyInfo);
        return getSuccessResponseVO(joinType);
    }

    /**
     * 获取申请列表
     */
    @RequestMapping("/loadApply")
    @GlobalInterceptor
    public BaseResponse<?> loadApply(HttpServletRequest request, Integer pageNo) {
        TokenUserInfoDto userDto = getTokenUserDto(request);
        UserContactApplyQuery query = new UserContactApplyQuery();
        query.setOrderBy("last_apply_time desc");
        query.setReceiveUserId(userDto.getUserId());
        query.setPageNo(pageNo);
        query.setPageSize(PageSize.SIZE15.getSize());
        query.setQueryContactInfo(true);
        PaginationResultVO<UserContactApply> resultVO = userContactApplyService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 处理好友群聊申请
     */
    @RequestMapping("/dealWithApply")
    @GlobalInterceptor
    public BaseResponse<?> dealWithApply(HttpServletRequest request,
                                         @NotNull Integer applyId,
                                         @NotNull Integer status) {
        TokenUserInfoDto userDto = getTokenUserDto(request);
        userContactApplyService.dealWithApply(userDto.getUserId(), applyId, status);
        return getSuccessResponseVO(null);
    }

    /**
     * 加载联系人/群组列表
     */
    @RequestMapping("/loadContact")
    @GlobalInterceptor
    public BaseResponse<?> loadContact(HttpServletRequest request,
                                       @NotNull String contactType) {
        TokenUserInfoDto userDto = getTokenUserDto(request);
        return getSuccessResponseVO(userContactService.loadContact(userDto.getUserId(), contactType));
    }

    /**
     * 获取联系人信息 不一定是好友
     */
    @RequestMapping("/getContactInfo")
    @GlobalInterceptor
    public BaseResponse<?> getContactInfo(HttpServletRequest request, @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserDto(request);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(contactId);
        UserInfoVO userInfoVO = CopyUtils.copy(userInfo, UserInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());

        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), contactId);
        if (userContact != null) {
            userInfoVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 获取用户信息 一定是好友 能看到都
     */
    @RequestMapping("/getContactUserInfo")
    @GlobalInterceptor
    public BaseResponse<?> getContactUserInfo(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserDto(request);
        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), contactId);
        if (userContact == null || !ArrayUtils.contains(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus()
        }, userContact.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(contactId);
        UserInfoVO userInfoVO = CopyUtils.copy(userInfo, UserInfoVO.class);
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 删除联系人
     */
    @RequestMapping("/delContact")
    @GlobalInterceptor
    public BaseResponse<?> delContact(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserDto(request);
        userContactService.changeContactType(tokenUserInfoDto.getUserId(), contactId, UserContactStatusEnum.DEL.getStatus());
        return getSuccessResponseVO(null);
    }

    /**
     * 拉黑联系人
     */
    @RequestMapping("/addContact2BlackList")
    @GlobalInterceptor
    public BaseResponse<?> addContact2BlackList(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserDto(request);
        userContactService.changeContactType(tokenUserInfoDto.getUserId(), contactId, UserContactStatusEnum.BLACKLIST.getStatus());
        return getSuccessResponseVO(null);
    }
}
