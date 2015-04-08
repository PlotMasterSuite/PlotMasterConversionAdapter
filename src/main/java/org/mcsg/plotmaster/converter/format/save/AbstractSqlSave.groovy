package org.mcsg.plotmaster.converter.format.save

import groovy.sql.Sql;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.mcsg.plotmaster.PlotMember;
import org.mcsg.plotmaster.Region;
import org.mcsg.plotmaster.converter.format.SaveFormat

 abstract class AbstractSqlSave implements SaveFormat {

	 
	 DataSource ds;
	 String world;
 
	 def regions, plots, access_list
 
	 public void setup(String world, Map settings){
		 this.world = world;
 
		 regions = "${world}_regions"
		 plots  = "${world}_plots"
		 access_list = "${world}_access_list"
 
 
	 }

	public void saveRegion(Region region) {
		
		
	}

	public void saveRegionsBulk(List<Region> regions) {
		Sql sql = getSql()
		
		
		
		
	}

	public void saveMember(PlotMember member) {
		// TODO Auto-generated method stub
		
	}

	public void saveMembersBulk(List<PlotMember> members) {
		// TODO Auto-generated method stub
		
	}

	public boolean supportsBulk() {
		return true;
	}
	
	Sql getSql(){
		new Sql(ds)
	}

	
}
