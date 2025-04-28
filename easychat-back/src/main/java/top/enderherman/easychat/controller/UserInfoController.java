package top.enderherman.easychat.controller;

import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.annotation.GlobalInterceptor;
import top.enderherman.easychat.component.RedisComponent;
import top.enderherman.easychat.constants.Constants;

import top.enderherman.easychat.entity.dto.TokenUserInfoDto;
import top.enderherman.easychat.entity.po.UserInfo;
import top.enderherman.easychat.utils.CopyUtils;
import top.enderherman.easychat.utils.RedisUtils;

import top.enderherman.easychat.common.BaseResponse;
import top.enderherman.easychat.entity.vo.UserInfoVO;
import top.enderherman.easychat.exception.BusinessException;
import top.enderherman.easychat.service.UserInfoService;
import top.enderherman.easychat.utils.StringUtils;
import top.enderherman.easychat.webSocket.ChannelContextUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;


@Slf4j
@Validated
@RestController
@RequestMapping("/account")
public class UserInfoController extends ABaseController {


    @Resource
    private RedisUtils<Object> redisUtils;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ChannelContextUtils channelContextUtils;


    /**
     * 获取验证码
     *
     * @return 验证码对应的UUID以及base64的验证码图片
     */
    @RequestMapping("/checkCode")
    public BaseResponse<?> checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        String checkCodeBase64 = captcha.toBase64();
        redisUtils.setEx(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_KEY_EXPIRES_TEN_MIN);

        HashMap<String, String> result = new HashMap<>();

        result.put(Constants.CHECK_CODE, checkCodeBase64);
        result.put(Constants.CHECK_CODE_KEY, checkCodeKey);
        return getSuccessResponseVO(result);
    }


    @PostMapping("/register")
    public BaseResponse<?> register(@NotEmpty String checkCodeKey,
                                    @NotEmpty @Email String email,
                                    @NotEmpty @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                                    @NotEmpty String nickName,
                                    @NotEmpty String checkCode) {
        try {
            log.info("用户注册:\n 邮箱:{}\n 昵称:{}\n 密码:{}\n 验证码存储id:{}\n 验证码:{}\n", email, nickName, password, checkCodeKey, checkCode);
            if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }

            userInfoService.register(email, nickName, password);
            return getSuccessResponseVO(null);
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    @PostMapping("/login")
    public BaseResponse<?> login(@NotEmpty String checkCodeKey,
                                 @NotEmpty @Email String email,
                                 @NotEmpty String password,

                                 @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
                log.info("用户注册:\n 邮箱:{}\n 密码:{}\n 验证码存储id:{}\n 验证码:{}\n", email, password, checkCodeKey, checkCode);
                throw new BusinessException("图片验证码错误");
            }
            UserInfoVO userInfoVO = userInfoService.login(email, password);
            return getSuccessResponseVO(userInfoVO);
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    /**
     * 获取系统配置
     */
    @PostMapping("/getSysSetting")
    @GlobalInterceptor
    public BaseResponse<?> getSysSetting() {
        return getSuccessResponseVO(redisComponent.getSysSetting());
    }

    /**
     * 获取用户信息
     */
    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public BaseResponse<?> getUserInfo(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserDto(request);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        UserInfoVO userInfoVO = CopyUtils.copy(userInfo, UserInfoVO.class);
        userInfoVO.setAdmin(tokenUserInfoDto.isAdmin());
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 修改用户信息
     */
    @RequestMapping("/saveUserInfo")
    @GlobalInterceptor
    public BaseResponse<?> saveUserInfo(HttpServletRequest request,
                                        UserInfo userInfo,
                                        MultipartFile avatarFile,
                                        MultipartFile coverFile) throws IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserDto(request);
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfoService.updateUserInfo(userInfo, avatarFile, coverFile);
        return getUserInfo(request);
    }

    /**
     * 修改密码
     */
    @RequestMapping("/updatePassword")
    @GlobalInterceptor
    public BaseResponse<?> updatePassword(HttpServletRequest request, @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserDto(request);
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(StringUtils.encodingByMd5(password));
        userInfoService.updateUserInfoByUserId(userInfo, tokenUserInfoDto.getUserId());
        //重新登陆 强制退出
        channelContextUtils.closeContact(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    /**
     * 退出登录
     */
    @RequestMapping("/logout")
    @GlobalInterceptor
    public BaseResponse<?> updatePassword(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserDto(request);
        channelContextUtils.closeContact(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
