package org.mcsg.plotmaster.converter.util;

import java.util.concurrent.atomic.AtomicLong;

public class Progress {

	private Object wait = new Object();

	private AtomicLong max = new AtomicLong(100);
	private AtomicLong prog = new AtomicLong(0); 
	private volatile boolean finished;


	public void setMax(long max){
		this.max.set(max);;
	}

	public long getMax(){
		return max.get();
	}

	public void setProgress(long prog){
		this.prog.set(prog);
	}

	public void incProgress(long inc){
		this.prog.addAndGet(inc);
	}

	public long getProgress(){
		return this.prog.get();
	}

	public  double getPercent(){
		return (prog.get() + 0.0) / (max.get() + 0.0);
	}

	public void waitForFinish(){
		synchronized (wait) {
			try {
				wait.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void waitForFinish(long time, Runnable r){
		synchronized (wait) {
			while(!finished){
				try {
					wait.wait(time);
					r.run();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void finish(){
		this.finished = true;
		synchronized (wait) {
			wait.notify();
		}
	}

	public boolean isFinished(){
		return finished;
	}




}