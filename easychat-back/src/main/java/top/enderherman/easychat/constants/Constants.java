package top.enderherman.easychat.constants;

import top.enderherman.easychat.entity.enums.UserContactTypeEnum;

public class Constants {
    public static final String ZERO_STR = "0";

    public static final Integer ZERO = 0;

    public static final Integer LENGTH_5 = 5;

    public static final Integer LENGTH_10 = 10;

    public static final Integer LENGTH_11 = 11;

    public static final Integer LENGTH_20 = 20;

    public static final Integer LENGTH_15 = 15;

    public static final Integer LENGTH_30 = 30;

    public static final Integer LENGTH_50 = 50;

    public static final Integer LENGTH_150 = 150;

    public static final Long MB = 1024 * 1024L;

    public static final String SESSION_KEY = "session_key";

    //注册时图片验证码
    public static final String CHECK_CODE = "check_code";

    //图片验证码唯一标识
    public static final String CHECK_CODE_KEY = "check_code_key";

    /**
     * redis 系统设置
     */

    //验证码存储位置
    public static final String REDIS_KEY_CHECK_CODE = "easychat:checkcode:";

    //心跳存储位置
    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "easychat:ws:user:heartbeat:";

    //token存储位置
    public static final String REDIS_KEY_WS_TOKEN = "easychat:ws:token:";

    //userId存储位置
    public static final String REDIS_KEY_WS_TOKEN_USERID = "easychat:ws:token:userid:";

    //系统默认设置
    public static final String REDIS_KEY_SYS_SETTING = "easychat:syssetting:";

    /**
     * redis过期时间设置
     */
    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN = 60;

    public static final Integer REDIS_KEY_EXPIRES_FIVE_MIN = REDIS_KEY_EXPIRES_ONE_MIN * 5;

    public static final Integer REDIS_KEY_EXPIRES_TEN_MIN = REDIS_KEY_EXPIRES_ONE_MIN * 10;

    public static final Integer REDIS_KEY_EXPIRES_ONE_HOUR = REDIS_KEY_EXPIRES_ONE_MIN * 60;

    public static final Integer REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;


    public static final String ROBOT_UID = UserContactTypeEnum.USER.getPrefix() + "robot";


    public static final String FILE_FOLDER = "file/";

    public static final String AVATAR_FOLDER = "avatar/";

    public static final String IMAGE_SUFFIX = ".png";

    public static final String COVER_IMAGE_SUFFIX = "_cover.png";

    public static final String APPLY_INFO_TEMPLATE = "我是%s";

    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";
}
