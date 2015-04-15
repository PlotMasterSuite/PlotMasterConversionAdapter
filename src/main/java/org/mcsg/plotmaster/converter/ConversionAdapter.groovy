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
	
	private static int THREADS = 8
	private static boolean thread = true
	LoadFormat loader
	SaveFormat saver
	Map settings
	
	boolean loadBulk
	boolean saveBulk
	
	Progress prog = new Progress()
	EventBus eventbus = new EventBus()
	
	Object wait = new Object()
	
	
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
	
	int plotAmount, plotPer, plotIndex,
	memberAmount, memberPer, memberIndex
	public Progress beginConversion(){
		
		plotAmount = loader.getAmountOfRegions()
		plotPer = plotAmount / THREADS
		plotIndex = 0
		
		memberAmount = loader.getAmountOfMembers()
		memberPer = memberAmount / THREADS
		memberIndex = 0
		
		prog.setMax(plotAmount)
		
		println "beginning PlotMaster plot conversion"
		
		println "Loader: ${loader.getClass().getSimpleName()}"
		println "Saver: ${saver.getClass().getSimpleName()}"
		
		println "Converting ${plotAmount} plots and $memberAmount members.."
		
		Thread.start{
			if(loader.supportsPlotThreading() && saver.supportsPlotThreading() && thread) {
				for(int i = 0; i < THREADS ; i++) {
					new ConverterThread(i, plotIndex, false).start()
					plotIndex += plotPer
				}
			} else {
				THREADS = 1
				new ConverterThread(1, 1, false).start()
			}
			
			synchronized (wait) {
				wait.wait()
			}
			
			if(loader.supportsMemberThreading() && saver.supportsMemberThreading() && thread) {
				for(int i = 0; i < THREADS ; i++) {
					new ConverterThread(i, memberIndex, true).start()
					memberIndex += memberPer
				}
			} else {
				THREADS = 1
				new ConverterThread(1, 1, true).start()
			}
			
			loader.finish()
			saver.finish()
			prog.finish()
		}
		return prog
	}
	
	
	class ConverterThread extends Thread {
		int i
		int index
		boolean members
		
		public ConverterThread(int i, int index, boolean members) {
			this.i = i
			this.index = index
			this.members = members
		}
		
		public void run() {
			if(i == THREADS - 1) {
				if(!members)
					convertRegions(index, plotAmount - index + 1)
				else
					convertMembers(index, memberAmount - index + 1)
			}
			else {
				if(!members)
					convertRegions(index, plotPer - 1)
				else
					convertMembers(index, memberPer - 1)
			}
			
			
			
			eventbus.post(new ConverterFinishedEvent(id: i, index: index))
		}
		
	}
	
	int tdone = 0
	@Subscribe
	void listenForFinish(ConverterFinishedEvent e) {
		//println "Thread ${e.getId()} complete, index ${e.index}"
		
		tdone++
		if(tdone == THREADS) {
			wait.notify()
		}
	}
	
	protected convertRegions(int index, int amount) {
		prog.setMessage("Converting regions...")
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
		prog.setMessage("Converting members...")
		
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
