package org.mcsg.plotmaster.converter

import org.mcsg.plotmaster.converter.format.load.PlotMeSQLITE
import org.mcsg.plotmaster.converter.format.save.PlotMasterFlatFile
import org.mcsg.plotmaster.converter.util.Progress

class Converter {
	
	
	static main(args) {
		
		Map settings = [
			
			load: [ :
				 
			],
			save: [
				location: "plots/"
			]
		]
		
		long time = System.currentTimeMillis()
		
		ConversionAdapter adapter = new ConversionAdapter(new PlotMeSQLITE(), new PlotMasterFlatFile(), settings)
		
		Progress p = adapter.beginConversion() 
		
		Thread.start {
			while(!p.isFinished()) {
				println "${((int)p.getPercent() * 100)}%"
				Thread.sleep(1000)
			}
		}
		
		p.waitForFinish()

		
		
		double finish = ((long)((System.currentTimeMillis() - time) / 100)) / 10.0
		
		
		println "${finish} seconds"
		
	}
	
	
}
