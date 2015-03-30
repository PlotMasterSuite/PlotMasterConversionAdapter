package org.mcsg.plotmaster.converter.format

import org.mcsg.plotmaster.PlotMember
import org.mcsg.plotmaster.Region

interface SaveFormat {

	
	void setup(Map settings)
	
	
	/**
	 * Save a region
	 * @param region
	 */
	void saveRegion(Region region)
	
	/**
	 * Save regions
	 * @param regions
	 */
	void saveRegionsBulk(List<Region> regions)
	
	/**
	 * save a member
	 * @param member
	 */
	void saveMember(PlotMember member)
	
	/**
	 * save members
	 * @param members
	 */
	void saveMembersBulk(List<PlotMember> members)
	
	
	/**
	 * returns true of this format supports bulk saving
	 * @return
	 */
	boolean supportsBulk()
}
