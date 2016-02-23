package scheduling.ILP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import flexRay.FlexRayConstants;
import flexRay.FlexRayStaticMessage;
import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpDirection;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.MpSolver;
import net.sf.jmpi.main.MpVariable;
import net.sf.jmpi.main.expression.MpExpr;
import net.sf.jmpi.main.expression.MpExprTerm;
import net.sf.jmpi.solver.cplex.SolverCPLEX;
import net.sf.jmpi.solver.gurobi.SolverGurobi;
import net.sf.jmpi.solver.sat4j.SolverSAT4J;
import SchedulingInterfaces.Schedule;

public class SchedulerILP
{
	private boolean debug=false;

	/**
	 * generates an MpProblem file for jmpi. taking the dynamic segment into account
	 * @param schedule
	 * @param dists
	 * @param n_tot: number of frames to consider ONLY STATIC SEGMENT
	 * @param frameArea: area in frames for which the algorithm should be applied. For FlexRay 2.1 this is one cycle, hence: n_all, for FlexRay 3.0 the full schedule, hence n_all*c
	 * @return
	 */
	public MpProblem generateProblem(Schedule schedule, Map<String, Integer> dists, int n_tot)
	{
		MpProblem problem = new MpProblem();
		int n=schedule.getSlotsPerCycle();
		Set<String> ecus = dists.keySet();
		
		//add Variables:
		for(String e:ecus){
			for(int k=0;k<n_tot;k++){
				problem.addVar(x(k,e),Boolean.class);
			}
			
		}
		
		//objective
		MpExpr objective =  new MpExpr();
		for(String e:ecus){
			for(int k=0;k<n_tot;k++){
				objective.add(x(k, e));
			}
		}
		problem.setObjective(objective, MpDirection.MIN);
		
		for(String e:ecus){
			for(int i=0;i<n_tot;i++){
				MpExpr lhs = new MpExpr();
				for(int k=i;k<i+dists.get(e);k++){
					lhs.add(x(k%n_tot,e));
				}
				problem.add(lhs, ">=", 1);
			}
			
		}
		

		for(int k=0;k<n_tot;k++){
			MpExpr lhs = new MpExpr();
			for(String e:ecus){
				lhs.add(x(k,e));
			}
			problem.add(lhs,"<=",1);
		}
		
		return problem;
	}
	
	/**
	 * generates an MpProblem file for jmpi. taking the dynamic segment into account
	 * @param schedule
	 * @param dists
	 * @param frameArea: area in frames for which the algorithm should be applied. For FlexRay 2.1 this is one cycle, hence: n_all, for FlexRay 3.0 the full schedule, hence n_all*c
	 * @return
	 */
	public MpProblem generateProblemDyn(Schedule schedule, Map<String, Integer> dists, Map<String, Integer> n_slots, int n_tot)
	{
		MpProblem problem = new MpProblem();
		int n_all= getN_all(schedule);
		int n=schedule.getSlotsPerCycle();
		Set<String> ecus = dists.keySet();
		
		//add Variables:
		for(String e:ecus){
			for(int k=0;k<n_tot;k++){
				problem.addVar(x(k,e),Boolean.class);
			}
			
		}
		
		//objective
		MpExpr objective =  new MpExpr();
		for(String e:ecus){
			for(int k=0;k<n_tot;k++){
				objective.add(x(k, e));
			}
		}
		problem.setObjective(objective, MpDirection.MIN);
		
		for(String e:ecus){
			for(int i=0;i<n_tot;i++){
				MpExpr lhs = new MpExpr();
				for(int k=i;k<i+dists.get(e);k++){
					lhs.add(x(k%n_tot,e));
				}
				problem.add(lhs, ">=", n_slots.get(e));
			}
			
		}
		
		for(String e:ecus){
			for(int k=0;k<n_tot;k++){
				if(k%n_all>=n){
					MpExpr lhs = new MpExpr();
					lhs.add(x(k,e));
					problem.add(lhs,"=",0);
				}
			}
			
		}
		

		for(int k=0;k<n_tot;k++){
			MpExpr lhs = new MpExpr();
			for(String e:ecus){
				if(k%n_all<n){
					lhs.add(x(k,e));
				}
			}
			problem.add(lhs,"<=",1);
		}
		
		return problem;
	}
	
