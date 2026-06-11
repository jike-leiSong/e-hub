package cn.enn.iot.vo;

import lombok.Data;

@Data
public class IotResultVo<T> {
    private Integer code;
    private String message;
    private T data;
}
