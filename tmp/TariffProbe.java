import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class TariffProbe {
    private static final String URL = "jdbc:mysql://82.156.97.190:3306/e_hub"
            + "?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(URL, "root", "ehub666$");
             Statement stmt = conn.createStatement()) {
            String[] sqls = {
                    "select version,count(*) cnt from e_agent_price where del_flag='0' group by version order by version desc limit 20",
                    "select version,count(*) cnt from e_fpgj_type where del_flag='0' group by version order by version desc limit 20",
                    "select province_code,province_name,second_type,third_type,count(*) cnt from e_agent_price where del_flag='0' and version in ('2606','202606','2026-06','260601','2026-06-01') group by province_code,province_name,second_type,third_type order by cnt desc limit 30",
                    "select version,province_code,province_name,second_type,third_type,dy_level,user_type,other_type,count(*) cnt,group_concat(distinct price_type order by price_type separator '|') price_types from e_agent_price where del_flag='0' and province_code='110000000000' and version in ('2606','202606','2026-06','260601','2026-06-01') group by version,province_code,province_name,second_type,third_type,dy_level,user_type,other_type order by cnt desc limit 30",
                    "select version,price_type,count(*) cnt from e_agent_price where del_flag='0' and province_code='110000000000' and second_type='全域' and third_type='不限' and dy_level='1-10千伏' and user_type='工商业用电' and other_type='两部制' and version in ('2606','202606','2026-06','260601','2026-06-01') group by version,price_type order by version,price_type",
                    "select p.version,p.price_type,count(d.id) data_cnt,min(d.biz_time) min_time,max(d.biz_time) max_time from e_agent_price p left join e_agent_price_data d on p.id=d.en_agent_price_id and d.del_flag='0' where p.del_flag='0' and p.province_code='110000000000' and p.second_type='全域' and p.third_type='不限' and p.dy_level='1-10千伏' and p.user_type='工商业用电' and p.other_type='两部制' and p.version in ('2606','202606','2026-06','260601','2026-06-01') group by p.version,p.price_type order by p.version,p.price_type",
                    "select count(*) parent_cnt from e_agent_price where del_flag='0' and version='2606'",
                    "select count(*) detail_cnt from e_agent_price_data where del_flag='0'",
                    "select p.version,p.price_type,count(distinct p.id) parent_cnt,count(d.id) detail_cnt from e_agent_price p left join e_agent_price_data d on p.id=d.en_agent_price_id and d.del_flag='0' where p.del_flag='0' and p.version='2606' group by p.version,p.price_type order by p.price_type",
                    "select count(*) detail_cnt,count(distinct en_agent_price_id) parent_id_cnt from e_agent_price_data where del_flag='0'",
                    "select d.en_agent_price_id,count(*) cnt,min(d.biz_time) min_time,max(d.biz_time) max_time from e_agent_price_data d where d.del_flag='0' group by d.en_agent_price_id order by cnt desc limit 20",
                    "select p.version,p.province_code,p.province_name,p.second_type,p.third_type,p.dy_level,p.user_type,p.other_type,p.price_type,count(d.id) detail_cnt from e_agent_price_data d left join e_agent_price p on p.id=d.en_agent_price_id where d.del_flag='0' group by p.version,p.province_code,p.province_name,p.second_type,p.third_type,p.dy_level,p.user_type,p.other_type,p.price_type order by detail_cnt desc limit 30",
                    "select count(*) orphan_detail_cnt from e_agent_price_data d left join e_agent_price p on p.id=d.en_agent_price_id where d.del_flag='0' and p.id is null",
                    "select p.province_code,p.province_name,p.second_type,p.third_type,count(distinct p.id) parent_cnt,count(d.id) detail_cnt from e_agent_price p left join e_agent_price_data d on p.id=d.en_agent_price_id and d.del_flag='0' where p.del_flag='0' and p.version='2606' group by p.province_code,p.province_name,p.second_type,p.third_type order by detail_cnt desc,parent_cnt desc limit 30",
                    "select id,version,province_code,province_name,second_type,third_type,dy_level,user_type,other_type,price_type,capacity_electricity_price,demand_electricity_price from e_agent_price where del_flag='0' and province_code='110000000000' and second_type='全域' and third_type='不限' and dy_level='1-10千伏' and user_type='工商业用电' and other_type='两部制' and version='2606'",
                    "select d.en_agent_price_id,count(*) detail_cnt,min(d.biz_time) min_time,max(d.biz_time) max_time from e_agent_price_data d where d.en_agent_price_id in (select id from e_agent_price where del_flag='0' and province_code='110000000000' and second_type='全域' and third_type='不限' and dy_level='1-10千伏' and user_type='工商业用电' and other_type='两部制' and version='2606') group by d.en_agent_price_id",
                    "select t.version,t.province_code,t.province_name,t.second_type,count(d.id) data_cnt,min(d.biz_time) min_time,max(d.biz_time) max_time,group_concat(distinct d.fpgj_type order by d.fpgj_type separator '|') fpgj_types from e_fpgj_type t left join e_fpgj_type_data d on t.id=d.en_fpgj_type_id and d.del_flag='0' where t.del_flag='0' and t.province_code='110000000000' and t.version in ('2606','202606','2026-06','260601','2026-06-01') group by t.version,t.province_code,t.province_name,t.second_type order by t.version,t.second_type",
                    "select version,second_type,count(*) cnt from e_fpgj_type where del_flag='0' and province_code='110000000000' and version in ('2606','202606','2026-06','260601','2026-06-01') group by version,second_type order by version,second_type",
                    "select table_name from information_schema.tables where table_schema = database() and table_name in ('en_agent_price','en_agent_price_data','e_agent_price','e_agent_price_data') order by table_name",
                    "select count(*) src_parent_cnt from en_agent_price",
                    "select count(*) src_detail_cnt from en_agent_price_data",
                    "select count(*) matched_src_parent_cnt from e_agent_price_data d join en_agent_price p on p.id = d.en_agent_price_id where d.del_flag='0'",
                    "select p.id,p.version,p.province_code,p.province_name,p.second_type,p.third_type,p.dy_level,p.user_type,p.other_type,p.price_type,count(d.id) detail_cnt from e_agent_price_data d join en_agent_price p on p.id=d.en_agent_price_id where d.del_flag='0' group by p.id,p.version,p.province_code,p.province_name,p.second_type,p.third_type,p.dy_level,p.user_type,p.other_type,p.price_type order by detail_cnt desc limit 20",
                    "select count(*) remappable_detail_cnt from e_agent_price_data d join en_agent_price oldp on oldp.id=d.en_agent_price_id join e_agent_price newp on newp.version=oldp.version and newp.province_code=oldp.province_code and newp.second_type=oldp.second_type and newp.third_type=oldp.third_type and newp.dy_level=oldp.dy_level and newp.user_type=oldp.user_type and newp.other_type=oldp.other_type and newp.price_type=oldp.price_type and newp.del_flag=oldp.del_flag where d.del_flag='0'",
                    "select oldp.id old_id,newp.id new_id,oldp.version,oldp.province_code,oldp.second_type,oldp.third_type,oldp.dy_level,oldp.user_type,oldp.other_type,oldp.price_type,count(d.id) detail_cnt from e_agent_price_data d join en_agent_price oldp on oldp.id=d.en_agent_price_id join e_agent_price newp on newp.version=oldp.version and newp.province_code=oldp.province_code and newp.second_type=oldp.second_type and newp.third_type=oldp.third_type and newp.dy_level=oldp.dy_level and newp.user_type=oldp.user_type and newp.other_type=oldp.other_type and newp.price_type=oldp.price_type and newp.del_flag=oldp.del_flag where d.del_flag='0' group by oldp.id,newp.id,oldp.version,oldp.province_code,oldp.second_type,oldp.third_type,oldp.dy_level,oldp.user_type,oldp.other_type,oldp.price_type order by detail_cnt desc limit 20"
            };
            for (String sql : sqls) {
                System.out.println("SQL> " + sql);
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    print(rs);
                } catch (Exception ex) {
                    System.out.println("ERR=" + ex.getMessage());
                }
            }
        }
    }

    private static void print(ResultSet rs) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        int columns = meta.getColumnCount();
        int rows = 0;
        while (rs.next()) {
            rows++;
            StringBuilder line = new StringBuilder();
            for (int i = 1; i <= columns; i++) {
                if (i > 1) {
                    line.append(" | ");
                }
                line.append(meta.getColumnLabel(i)).append("=").append(rs.getString(i));
            }
            System.out.println(line);
        }
        if (rows == 0) {
            System.out.println("(no rows)");
        }
    }
}
