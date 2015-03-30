package org.mcsg.plotmaster.converter

import org.mcsg.plotmaster.PlotMember
import org.mcsg.plotmaster.Region
import org.mcsg.plotmaster.converter.format.LoadFormat
import org.mcsg.plotmaster.converter.format.SaveFormat
import org.mcsg.plotmaster.converter.util.Progress

class Adapter {
	
	
	LoadFormat loader
	SaveFormat saver
	Map settings
	
	boolean loadBulk
	boolean saveBulk
	
	Progress prog = new Progress()
	
	public Adapter(LoadFormat loader, SaveFormat saver, Map settings){
		this.loader = loader
		this.saver = saver
		
		loadBulk = loader.supportsBulk()
		saveBulk = saver.supportsBulk()
	}
	
	
	public Progress beginConversion(){
		Thread.start {
			convertRegions()
			convertMembers()
		}
		
		return prog
	}
	
	
	private convertRegions() {
		prog.setMax(loader.getAmountOfRegions())
		prog.setMessage("Converting Plots...")
		
		def loadRegionGroup = {
			if(loadBulk){
				return loader.loadRegionsBulk(50)
			} else {
				List<Region> regions = []
				for(a in [0..50]) {
					Region r = loader.nextRegion()
					if(r){
						regions.add(r)
					} else {
						break;
					}
				}
				return regions
			}
		}
		
		def saveRegionGroup = { List<Region> regions ->
			if(saveBulk){
				saver.saveRegionsBulk(regions)
			} else {
				regions.each {
					saver.saveRegion(it)
				}
			}
		}
		
		
		List<Region> regions
		
		while((regions = loadRegionGroup())) {
			if(regions.size() == 0){
				break
			}
			saveRegionGroup(regions)
			prog.incProgress(regions.size())
		}
	}
	
	private convertMembers() {
		prog.setMax(loader.getAmountOfMembers())
		prog.setMessage("Converting Users...")
		
		def loadMemberGroup = {
			if(loadBulk){
				return loader.loadMembersBulk(50)
			} else {
				List<PlotMember> members = []
				for(a in [0..50]) {
					PlotMember r = loader.nextMember()
					if(r){
						members.add(r)
					} else {
						break;
					}
				}
				return members
			}
		}
		
		def saveMemberGroup = { List<PlotMember> members ->
			if(saveBulk){
				saver.saveMembersBulk(members)
			} else {
				members.each {
					saver.saveRegion(it)
				}
			}
		}
		
		
		List<Region> members
		
		while((members = loadMemberGroup())) {
			if(members.size() == 0){
				break
			}
			saveMemberGroup(members)
			prog.incProgress(members.size())
		}
	}
	
	
}
