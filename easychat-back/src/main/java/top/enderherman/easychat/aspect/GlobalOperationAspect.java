package top.enderherman.easychat.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.enderherman.easychat.constants.Constants;
import top.enderherman.easychat.utils.RedisUtils;
import top.enderherman.easychat.annotation.GlobalInterceptor;
import top.enderherman.easychat.utils.StringUtils;
import top.enderherman.easychat.entity.dto.TokenUserInfoDto;
import top.enderherman.easychat.common.ResponseCodeEnum;
import top.enderherman.easychat.exception.BusinessException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {

    @Resource
    private RedisUtils redisUtils;

    @Pointcut("@annotation(top.enderherman.easychat.annotation.GlobalInterceptor)")
    private void pointcut() {
    }

    @Before("pointcut()")
    public void interceptor(JoinPoint point) throws BusinessException {
        try {
            //1.获取方法相关信息
            Object target = point.getTarget();
            String methodName = point.getSignature().getName();
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getParameterTypes();
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if (interceptor == null) {
                return;
            }
            //2.校验登录
            if (interceptor.checkLogin() || interceptor.checkAdmin()) {
                validateLogin(interceptor.checkAdmin());
            }

        } catch (BusinessException e) {
            log.error("全局拦截器异常", e);
            throw e;
        } catch (Throwable e) {
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void validateLogin(Boolean checkAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        TokenUserInfoDto userInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        if (userInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (checkAdmin && !userInfoDto.isAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }


}
