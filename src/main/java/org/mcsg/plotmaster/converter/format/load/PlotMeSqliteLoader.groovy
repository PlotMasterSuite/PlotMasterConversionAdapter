package org.mcsg.plotmaster.converter.format.load

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import groovy.transform.TypeCheckingMode;

import java.lang.invoke.LambdaForm.Compiled;
import java.nio.ByteBuffer
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.mcsg.plotmaster.Plot
import org.mcsg.plotmaster.PlotMember;
import org.mcsg.plotmaster.Region;
import org.mcsg.plotmaster.converter.format.LoadFormat
import org.mcsg.plotmaster.converter.util.AbstractSqlFormat;
import org.mcsg.plotmaster.converter.util.DefaultSettings;

@CompileStatic
class PlotMeSqliteLoader extends AbstractSqlFormat implements LoadFormat {
	
	String world
	Map settings
	
	public void setup(String world, Map conf) {
		Class.forName("org.sqlite.JDBC")
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:sqlite:plots.db");
		config.setConnectionTestQuery("SELECT 1")
		
		
		super.setup(config, conf)
		
		this.world = world
		loadSettings()
	}
	
	public Map loadSettings() {
		settings = DefaultSettings.getWorld("world")
	}
	
	public Region nextRegion(int index) {
		return null;
	}
	
	
	@CompileStatic(TypeCheckingMode.SKIP)
	public List<Region> loadRegionsBulk(int index, int amount) {
		def list = new ArrayList<Region>()
		def sql = getSql()
		
		sql.eachRow("SELECT * FROM plotmePlots WHERE world=${world} LIMIT ${index},${amount}") {
			Region r = new Region(x: it.bottomX, z: it.bottomZ, w: settings.grid.width,
			h: settings.grid.height, world: world)
			
			Plot p = new Plot(region: r, x: it.bottomX, z: it.bottomZ, w: settings.grid.width,
			h: settings.grid.height, world: world, ownerName: it.owner)
			
			//wtf plotme lol
			byte[] uidbytes = it.ownerid
			if(uidbytes) {
				UUID uuid = fromBytes(uidbytes)
				p.setOwnerUUID(uuid.toString())
			}
			
			r.plots.put(p.id, p)
			
			
			list.add(r)
			index++
		}
	//	println "$index, $amount, ${list.size()}"
		
		sql.close()
		
		return list;
	}
	
	public static UUID fromBytes(byte[] array) {
		if (array.length != 16) {
			throw new IllegalArgumentException("Illegal byte array length: " + array.length);
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(array);
		long mostSignificant = byteBuffer.getLong();
		long leastSignificant = byteBuffer.getLong();
		return new UUID(mostSignificant, leastSignificant);
	}
	
	public PlotMember nextMember(int index) {
		return null;
	}
	
	public List<PlotMember> loadMembersBulk(int index, int amount) {
		def list = new ArrayList<Region>()
		def sql = getSql()
		
		sql.eachRow("SELECT * FROM plotmeAllowed INNER JOIN plotmeDenied LIMIT 5") {
			println it
		}
		
		System.exit(0)
		
	}
	
	public boolean supportsBulk() {
		return true;
	}
	
	@CompileStatic(TypeCheckingMode.SKIP)
	public int getAmountOfRegions() {
		return getSql().firstRow("SELECT COUNT(*) as amount FROM plotmePlots WHERE world=${world}").amount;
	}
	
	public int getAmountOfMembers() {
		return 0;
	}

	public void finish() {
		// TODO Auto-generated method stub
		
	}

	public boolean supportsThreading() {
		return true;
	}


	
}
