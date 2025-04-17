package top.enderherman.easychat.component;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.enderherman.easychat.constants.Constants;
import top.enderherman.easychat.utils.RedisUtils;
import top.enderherman.easychat.entity.dto.SysSettingDto;
import top.enderherman.easychat.entity.dto.TokenUserInfoDto;

import javax.annotation.Resource;

@Slf4j
@Component("redisComponent")
public class RedisComponent {

    @Resource
    private RedisUtils<Object> redisUtils;

    /**
     * 获取系统参数
     */
    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto = sysSettingDto == null ? new SysSettingDto() : sysSettingDto;
        return sysSettingDto;
    }

    /**
     * 保存系统参数
     */
    public void saveSysSetting(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    /**
     * 存储用户Token
     */
    public void saveTokenUserInfoDto(TokenUserInfoDto dto) {
        redisUtils.setEx(Constants.REDIS_KEY_WS_TOKEN + dto.getToken(), dto, Constants.REDIS_KEY_EXPIRES_DAY * 2);
        redisUtils.setEx(Constants.REDIS_KEY_WS_TOKEN_USERID + dto.getUserId(), dto.getToken(), Constants.REDIS_KEY_EXPIRES_DAY * 2);
    }

    /**
     * 获取用户Token
     */
    public TokenUserInfoDto getTokenUserInfoDto(String token) {
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
    }

    /**
     * 获取心跳
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtils.get(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    /**
     * 存储心跳
     */
    public void saveUserHeartBeat(String userId) {
        redisUtils.setEx(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId, System.currentTimeMillis(), Constants.REDIS_KEY_EXPIRES_HEART_BEAT);

    }


    /**
     * 保存通道
     */
    public void saveChannel(String userId, Channel channel) {
        redisUtils.set(Constants.REDIS_KEY_WS_TOKEN + userId, channel);
    }


}
