package cn.sl.ehub.upstream.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "declareBid", propOrder = {
        "cmdData"
})
public class DeclareBid {

    protected String cmdData;

    /**
     * 获取cmdData属性的值。
     *
     * @return
     *         possible object is
     *         {@link String }
     *
     */
    public String getCmdData() {
        return cmdData;
    }

    /**
     * 设置cmdData属性的值。
     *
     * @param value
     *              allowed object is
     *              {@link String }
     *
     */
    public void setCmdData(String value) {
        this.cmdData = value;
    }

}
