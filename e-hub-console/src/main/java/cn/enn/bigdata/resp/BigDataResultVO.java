package cn.enn.bigdata.resp;

import lombok.Data;

@Data
public class BigDataResultVO<T> {
    private Integer code;
    private String message;
    private T data;
}
