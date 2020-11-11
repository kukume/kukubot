package me.kuku.yuq.pojo;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum ResultStatus {
    SUCCESS(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    DYNAMIC_NOT_FOUNT(500, "动态没有找到"),
    QRCODE_IS_SCANNED(2, "二维码已被扫描"),
    QRCODE_NOT_SCANNED(1, "二维码未被扫描"),
    COOKIE_EXPIRED(500, "您的Cookie已失效"),
    ;

    private final Integer code;
    private final String message;

    ResultStatus(Integer code, String message){
        this.code = code;
        this.message = message;
    }
}
