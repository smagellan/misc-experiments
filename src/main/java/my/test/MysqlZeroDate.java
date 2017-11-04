package my.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MysqlZeroDate {
    public static void main(String[] args) throws SQLException {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
        DataSource ds = context.getBean("dataSource2", DataSource.class);
        try (Connection conn = ds.getConnection()) {
            //String query = "select zero_dt from (SELECT CAST('0000-00-00' AS DATE) zero_dt) t1";
            String query = "select zero_dt, int_field from (SELECT CAST('2000-01-01' AS DATE) zero_dt, NULL int_field) t1";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                try(ResultSet rs = stmt.executeQuery()){
                    while (rs.next()) {
                        Object obj = rs.getObject("zero_dt");
                        System.err.println("obj: " + obj + "; wasNull: " + rs.wasNull());

                        Integer intField = rs.getObject("int_field", Integer.class);
                        System.err.println("obj: " + intField + "; wasNull: " + rs.wasNull());
                    }
                }
            }
        }
    }
}
