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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.mcsg.plotmaster.AccessLevel;
import org.mcsg.plotmaster.Plot
import org.mcsg.plotmaster.PlotMember;
import org.mcsg.plotmaster.Region;
import org.mcsg.plotmaster.converter.format.LoadFormat
import org.mcsg.plotmaster.converter.util.AbstractSqlFormat;
import org.mcsg.plotmaster.converter.util.DefaultSettings;

class PlotMeSqliteLoader extends AbstractSqlFormat implements LoadFormat {
	
	String world
	Map settings
	
	//for whatever reason, PlotMe doesn't update half the users plots
	//when converting to uuid's. We'll keep a map of players uuid's here
	//so we can complete that for plotme
	Map<String, String> uuidmap
	
	Map<String, Integer> xzMap = new ConcurrentHashMap<>()
	
	
	int allowed
	int denied
	int owners
	
	int bw
	int w
	
	public void setup(String world, Map conf) {
		Class.forName("org.sqlite.JDBC")
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:sqlite:plots.db");
		config.setConnectionTestQuery("SELECT 1")
		super.setup(config, conf)
		
		this.world = world
		loadSettings()
		
		uuidmap = new ConcurrentHashMap<>()
		
		bw = conf.border ?: 4
		w = conf.width ?: 99
		
		Sql sql = getSql()
		allowed = sql.firstRow("SELECT count(*) AS amount FROM plotmeAllowed WHERE world=${world}").amount
		denied = sql.firstRow("SELECT count(*) AS amount FROM plotmeDenied WHERE world=${world}").amount
		owners = sql.firstRow("SELECT COUNT(*) as amount FROM plotmePlots WHERE world=${world}").amount;
		sql.close()
	}
	
	public Map getSettings() {
		
	}
	
	public Map loadSettings() {
		settings = DefaultSettings.getWorld("world")
	}
	
	public Region nextRegion(int index) {
		return null;
	}
	
	AtomicLong regid = new AtomicLong(0)
	AtomicLong plotid = new AtomicLong(0)
	
	@CompileStatic(TypeCheckingMode.SKIP)
	public List<Region> loadRegionsBulk(int index, int amount) {
		def list = new ArrayList<Region>()
		def sql = getSql()
		
		sql.eachRow("SELECT * FROM plotmePlots WHERE world=${world} LIMIT ${index},${amount}") {
			Region r = new Region(id: regid.getAndIncrement(), x: it.bottomX, z: it.bottomZ, w: settings.grid.width,
			h: settings.grid.height, world: world)
			
			Plot p = new Plot(id: plotid.getAndIncrement(), region: r, x: it.bottomX, z: it.bottomZ, w: settings.grid.width,
			h: settings.grid.height, world: world, ownerName: it.owner)
			
			//wtf plotme lol
			byte[] uidbytes = it.ownerid
			if(uidbytes) {
				UUID uuid = fromBytes(uidbytes)
				p.setOwnerUUID(uuid.toString())
			}
			
			r.plots.put(p.id, p)
			xzMap.put("${it.idX}:${it.idZ}", p.getId())
			
			list.add(r)
			index++
		}
		
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
		def sql = getSql()
		
		def map = [:]
		
		int am = 0
		while(am < amount && am + index < getAmountOfMembers()) {
			if(index <= allowed) {
				//println "SELECT * FROM plotmeAllowed  WHERE world=${world} ORDER BY player LIMIT ${index},${amount - am}"
				sql.eachRow("SELECT * FROM plotmeAllowed  WHERE world=${world} ORDER BY player LIMIT ${index},${amount - am}") {
					if(it.player){
						PlotMember member = map.get(it.player) ?: new PlotMember(name: it.player,uuid: (it.playerid)? fromBytes(it.playerid) : "")
						if(xzMap.get("${it.idX}:${it.idZ}")) {
							member.getPlotAccessMap().put(xzMap.get("${it.idX}:${it.idZ}"), AccessLevel.MEMBER)
						}
						map.put(it.player, member)
						am++
					}
				}
			} else if(index > allowed && index < allowed + denied){
				index -= allowed
				//println "SELECT * FROM plotmeDenied WHERE world=${world}  ORDER BY player LIMIT ${index},${amount - am}"
				sql.eachRow("SELECT * FROM plotmeDenied WHERE world=${world}  ORDER BY player LIMIT ${index},${amount - am}") {
					if(it.player) {
						PlotMember member = map.get(it.player) ?: new PlotMember(name: it.player,  uuid: (it.playerid)? fromBytes(it.playerid) : "")
						if(xzMap.get("${it.idX}:${it.idZ}")) {
							member.getPlotAccessMap().put(xzMap.get("${it.idX}:${it.idZ}"), AccessLevel.DENY)
						}
						map.put(it.player, member)
						am++
					}
				}
			} else {
				index -= allowed + denied
				//println "SELECT * FROM plotmePlots WHERE world=${world} ORDER BY owner LIMIT ${index},${amount - am}"
				sql.eachRow("SELECT * FROM plotmePlots WHERE world=${world} ORDER BY owner LIMIT ${index},${amount - am}") {
					if(it.owner) {
						PlotMember member = map.get(it.owner) ?: new PlotMember(name: it.owner, uuid: (it.ownerid)? fromBytes(it.ownerid) : "")
						if(xzMap.get("${it.idX}:${it.idZ}")) {
							member.getPlotAccessMap().put(xzMap.get("${it.idX}:${it.idZ}"), AccessLevel.OWNER)
						}
						map.put(it.owner, member)
						am++
					}
				}
			}
		}
		
		if(am == 0)
			println "AM  IS 0, INDEX IS $index"
		
		sql.close()
		return new ArrayList(map.values())
		
	}
	
	public boolean supportsBulk() {
		return true;
	}
	
	@CompileStatic(TypeCheckingMode.SKIP)
	public int getAmountOfRegions() {
		return owners
	}
	
	public int getAmountOfMembers() {
		return allowed + denied + owners
	}
	
	public void finish() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean supportsPlotThreading() {
		return true;
	}
	
	
	public boolean supportsMemberThreading() {
		return true;
	}
	
	private int getRegionX(int x) {
		int ix = (x - bw) / (bw + bw + w - 1)
		
		return (ix * (bw + w)) + bw
	}
	
	private int getRegionZ(int z) {
		int iz = (z - bw) / (bw + bw + w - 1)
		
		return (iz * (bw + w)) + bw
	}
	
}
