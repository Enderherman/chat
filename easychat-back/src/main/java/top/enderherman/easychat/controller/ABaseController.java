package top.enderherman.easychat.controller;

import top.enderherman.easychat.constants.Constants;
import top.enderherman.easychat.utils.RedisUtils;
import top.enderherman.easychat.entity.dto.TokenUserInfoDto;
import top.enderherman.easychat.common.ResponseCodeEnum;
import top.enderherman.easychat.common.BaseResponse;
import top.enderherman.easychat.exception.BusinessException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


public class ABaseController {

    protected static final String STATUS_SUCCESS = "success";

    protected static final String STATUS_ERROR = "error";


    @Resource
    private RedisUtils<?> redisUtils;

    /**
     * 响应成功
     *
     * @param t 返回数据
     * @return restful返回类型数据
     * @date 2024/12/24
     */
    protected <T> BaseResponse<T> getSuccessResponseVO(T t) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(STATUS_SUCCESS);
        baseResponse.setCode(ResponseCodeEnum.CODE_200.getCode());
        baseResponse.setMessage(ResponseCodeEnum.CODE_200.getMsg());
        baseResponse.setData(t);
        return baseResponse;
    }

    /**
     * 业务异常响应失败 请求参数错误
     *
     * @param t 返回数据
     * @param e 异常堆栈
     * @return restful格式返回类型数据
     * @date 2024/12/24
     */
    protected <T> BaseResponse<T> getBusinessErrorResponseVO(BusinessException e, T t) {
        BaseResponse<T> vo = new BaseResponse<>();
        vo.setStatus(STATUS_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setMessage(e.getMessage());
        vo.setData(t);
        return vo;
    }


    /**
     * 服务器异常
     *
     * @param t 返回数据
     * @return restful格式返回类型数据
     * @date 2024/12/24
     */
    protected <T> BaseResponse<T> getServerErrorResponseVO(T t) {
        BaseResponse<T> vo = new BaseResponse<>();
        vo.setStatus(STATUS_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setMessage(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    protected TokenUserInfoDto getTokenUserDto(HttpServletRequest request) {
        String token = request.getHeader("token");
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN +token);
    }
}
