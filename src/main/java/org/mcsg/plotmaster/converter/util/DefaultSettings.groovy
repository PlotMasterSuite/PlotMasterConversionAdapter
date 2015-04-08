package org.mcsg.plotmaster.converter.util

import org.mcsg.plotmaster.Settings;

class DefaultSettings {
	
	static Map get() {
		return Settings.config
	}
	
	static Map getWorld(String world) {
		def ret
		get().worlds.each {
			if(it.world == world) {
				ret =  it
				return
			}
		}
		return ret
	}
	
}
