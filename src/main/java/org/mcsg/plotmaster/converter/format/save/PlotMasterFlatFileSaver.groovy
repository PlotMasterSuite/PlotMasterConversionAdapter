package org.mcsg.plotmaster.converter.format.save

import com.google.gson.Gson

import groovy.transform.CompileStatic;

import com.google.gson.Gson
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong;

import org.mcsg.plotmaster.Plot
import org.mcsg.plotmaster.Plot.AccessMode;
import org.mcsg.plotmaster.AccessLevel
import org.mcsg.plotmaster.PlotMember;
import org.mcsg.plotmaster.Region;
import org.mcsg.plotmaster.backend.flatfile.FlatFileBackend
import org.mcsg.plotmaster.backend.flatfile.FlatFileBackend.XZLoc;
import org.mcsg.plotmaster.converter.format.SaveFormat

@CompileStatic
class PlotMasterFlatFileSaver implements SaveFormat{
	
	
	File folder
	File regionMapFile
	File plotMapFile
	File regionFolder
	File userFolder
	
	
	Map<Integer, XZLoc> regionMap
	Map<Integer, Integer> plotMap
	
	Gson gson
	
	static class XZLoc {
		int x, z
	}
	
	public void setup(String world, Map settings) {
		if(settings.debug) {
			gson = new GsonBuilder().setPrettyPrinting().create()
		} else {
			gson = new Gson()
		}
		
		File loc = new File(settings.location.toString());
		loc.mkdirs()
		
		folder = new File(loc, world)
		folder.mkdirs()
		
		regionFolder = new File(folder, "regions/")
		regionFolder.mkdirs()
		
		userFolder = new File(folder, "users/")
		userFolder.mkdirs()
		
		regionMapFile = new File(folder, "regionmap.json")
		regionMapFile.createNewFile()
		
		
		regionMap = new ConcurrentHashMap<>()
		
		plotMapFile = new File(folder, "plotmap.json")
		plotMapFile.createNewFile()
		
		plotMap = new ConcurrentHashMap<>()
	}
	
	
	
	
	public void saveRegion(int index, Region region) {
		region.plots.values().each { Plot plot ->
			plot.accessMap.put(plot.ownerUUID, AccessLevel.OWNER)
			plotMap.put(plot.id, region.id)
		}
		regionMap.put(region.id, new XZLoc(x: region.x, z: region.z))
		
		def file = new File(regionFolder, "${region.x}.${region.z}.rg")
		file.createNewFile()
		file.setText(gson.toJson(region))
		
	}
	
	public void saveRegionsBulk(List<Region> regions) {
		
	}
	
	
	Map<String, PlotMember> memberCache = new ConcurrentHashMap<>()
	public void saveMember(int index, PlotMember member) {		
		if(memberCache.containsKey(member.uuid)) {
			PlotMember existing = memberCache.get(member.uuid)
			existing.getPlotAccessMap().putAll(member.getPlotAccessMap())
			member = existing
		}

		memberCache.put(member.uuid, member)
	}
	
	public void saveMembersBulk(List<PlotMember> members) {
		
	}
	
	public boolean supportsBulk() {
		return false;
	}
	
	public void saveRegionsBulk(int index, List<Region> regions) {
		// TODO Auto-generated method stub
		
	}
	
	public void saveMembersBulk(int index, List<PlotMember> members) {
		// TODO Auto-generated method stub
		
	}
	
	public void finish() {
		regionMapFile.setText(gson.toJson(regionMap))
		plotMapFile.setText(gson.toJson(plotMap))
		
		memberCache.each {key, val ->
			def file = new File(userFolder, key+".json")
			def json = gson.toJson(val)
			file.setText(json)
		}
	}
	
	public boolean supportsPlotThreading() {
		return true;
	}
	
	public boolean supportsMemberThreading() {
		return true
	}




	public Map getSettings() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
