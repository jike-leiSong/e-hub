package cn.sl.ehub.service.vo;

import javax.persistence.*;

@Table(name = "plan_delivery_on_off")
public class PlanDeliveryOnOff {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 开关标记-1开启 0关闭
     */
    @Column(name = "mark")
    private Boolean mark;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取开关标记-1开启 0关闭
     *
     * @return mark - 开关标记-1开启 0关闭
     */
    public Boolean getMark() {
        return mark;
    }

    /**
     * 设置开关标记-1开启 0关闭
     *
     * @param mark 开关标记-1开启 0关闭
     */
    public void setMark(Boolean mark) {
        this.mark = mark;
    }
}