	/**
	 * generates an MpProblem file for jmpi. taking the dynamic segment into account. Bandwidth-based
	 * @param schedule
	 * @param dists
	 * @param frameArea: area in frames for which the algorithm should be applied. For FlexRay 2.1 this is one cycle, hence: n_all, for FlexRay 3.0 the full schedule, hence n_all*c
	 * @return
	 */
	public MpProblem generateProblem(Schedule schedule, Collection<FlexRayStaticMessage> messages, int n_tot)
	{
		MpProblem problem = new MpProblem();
		int n_all= getN_all(schedule);
		int n=schedule.getSlotsPerCycle();
		Set<String> ecus = getECUs(messages);
		
		//add Variables:
		for(String e:ecus){
			for(int k=0;k<n_tot;k++){
				problem.addVar(x(k,e),Boolean.class);
			}
			
		}
		System.out.println("problem.getVariablesCount()="+problem.getVariablesCount());
		
		//objective
		MpExpr objective =  new MpExpr();
		for(String e:ecus){
			for(int k=0;k<n_tot;k++){
				objective.add(x(k, e));
			}
		}
		problem.setObjective(objective, MpDirection.MIN);
		
		for(String e:ecus){
			Set<Double> periods =getPeriods(e, messages);
			System.out.println("e="+e);
			for(double p:periods){
				System.out.println("p="+p);
				int n_slots=getMinSlots(p,e,messages,schedule.getSlotSize());
				int minDist=(int)Math.floor(p/schedule.getSlotDuration());
				for(int i=0;i<n_tot;i++){
//					System.out.println("i="+i);
					MpExpr lhs = new MpExpr();
					for(int k=i;k<i+minDist;k++){
						lhs.add(x(k%n_tot,e));
					}
					problem.add(lhs, ">=", n_slots);
				}
				System.out.println("problem.getConstraintsCount()="+problem.getConstraintsCount());
			}
			
		}
		
		for(String e:ecus){
			for(int k=0;k<n_tot;k++){
				if(k%n_all>=n){
					MpExpr lhs = new MpExpr();
					lhs.add(x(k,e));
					problem.add(lhs,"=",0);
				}
			}
			
		}
		

		for(int k=0;k<n_tot;k++){
			MpExpr lhs = new MpExpr();
			for(String e:ecus){
				if(k%n_all<n){
					lhs.add(x(k,e));
				}
			}
			if(k%n_all<n){
				MpConstraint constraint=problem.add(lhs,"<=",1);
			}
		}
		
		return problem;
	}

	private int getMinSlots(double p, String e, Collection<FlexRayStaticMessage> messages, int slotLen)
	{
		int bandwidth=0;
		for(FlexRayStaticMessage msg:messages){
			if(((String)msg.getSender()).equalsIgnoreCase(e)){
				if(msg.getPeriod()<=p){
					bandwidth+=(p/msg.getPeriod()*(msg.getSize() +FlexRayConstants.HEADER_LEN));
				}
			}
		}
		return (int)Math.ceil(bandwidth/(double)(slotLen-1));
	}

	private Set<Double> getPeriods(String e, Collection<FlexRayStaticMessage> messages)
	{
		Set<Double> periods = new LinkedHashSet<Double>();
		for(FlexRayStaticMessage msg:messages){
			if(((String)msg.getSender()).equalsIgnoreCase(e)){
				periods.add(msg.getPeriod());
			}
		}
		return periods;
	}

	private Set<String> getECUs(Collection<FlexRayStaticMessage> messages)
	{
		Set<String> ecus = new LinkedHashSet<String>();
		for(FlexRayStaticMessage msg: messages){
			ecus.add((String)msg.getSender());
		}
		return ecus;
	}

	public Map<String,List<Integer>> updateSchedule(MpResult result,Schedule schedule, Map<String, Integer> dists,int n_tot){
		
		Map<String,List<Integer>> ecuToFrames =new LinkedHashMap<String, List<Integer>>();
		
		Set<String> ecus = dists.keySet();
		
		for(String e:ecus){
			ecuToFrames.put(e,new ArrayList<Integer>());
			for(int k=0;k<n_tot;k++){
				if(!result.containsVar(x(k,e))){
					throw new IllegalArgumentException("Result does not contain variable " + x(k,e));
				}
				if(result.getBoolean(x(k,e))){
					ecuToFrames.get(e).add(k);
				}
			}	
		}
		
		return ecuToFrames;
	}

	public int getN_all(Schedule schedule){
		return (int)Math.ceil(schedule.getCycleDuration()/schedule.getSlotDuration());
//		return schedule.getSlotsPerCycle();
	}
	
	protected Map<VarILP,VarILP> map = new HashMap<VarILP, VarILP>();
	
	private VarILP x(int i,String e){
		VarILP var = new VarILP("x_"+i+"_"+e,"bool");
		if(map.containsKey(var)){
			return map.get(var);
		} else {
			map.put(var, var);
			return var;
		}
	}
	
	public int availableSlots(Schedule schedule, int frameArea)
	{
		if(schedule.getSlotsPerCycle()*schedule.getNumberOfCycles()>frameArea){
			return schedule.getSlotsPerCycle();
		}else{
			return schedule.getSlotsPerCycle()*schedule.getNumberOfCycles();
		}
	}

	public MpResult solve(MpProblem problem,int timeout)
	{
//		MpSolver solver = new SolverGurobi();
//		MpSolver solver = new SolverCPLEX();
		MpSolver solver = new SolverSAT4J();
		solver.setTimeout(timeout);
		solver.add(problem);
		return solver.solve();
	}
	
	public HashMap<String, Collection<String>>getReceivers(Collection<FlexRayStaticMessage> messages){
		HashMap<String, Collection<String>> receivers = new HashMap<String, Collection<String>>();

		for(FlexRayStaticMessage m:messages){
			receivers.put(m.getSender().toString(),m.getReceivers());
		}
		return receivers;
	}

}
