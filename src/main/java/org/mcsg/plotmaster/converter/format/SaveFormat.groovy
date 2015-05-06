package org.mcsg.plotmaster.converter.format

import org.mcsg.plotmaster.PlotMember
import org.mcsg.plotmaster.Region

interface SaveFormat {

	
	void setup(String world, Map settings)
	
	
	Map getSettings()
	
	/**
	 * Save a region
	 * @param region
	 */
	void saveRegion(int index, Region region)
	
	/**
	 * Save regions
	 * @param regions
	 */
	void saveRegionsBulk(int index, List<Region> regions)
	
	/**
	 * save a member
	 * @param member
	 */
	void saveMember(int index, PlotMember member)
	
	/**
	 * save members
	 * @param members
	 */
	void saveMembersBulk(int index, List<PlotMember> members)
	
	
	/**
	 * returns true of this format supports bulk saving
	 * @return
	 */
	boolean supportsBulk()
	
	/**
	 * 
	 * @return true if supports threading
	 */
	boolean supportsPlotThreading()
	
	boolean supportsMemberThreading()
	
	void finish()
}
