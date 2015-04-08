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

import org.mcsg.plotmaster.Plot
import org.mcsg.plotmaster.PlotMember;
import org.mcsg.plotmaster.Region;
import org.mcsg.plotmaster.backend.flatfile.FlatFileBackend
import org.mcsg.plotmaster.backend.flatfile.FlatFileBackend.XZLoc;
import org.mcsg.plotmaster.converter.format.SaveFormat

@CompileStatic
class PlotMasterFlatFile implements SaveFormat{
	
	
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
	
	
	int regid = 0
	int plotid = 0
	
	public void saveRegion(int index, Region region) {
		if(region.id == -1)
			region.id = regid++
		
		
		region.plots.values().each { Plot plot ->
			if(plot.id == -1)
				plot.id = plotid++
			
			plotMap.put(plot.id, region.id)
		}
		
		regionMap.put(region.id, new XZLoc(x: region.x, z: region.z))
		
		def file = new File(regionFolder, "${region.x}.${region.z}.rg")
		
		file.createNewFile()
		file.setText(gson.toJson(region))
		
	}
	
	public void saveRegionsBulk(List<Region> regions) {
		
	}
	
	public void saveMember(int index, PlotMember member) {
		
		
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
	}
	
}
