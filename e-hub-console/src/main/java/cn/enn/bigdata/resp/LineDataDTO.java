package cn.enn.bigdata.resp;

import lombok.Data;

import java.util.List;

@Data
public class LineDataDTO {
    private String time;
    private List<Double> values;
}
