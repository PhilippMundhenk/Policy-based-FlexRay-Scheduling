package scheduling.ILP;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import SchedulingInterfaces.Schedule;

public class PlausibilityCheck {

	public void check(Map<String, List<Integer>> ecuToFrames,Schedule schedule, Map<String, Integer> dists, int frameArea){
		
		for(String e:ecuToFrames.keySet()){
			for(int f:ecuToFrames.get(e)){
				if(f%schedule.getSlotsPerCycle()>schedule.getSlotsPerCycle()){
//					throw new IllegalArgumentException("frame "+ f + " is outside the static segment (" + schedule.getSlotsPerCycle() +")");
					System.out.println("frame "+ f + " is outside the static segment (" + schedule.getSlotsPerCycle() +")");
				}
				
			}
			
			List<Integer> frames =ecuToFrames.get(e);
			Collections.sort(frames);
			for(int i=0;i<(frames.size()-2);i++){
				if(frames.get(i+1)-frames.get(i)>dists.get(e)){
//					throw new IllegalArgumentException("maximal slot dist "+ dists.get(e) + "is not kept for frames " + frames.get(i+1) +"-" + frames.get(i));
					System.out.println("ECU " + e + ": maximal slot dist "+ dists.get(e) + " is not kept for frames " + frames.get(i+1) +"-" + frames.get(i));
				}
			}
			if(frames.get(0)+frameArea-frames.get(frames.size()-1)>dists.get(e)){
//				throw new IllegalArgumentException("maximal slot dist "+ dists.get(e) + "is not kept for frames " + frames.get(1) +" + (" +frameArea + ") -" + frames.get(frames.size()-1));
				System.out.println("ECU " + e + ": maximal slot dist "+ dists.get(e) + " is not kept for frames " + frames.get(0) +" + (" +frameArea + ") -" + frames.get(frames.size()-1));
			}
		}
		
		
		String[][] frameOccupancy = new String[schedule.getSlotsPerCycle()][getCycleNumber(schedule, frameArea)];
		SchedulerILP scheduler=new SchedulerILP();
		int n_all=scheduler.getN_all(schedule);
		for(String e:ecuToFrames.keySet()){
			for(int f:ecuToFrames.get(e)){
				int slot = f%schedule.getSlotsPerCycle();
				int cycle = (int)Math.floor((double)f/(double)n_all);
				if(frameOccupancy[slot][cycle]!=null &&frameOccupancy[slot][cycle].equalsIgnoreCase("")){
//					throw new IllegalArgumentException("ecu " + e + " cannot occupy slot " + slot +" in cycle " + cycle + "as it is already occupied by ecu " + frameOccupancy[slot][cycle]);
					System.out.println("ecu " + e + " cannot occupy slot " + slot +" in cycle " + cycle + "as it is already occupied by ecu " + frameOccupancy[slot][cycle]);
				}
				frameOccupancy[slot][cycle]=e;
			}
		}
		
	}

	private int getCycleNumber(Schedule schedule, int frameArea) {
		SchedulerILP scheduler = new SchedulerILP();
		int availableSlots = scheduler.availableSlots(schedule, frameArea);
		if(availableSlots>schedule.getSlotsPerCycle()){
			return schedule.getNumberOfCycles();
		}else{
			return 1;
		}
	}
	
	
	
}
