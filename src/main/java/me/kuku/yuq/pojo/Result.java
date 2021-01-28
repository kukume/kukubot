package me.kuku.yuq.pojo;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Result<T> {
    private final Integer code;
    private final String message;
    private final T data;

    private Result(ResultStatus resultStatus, T data){
        this.code = resultStatus.getCode();
        this.message = resultStatus.getMessage();
        this.data = data;
    }

    private Result(Integer code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess(){
        return this.code == 200;
    }

    public boolean isFailure(){
        return this.code != 200;
    }

    public static Result<Void> success(){
        return new Result<>(ResultStatus.SUCCESS, null);
    }

    public static <T> Result<T> success(T data){
        return new Result<>(ResultStatus.SUCCESS, data);
    }

    public static <T> Result<T> success(ResultStatus resultStatus, T data){
        if (resultStatus == null){
            return success(data);
        }
        return new Result<>(resultStatus, data);
    }

    public static <T> Result<T> success(String message, T data){
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> failure(){
        return new Result<>(ResultStatus.INTERNAL_SERVER_ERROR, null);
    }

    public static <T> Result<T> failure(T data){
        return failure(ResultStatus.INTERNAL_SERVER_ERROR, data);
    }

    public static <T> Result<T> failure(String msg){
        return failure(msg, null);
    }

    public static <T> Result<T> failure(ResultStatus resultStatus){
        return failure(resultStatus, null);
    }

    public static <T> Result<T> failure(ResultStatus resultStatus, T data){
        if (resultStatus == null){
            return new Result<>(ResultStatus.INTERNAL_SERVER_ERROR, null);
        }
        return new Result<>(resultStatus, data);
    }

    public static <T> Result<T> failure(String message, T data){
        return new Result<>(500, message, data);
    }

    public static <T> Result<T> failure(Integer code, String message){
        return new Result<>(code, message, null);
    }
    public static <T> Result<T> failure(Integer code, String message, T data){
        return new Result<>(code, message, data);
    }
}
