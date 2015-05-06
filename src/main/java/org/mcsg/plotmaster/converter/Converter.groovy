package org.mcsg.plotmaster.converter

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import groovy.swing.SwingBuilder

import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar

import org.mcsg.plotmaster.converter.format.LoadFormat
import org.mcsg.plotmaster.converter.format.SaveFormat
import org.mcsg.plotmaster.converter.format.load.PlotMeSqliteLoader
import org.mcsg.plotmaster.converter.format.save.PlotMasterFlatFileSaver
import org.mcsg.plotmaster.converter.format.save.PlotMasterMySqlSaver;
import org.mcsg.plotmaster.converter.format.save.PlotMasterSqliteSave
import org.mcsg.plotmaster.converter.util.Progress

class Converter {
	
	static final Map<String, Class<LoadFormat>> loaders = [:]
	static final Map<String, Class<SaveFormat>> savers = [:]
	static final Gson gson = new GsonBuilder().setPrettyPrinting().create()
	
	static main(String[] args) {
		register()
		
		if(args.length < 4) {
			println "java -jar PlotMasterConverter.jar <loadformat> <saveformat> <loadworld> <saveworld>"
			println "Known Load Formats: ${loaders.keySet().toString()}"
			println "Known Save Formats: ${savers.keySet().toString()}"
			return
		}
		
		
		def loadFile = new File("${args[0]}.json")
		def saveFile = new File("${args[1]}.json")
		
		def loader = loaders[args[0]]?.newInstance()
		def saver = savers[args[1]]?.newInstance()
		
		if(loader == null) {
			println "Loader not found, please use a valid loader"
			return
		}
		if(saver == null) {
			println "Saver not found, please use a valid saver"
			return
		}
		
		if(!loadFile.exists() || !saveFile.exists()) {
			println "Settings files don't exist, creating now. Please edit ${loadFile.getPath()} and ${saveFile.getPath()} with the correct settings"
			
			if(!loadFile.exists())
				loadFile.setText(gson.toJson(loader.getSettings()))
			if(!saveFile.exists())
				saveFile.setText(gson.toJson(saver.getSettings()))
			
			return;
		}
		
		Map loadSettings = gson.fromJson(loadFile.getText(), Map.class)
		Map saveSettings = gson.fromJson(saveFile.getText(), Map.class)
		
		loader.setup(args[2], loadSettings)
		saver.setup(args[3], saveSettings)
		
		ConversionAdapter adapter = new ConversionAdapter(loader, saver)
		Progress prog = adapter.beginConversion()
		
		
		
		prog.waitForFinish(5000,{
			println (int)(prog.getPercent() * 100)
		})
		
		println "Finished"
	}
	
	
	public static void register() {
		loaders["PlotMeSqlite"] = PlotMeSqliteLoader.class
		
		savers["PlotMasterFlatFile"] = PlotMasterFlatFileSaver.class
		savers["PlotMasterSqlite"] = PlotMasterSqliteSave.class
		savers["PlotMasterMysql"] = PlotMasterMySqlSaver.class
		
	}
	
	
	
}
