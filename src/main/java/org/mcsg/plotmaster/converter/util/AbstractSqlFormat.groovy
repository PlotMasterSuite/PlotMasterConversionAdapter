package org.mcsg.plotmaster.converter.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import groovy.sql.Sql
import javax.sql.DataSource

class AbstractSqlFormat {
	
	DataSource ds
	
	void setup(HikariConfig config, Map conf) {
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("useServerPrepStmts", "true");
		ds = new HikariDataSource(config);
	}
	
	Sql getSql() {
		return new Sql(ds)
	}
	
	
}
