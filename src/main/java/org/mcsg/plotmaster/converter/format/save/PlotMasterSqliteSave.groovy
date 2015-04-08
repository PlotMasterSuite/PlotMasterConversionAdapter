package org.mcsg.plotmaster.converter.format.save

import com.zaxxer.hikari.HikariConfig

class PlotMasterSqliteSave extends AbstractSqlSaver{
	
	public void setup(String world, Map conf) {
		Class.forName("org.sqlite.JDBC")
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:sqlite:plots_NEW.db");
		config.setConnectionTestQuery("SELECT 1")
		
		
		super.setup(config, conf)
		super.setup(world, conf)
		
		sql.execute("""
			CREATE TABLE IF NOT EXISTS `${regions}` (
				 `id` INTEGER PRIMARY KEY,
				 `name` TEXT NOT NULL DEFAULT '',
				 `world` TEXT NOT NULL,
				 `x` INTEGER NOT NULL,
				 `z` INTEGER NOT NULL,
				 `h` INTEGER NOT NULL,
				 `w` INTEGER NOT NULL,
				 `createdAt` INTEGER NOT NULL
			);
		""".toString())
		
		sql.execute("""
			CREATE TABLE IF NOT EXISTS `${plots}` (
				 `id` INTEGER PRIMARY KEY,
				 `region` INTEGER NOT NULL,
				 `world` TEXT NOT NULL DEFAULT '',
				 `name` TEXT NOT NULL DEFAULT '',
				 `owner` TEXT NOT NULL DEFAULT '',
				 `uuid` TEXT NOT NULL DEFAULT '',
				 `x` INTEGER NOT NULL,
				 `z` INTEGER NOT NULL,
				 `h` INTEGER NOT NULL,
				 `w` INTEGER NOT NULL,
				 `createdAt` bigint(32) NOT NULL,
				 `type` text DEFAULT '',
				 `accessmode` TEXT  DEFAULT 'ALLOW',
				 `settings` TEXT,
				 `metadata` TEXT
			);
		""".toString())
		
		sql.execute("""
			CREATE TABLE IF NOT EXISTS`${access_list}` (
				 `id` INTEGER PRIMARY KEY,
				 `uuid` TEXT,
				 `name` TEXT,
				 `type` TEXT,
				 `plot` INTEGER
			);
		""".toString())
		
		sql.commit()
		
		sql.close()
		
	}
	
	
	
	
	
	public void finish() {
		// TODO Auto-generated method stub
		
	}





	public boolean supportsThreading() {
		return false;
	}
	
}
