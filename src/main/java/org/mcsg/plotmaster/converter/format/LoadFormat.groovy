package org.mcsg.plotmaster.converter.format

import org.mcsg.plotmaster.PlotMember
import org.mcsg.plotmaster.Region

interface LoadFormat {

	
	void setup(String world, Map settings)
	
	
	Map getSettings()
	
	
	Map loadSettings()
	
		
	/**
	 * Attempts to load the next region
	 * @return 
	 */
	Region nextRegion(int index)
	
	
	/*
	 * Attempts to load the next 'amount' of regions
	 */
	List<Region> loadRegionsBulk(int index, int amount)
	
	/**
	 * Attempts to load the next plotMember
	 * @return the next plotMember, or null of none exist
	 */
	PlotMember nextMember(int index)
	
	/**
	 * Attempts to load the next 'amount' of plotMemebers
	 * @return the next plotMembers
	 */
	List<PlotMember> loadMembersBulk(int index, int amount)
	
	


	
	/**
	 * Returns true if this format supports bulk loading
	 * @return
	 */
	boolean supportsBulk()
	
	
	boolean supportsPlotThreading()
	
	
	boolean supportsMemberThreading()
	/**
	 * Attempt to retrieve the number of regions to load
	 * @return the amount, -1 if it cannot be calculated
	 */
	int getAmountOfRegions()
	
	/**
	 * Attemt to retrieve the number of memebers
	 * @return the amount, -1 if it cannot be calculated
	 */
	int getAmountOfMembers()
	
	
	void finish()
}
