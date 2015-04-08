package org.mcsg.plotmaster.converter

import groovy.swing.SwingBuilder

import javax.swing.JFrame
import javax.swing.JProgressBar
import org.mcsg.plotmaster.converter.format.load.PlotMeSqliteLoader
import org.mcsg.plotmaster.converter.format.save.PlotMasterFlatFileSaver
import org.mcsg.plotmaster.converter.format.save.PlotMasterMySqlSaver;
import org.mcsg.plotmaster.converter.format.save.PlotMasterSqliteSave
import org.mcsg.plotmaster.converter.util.Progress
import java.awt.BorderLayout as BL

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
		
		ConversionAdapter adapter = new ConversionAdapter(new PlotMeSqliteLoader(), new PlotMasterSqliteSave(), settings)
		
		Progress p = adapter.beginConversion() 
		JProgressBar progress;
		Thread.start {
			
			new SwingBuilder().edt{
				frame(title: 'Converting', size: [500, 100], show: true) {
					borderLayout()
					label(text: "Converting...", constraints: BL.NORTH)
					progress = progressBar(constraints: BL.CENTER)
				}
			}
			
			
			while(!p.isFinished()) {
				progress.setStringPainted(true)
				progress.setMaximum(p.getMax().toInteger())
				progress.setValue(p.getProgress().toInteger())
				//println "${((int)p.getPercent() * 100)}%"
				Thread.sleep(500)
			}
		}
		
		p.waitForFinish()

		
		
		double finish = ((long)((System.currentTimeMillis() - time) / 100)) / 10.0
		
		
		println "${finish} seconds"
		
	}
	
	
}
