package top.enderherman.easychat.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Enderherman
 * @date 2024/12/24
 * 通用返回类
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> implements Serializable {
    private String status;
    private Integer code;
    private String message;
    private T data;

    public static <T> BaseResponse<T> success() {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.status = "success";
        baseResponse.code = 200;
        return baseResponse;
    }

    public static <T> BaseResponse<T> success(String message) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.status = message.isEmpty() ? "success" : message;
        baseResponse.code = 200;
        return baseResponse;
    }

    public static <T> BaseResponse<T> success(T object) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.status = "success";
        baseResponse.data = object;
        baseResponse.code = 200;
        return baseResponse;
    }

    public static <T> BaseResponse<T> error(String message) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.message = message;
        baseResponse.code = 500;
        return baseResponse;
    }


}
