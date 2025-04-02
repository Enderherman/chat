package top.enderherman.easychat.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import top.enderherman.easychat.constants.Constants;
import top.enderherman.easychat.entity.enums.UserContactTypeEnum;
import top.enderherman.easychat.exception.BusinessException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class StringUtils {

    public static void checkParam(Object param) {
        try {
            Field[] fields = param.getClass().getDeclaredFields();
            boolean notEmpty = false;
            for (Field field : fields) {
                String methodName = "get" + StringUtils.upperCaseFirstLetter(field.getName());
                Method method = param.getClass().getMethod(methodName);
                Object object = method.invoke(param);
                if (object != null && object instanceof java.lang.String && !StringUtils.isEmpty(object.toString())
                        || object != null && !(object instanceof java.lang.String)) {
                    notEmpty = true;
                    break;
                }
            }
            if (!notEmpty) {
                throw new BusinessException("多参数更新，删除，必须有非空条件");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("校验参数是否为空失败");
        }
    }

    public static String upperCaseFirstLetter(String field) {
        if (isEmpty(field)) {
            return field;
        }
        //如果第二个字母是大写，第一个字母不大写
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else return "".equals(str.trim());
    }

    /**
     * 生成用户id
     */
    public static String getUserId() {
        return UserContactTypeEnum.USER.getPrefix() + getRandomNumber(Constants.LENGTH_11);
    }

    /**
     * 生成群组id
     */
    public static String getGroupId() {
        return UserContactTypeEnum.GROUP.getPrefix() + StringUtils.getRandomNumber(Constants.LENGTH_11);
    }

    /**
     * 生成随机数
     *
     * @param length 长度
     * @return 随机数
     */
    public static String getRandomNumber(Integer length) {
        return RandomStringUtils.random(length, false, true);
    }

    /**
     * 生成随机码
     *
     * @param length 长度
     * @return 随机码
     */
    public static String getRandomString(Integer length) {
        return RandomStringUtils.random(length, true, true);
    }


    /**
     * md5编码
     *
     * @param originString 密码
     * @return 编码后的密码
     */
    public static String encodingByMd5(String originString) {
        return isEmpty(originString) ? null : DigestUtils.md5Hex(originString);
    }
}
