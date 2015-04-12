package org.mcsg.plotmaster.converter

import groovy.transform.CompileStatic;

import org.mcsg.plotmaster.PlotMember
import org.mcsg.plotmaster.Region
import org.mcsg.plotmaster.converter.format.LoadFormat
import org.mcsg.plotmaster.converter.format.SaveFormat
import org.mcsg.plotmaster.converter.util.Progress

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;


class ConversionAdapter {
	
	private static final int THREADS = 4
	
	LoadFormat loader
	SaveFormat saver
	Map settings
	
	boolean loadBulk
	boolean saveBulk
	
	Progress prog = new Progress()
	
	
	EventBus eventbus = new EventBus()
	
	
	static class ConverterFinishedEvent{
		int id
		int index
	}
	
	public ConversionAdapter(LoadFormat loader, SaveFormat saver, Map settings){
		this.loader = loader
		this.saver = saver
		
		loader.setup("creative", settings.load)
		saver.setup("world", settings.save)
		
		loadBulk = loader.supportsBulk()
		saveBulk = saver.supportsBulk()
		
		
		eventbus.register(this)
	}
	
	
	public Progress beginConversion(){
		
		int amount = loader.getAmountOfRegions()
		int per = amount / THREADS
		int index = 0
		
		prog.setMax(amount)
		
		println "beginning PlotMaster plot conversion"
		
		println "Loader: ${loader.getClass().getSimpleName()}"
		println "Saver: ${saver.getClass().getSimpleName()}"
		
		println "Converting ${amount} plots..."
		
		if(loader.supportsThreading() && saver.supportsThreading()) {
			for(int i = 0; i < THREADS ; i++) {
				new ConverterThread(i, index, per, amount).start()
				index += per
			}
		} else {
			new ConverterThread(1, 1, amount, amount).start()
		}
		return prog
	}
	
	
	class ConverterThread extends Thread {
		int i
		int index
		int per
		int amount
		
		public ConverterThread(int i, int index, int per, int amount) {
			this.i = i
			this.index = index
			this.amount = amount
			this.per = per
		}
		
		public void run() {
			if(i == THREADS - 1)
				convertRegions(index, amount - index + 1)
			else
				convertRegions(index, per - 1)
			
			convertMembers()
			
			
			eventbus.post(new ConverterFinishedEvent(id: i, index: index))
		}
		
	}
	
	int tdone = 0
	@Subscribe
	void listenForFinish(ConverterFinishedEvent e) {
		//println "Thread ${e.getId()} complete, index ${e.index}"
		
		tdone++
		if(tdone == THREADS) {
			loader.finish()
			saver.finish()
			
			prog.finish()
		}
	}
	
	protected convertRegions(int index, int amount) {
		int am = 50
		int till = index + amount
		
		//println "$index, $amount, $am, $till"
		
		def loadRegionGroup = {
			if(loadBulk){
				return loader.loadRegionsBulk(index, (am < till)? am : am - (am - till))
			} else {
				List<Region> regions = []
				for(a in [0..(am < till)? am : am - (am - till)]) {
					Region r = loader.nextRegion(index + a)
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
				saver.saveRegionsBulk(index, regions)
			} else {
				regions.eachWithIndex { reg, i ->
					saver.saveRegion(index + i, reg)
				}
			}
		}
		
		List<Region> regions
		
		while(index < till) {
			regions = loadRegionGroup()
			if(regions.size() == 0){
				break
			}
			saveRegionGroup(regions)
			prog.incProgress(regions.size())
			index += am
		}
	}
	
	private convertMembers(int index, int amount) {
		int am = 50
		int till = index + amount
		
		def loadMemberGroup = {
			if(loadBulk){
				return loader.loadMembersBulk(index, (am < till)? am : am - (am - till))
			} else {
				List<PlotMember> members = []
				for(a in [0..(am < till)? am : am - (am - till)]) {
					PlotMember r = loader.nextMember(index + a)
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
				saver.saveMembersBulk(index, members)
			} else {
				members.eachWithIndex { member, i ->
					saver.saveMember(index + i, member)
				}
			}
		}
		
		
		List<Region> members
		
		while(index < till) {
			members = loadMemberGroup()
			if(members.size() == 0){
				break
			}
			saveMemberGroup(members)
			prog.incProgress(members.size())
		}
	}
	
	
}
