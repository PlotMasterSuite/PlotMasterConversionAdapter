package org.mcsg.plotmaster.converter.format.save

import com.google.gson.Gson

import groovy.sql.Sql;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.mcsg.plotmaster.Plot
import org.mcsg.plotmaster.PlotMember;
import org.mcsg.plotmaster.Region;
import org.mcsg.plotmaster.PlotMember.PlotInfo
import org.mcsg.plotmaster.converter.format.SaveFormat
import org.mcsg.plotmaster.converter.util.AbstractSqlFormat

abstract class AbstractSqlSaver extends AbstractSqlFormat implements SaveFormat {
	
	
	String world;
	
	def regions, plots, access_list
	
	public void setup(String world, Map settings){
		this.world = world;
		
		regions = "${world}_regions"
		plots  = "${world}_plots"
		access_list = "${world}_access_list"
		
	}
	
	public void saveRegion(int index, Region region) {
		return
	}
	
	AtomicLong regid = new AtomicLong(0)
	AtomicLong plotid = new AtomicLong(0)
	
	public void saveRegionsBulk(int index, List<Region> regions) {
		Sql sql = getSql()
		
		sql.withTransaction {
			
			sql.withBatch("INSERT INTO ${this.regions} (id, name, world, x, z, h, w, createdAt) VALUES(?, ?, ?, ?, ?, ?, ?, ?)".toString()){ ps ->
				regions.each {
					it.id = regid.incrementAndGet()
					ps.addBatch([it.id, it.name, it.world, it.x, it.z, it.h, it.w, it.createdAt])
				}
			}
			
			sql.commit()
			
			
			sql.withBatch("""INSERT INTO ${plots} (id, region, world, name, owner, uuid, x, z, h, w, createdAt, type, accessmode, settings, metadata)
			VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""".toString()) { ps ->
						
						regions.each{ Region region ->
							region.plots.values().each { Plot plot ->
								plot.id = plotid.incrementAndGet()
								ps.addBatch([plot.id, region.id, region.world, plot.plotName, plot.ownerName, plot.ownerUUID, plot.x, plot.z,
									plot.h, plot.w, plot.createdAt, plot.type, plot.accessMode.toString(), plot.settingsToJson(), plot.metadataToJson()])
							}
						}
					}
		}
		sql.close()
	}
	
	public void saveMember(int index, PlotMember member) {
		return
	}
	
	public void saveMembersBulk(int index, List<PlotMember> members) {
		Sql sql = getSql()
		
		sql.withTransaction {
			sql.withBatch("INSERT INTO ${access_list} (id, uuid, name, level, plot)  VALUES(NULL, ?, ?,?,?)"){ ps ->
				members.each { mem ->
					mem.getPlotAccessMap().each { key, List<PlotInfo>val ->
						val.each{
							ps.addBatch([mem.uuid, mem.name, key, it.getId()])
						}
					}
				}
			}
			
		}
	}
	
	public boolean supportsBulk() {
		return true;
	}
	
	
	public void finish() {
		
	}
	
}
