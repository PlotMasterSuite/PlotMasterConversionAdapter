package org.mcsg.plotmaster.converter.format

import org.mcsg.plotmaster.PlotMember
import org.mcsg.plotmaster.Region

interface LoadFormat {

	
	void setup(Map settings)
	
	
		
	/**
	 * Attempts to load the next region
	 * @return 
	 */
	Region nextRegion()
	
	
	/*
	 * Attempts to load the next 'amount' of regions
	 */
	List<Region> loadRegionsBulk(int amount)
	
	/**
	 * Attempts to load the next plotMember
	 * @return the next plotMember, or null of none exist
	 */
	PlotMember nextMember()
	
	/**
	 * Attempts to load the next 'amount' of plotMemebers
	 * @return the next plotMembers
	 */
	PlotMember loadMembersBulk(int amount)
	
	


	
	/**
	 * Returns true if this format supports bulk loading
	 * @return
	 */
	boolean supportsBulk()
	
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
	
}
