package org.mcsg.plotmaster.converter.format.save

import com.zaxxer.hikari.HikariConfig

class PlotMasterMySqlSaver extends AbstractSqlSaver{
	
	public void setup(String world, Map conf) {
		HikariConfig config = new HikariConfig();
		
		config.setJdbcUrl("jdbc:mysql://localhost/PlotMaster");
		config.setUsername("root");
		config.setPassword("root");
		
		super.setup(config, conf)
		super.setup(world, conf)
		
		def sql = getSql();
		
		sql.execute("""
			CREATE TABLE IF NOT EXISTS `${regions}` (
				 `id` int(11) NOT NULL AUTO_INCREMENT,
				 `name` varchar(128) NOT NULL DEFAULT '',
				 `world` varchar(64) NOT NULL,
				 `x` int(11) NOT NULL,
				 `z` int(11) NOT NULL,
				 `h` int(11) NOT NULL,
				 `w` int(11) NOT NULL,
				 `createdAt` bigint(32) NOT NULL,
				 PRIMARY KEY (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=latin1 ;
		""".toString())
		
		sql.execute("""
			CREATE TABLE IF NOT EXISTS `${plots}` (
				 `id` int(11) NOT NULL AUTO_INCREMENT,
				 `region` int(11) NOT NULL,
				 `world` varchar(64) NOT NULL DEFAULT '',
				 `name` varchar(64) NOT NULL DEFAULT '',
				 `owner` varchar(16) NOT NULL DEFAULT '',
				 `uuid` varchar(36) NOT NULL DEFAULT '',
				 `x` int(11) NOT NULL,
				 `z` int(11) NOT NULL,
				 `h` int(11) NOT NULL,
				 `w` int(11) NOT NULL,
				 `createdAt` bigint(32) NOT NULL,
				 `type` varchar(32)  DEFAULT '',
				 `accessmode` enum('ALLOW', 'DENY') NOT NULL DEFAULT 'ALLOW',
				 `settings` TEXT,
				 `metadata` TEXT,
				  PRIMARY KEY (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=latin1 ;
		""".toString())
		
		sql.execute("""
			CREATE TABLE IF NOT EXISTS`${access_list}` (
				 `id` int(11) NOT NULL AUTO_INCREMENT,
				 `uuid` varchar(36) NOT NULL,
				 `name` varchar(16) ,
				 `type` enum('OWNER', 'ADMIN', 'MEMBER', 'ALLOW', 'DENY') NOT NULL,
				 `plot` int(11) NOT NULL,
				 PRIMARY KEY (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=latin1
		""".toString())
		
		
		sql.close()
	}
	
	
	
	public void finish() {
		
		
	}



	public boolean supportsThreading() {
		return true;
	}
	
}
