package cn.sl.ehub.console.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageResultVO<T> {
    private List<T> list;
    private Integer total;
    private Integer pageIndex;
    private Integer pageSize;

    public PageResultVO(List<T> list) {
        this.list = list;
    }
}