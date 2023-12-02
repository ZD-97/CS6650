package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
public class DatabaseUtil {
  private static HikariConfig config = new HikariConfig();
  private static HikariDataSource ds;

  static {
    try {
      // Explicitly loading the MySQL JDBC driver class
      Class.forName("com.mysql.cj.jdbc.Driver");
// RDS connection
      config.setJdbcUrl("jdbc:mysql://database-4.c6adlmbkalml.us-west-2.rds.amazonaws.com:3306/mydb4?useSSL=false");
      config.setUsername("admin");
      config.setPassword("wasdzx123");

//      config.setJdbcUrl("jdbc:mysql://localhost:3306/AlbumApplication?useSSL=false");
//      config.setUsername("root");
//      config.setPassword("password");
      config.setMaximumPoolSize(290); // Set your desired pool size
      ds = new HikariDataSource(config);

    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Failed to load MySQL JDBC driver", e);
    }
  }

  private  DatabaseUtil() {}

  public static Connection getConnection() throws SQLException, ClassNotFoundException{
    return ds.getConnection();
  }

  public static void closeDataSource() {
    if (ds != null) {
      ds.close();
    }
  }
}

