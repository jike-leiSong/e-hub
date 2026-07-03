import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DbProbe {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://82.156.97.190:3306/e_hub?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "ehub666$");
             Statement stmt = conn.createStatement()) {
            String[] sqls = {
                    "select count(*) cnt from e_agent_price where del_flag='0' and province_code='440000000000' and second_type='东西两翼地区' and third_type='不限' and dy_level='1-10(20)千伏' and user_type='工商业用电' and other_type='两部制' and version='2606'",
                    "select price_type,count(*) cnt from e_agent_price where del_flag='0' and province_code='440000000000' and second_type='东西两翼地区' and third_type='不限' and dy_level='1-10(20)千伏' and user_type='工商业用电' and other_type='两部制' and version='2606' group by price_type",
                    "select count(*) cnt from e_agent_price p join e_agent_price_data d on p.id=d.en_agent_price_id where p.del_flag='0' and d.del_flag='0' and p.province_code='440000000000' and p.second_type='东西两翼地区' and p.third_type='不限' and p.dy_level='1-10(20)千伏' and p.user_type='工商业用电' and p.other_type='两部制' and p.version='2606'",
                    "select count(*) cnt from e_fpgj_type where del_flag='0' and province_code='440000000000' and second_type='东西两翼地区' and version='2606'",
                    "select count(*) cnt from e_fpgj_type_data d join e_fpgj_type t on t.id=d.en_fpgj_type_id where t.del_flag='0' and d.del_flag='0' and t.province_code='440000000000' and t.second_type='东西两翼地区' and t.version='2606'",
                    \"select second_type,third_type,dy_level,user_type,other_type,count(*) cnt,group_concat(price_type order by price_type separator '|') price_types from e_agent_price where del_flag='0' and province_code='440000000000' and version='2606' group by second_type,third_type,dy_level,user_type,other_type having count(*) >= 3 order by cnt desc limit 20\",
                    \"select second_type,count(*) cnt from e_fpgj_type where del_flag='0' and province_code='440000000000' and version='2606' group by second_type\",
                    \"select t.second_type,count(*) cnt from e_fpgj_type_data d join e_fpgj_type t on t.id=d.en_fpgj_type_id where t.del_flag='0' and d.del_flag='0' and t.province_code='440000000000' and t.version='2606' group by t.second_type\"
            };
            for (String sql : sqls) {
                System.out.println("SQL> " + sql);
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    ResultSetMetaData md = rs.getMetaData();
                    while (rs.next()) {
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            System.out.print(md.getColumnLabel(i) + "=" + rs.getString(i) + " ");
                        }
                        System.out.println();
                    }
                } catch (Exception ex) {
                    System.out.println("ERR=" + ex.getMessage());
                }
            }
        }
    }
}
