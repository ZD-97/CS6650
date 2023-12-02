package org.example;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Close HikariCP DataSource
    DatabaseUtil.closeDataSource();
  }
}

