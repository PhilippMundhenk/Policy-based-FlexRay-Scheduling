package scheduling.ILP;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import SchedulingInterfaces.Schedule;

public class MineSchedulerILP
{
	private boolean debug=false;

	/**
	 * generates an MpProblem file for jmpi.
	 * @param schedule
	 * @param dists
	 * @param frameArea: area in frames for which the algorithm should be applied. For FlexRay 2.1 this is one cycle, hence: n_all, for FlexRay 3.0 the full schedule, hence n_all*c
	 * @return
	 */
	public MpProblem generateProblem(Schedule schedule, Map<String, Integer> dists, int frameArea)
	{
		MpProblem problem = new MpProblem();
		int n_all= getN_all(schedule);
		int n=schedule.getSlotsPerCycle();
		Set<String> ecus = dists.keySet();
		int availableSlots= availableSlots(schedule, frameArea);
		
		//add Variables:
		for(String e:ecus){
			for(int i=1;i<frameMax(e,availableSlots, dists);i++){
				problem.addVar(x(i,e),Integer.class);
				problem.addVar(xb(i,e),Boolean.class);
			}
			
		}
		
		//objective
		MpExpr objective =  new MpExpr();
		for(String e:ecus){
			for(int i=1;i<frameMax(e,availableSlots, dists);i++){
				objective.add(xb(i, e));
			}
		}
		problem.setObjective(objective, MpDirection.MIN);
		
		for(String e:ecus){
			for(int i=1;i<frameMax(e,availableSlots, dists)-1;i++){
				MpExpr lhs = new MpExpr();
				lhs.add(x(i,e),1);	//x(i,e)+1
				MpExpr rhs = new MpExpr();
				rhs.add(x(i+1,e));
				MpConstraint contraint =problem.add(lhs,"<=",rhs);
				if(debug)System.out.println(contraint);
			}
		}
		
		for(String e:ecus){
			for(int i=1;i<frameMin(frameArea,dists.get(e));i++){
				MpExpr xb_ie=new MpExpr();
				xb_ie.add(xb(i, e));
				MpConstraint constraint = problem.add(xb_ie,"=",1);
				if(debug)System.out.println(constraint);
			}
		}
		
//		for(String e:ecus){
//			for(int i=1;i<frameMax(e,availableSlots, dists)-1;i++){
//				MpExpr lhs=new MpExpr();
//				lhs.add(x(1,e),frameArea);	
//				lhs.addTerm(new MpExprTerm(-1,xb(i+1,e),x(1,e)));
//				lhs.addTerm(new MpExprTerm(-frameArea, xb(i+1,e))); //(1-[x_i+1,e])*(x_1,e+n_all*c)
//				lhs.addTerm(new MpExprTerm(1,xb(i+1,e),x(i+1,e)));
//				lhs.addTerm(new MpExprTerm(-1, x(i,e)));
//				
//				MpExpr rhs = new MpExpr();
//				rhs.add(dists.get(e));
//				rhs.add(2*frameArea);
//				rhs.addTerm(new MpExprTerm(-2*frameArea, xb(i,e)));//(1-[x_i,e])*2*n_all*c
//				
//				MpConstraint contraint =problem.add(lhs,"<=",rhs);
//				if(debug)System.out.println(contraint);
//			}
//		}
		
//	Simplified:
		for(String e:ecus){
			for(int i=1;i<frameMax(e,availableSlots, dists)-1;i++){
				MpExpr lhs=new MpExpr();
				lhs.add(x(1,e),frameArea);	
				lhs.addTerm(new MpExprTerm(1,x(i+1,e)));
				lhs.addTerm(new MpExprTerm(-1, x(i,e)));
				
				MpExpr rhs = new MpExpr();
				rhs.add(dists.get(e));
				
				MpConstraint contraint =problem.add(lhs,"<=",rhs);
				if(debug)System.out.println(contraint);
			}
		}
		
		for(String e: ecus){
			for(String et:ecus){
				if(!e.equals(et)){
					for(int i=1;i<frameMax(e,availableSlots, dists);i++){
						for(int j=1;j<frameMax(et,availableSlots, dists);j++){
							problem.addVar(y(e,et,i,j),Boolean.class);
							MpExpr rhs1 =new MpExpr();
							rhs1.add(x(j,et));	
							rhs1.addTerm(new MpExprTerm(2*frameArea, y(e,et,i,j)));//x_j,et +2*n_all*c*y_x_i,e,x_j,et
							MpExpr lhs1=new MpExpr();
							lhs1.add(x(i,e),1);
							MpExpr rhs2 =new MpExpr();
							rhs2.add(x(i,e),2*frameArea); //x_i,e +2*n_all*c(1-y_x_i,e,x_j,et)
							rhs2.addTerm(new MpExprTerm(-2*frameArea, y(e,et,i,j)));
							MpExpr lhs2=new MpExpr();
							lhs2.add(x(j,et),1);
							
							MpConstraint constraint =problem.add(lhs1, "<=", rhs1);	// < not available => +1
							MpConstraint constraint2 =problem.add(lhs2, "<=", rhs2);
							if(debug)System.out.println(constraint);
							if(debug)System.out.println(constraint2);
							
						}
					}
				}
			}
		}
		
		for(int k=0;k<cycles(frameArea,schedule);k++){
			for(String e:ecus){
				
				for(int i=1;i<frameMax(e,availableSlots, dists);i++){
					problem.addVar(y(k,i,e), Boolean.class);
				
					MpExpr rhs1=new MpExpr();
					rhs1.add(k*n_all,n,2*frameArea);	
					rhs1.addTerm(new MpExprTerm(-2*frameArea, y(k,i,e)));//k*n_all + n + (1-y_k,x_i,e)*2*n_all*c 
					MpExpr lhs1=new MpExpr();
					lhs1.add(x(i,e),1);	//x_i,e +1
					MpExpr lhs2=new MpExpr();
					lhs2.add(x(i,e),2*frameArea);	
					lhs2.addTerm(new MpExprTerm(-2*frameArea, y(k,i,e)));//2*n_all*c*y_k,x_i,e +x_i,e
					
					MpConstraint constraint =problem.add(lhs1,"<=",rhs1);	// +1, < not available
					MpConstraint constraint2 =problem.add(lhs2,">=",(k)*n_all);
					if(debug)System.out.println(constraint);
					if(debug)System.out.println(constraint2);
					
				}
				

			}
		}
		
		for(String e:ecus){
			for(int i=1;i<frameMax(e,availableSlots, dists);i++){
				MpExpr lhs=new MpExpr();
				for(int k=0;k<cycles(frameArea,schedule);k++){
					lhs.add(y(k,i,e));
				}
				MpConstraint constraint =problem.add(lhs, "=", 1);	//sum(y_k,x_i,e)=1
				if(debug)System.out.println(constraint);
			}
		}
		
		for(String e:ecus){
			for(int i=1;i<frameMax(e,availableSlots, dists);i++){
				MpExpr lhs = new MpExpr();
				lhs.add(x(i,e));
				
				MpConstraint constraint=problem.add(lhs,">=",0);
				if(debug)System.out.println(constraint);
			}
		}
		
		//additional constraint:
		int n_dyn=n_all-n;	//size dynamic segment
		for(String e:ecus){
			for(int i=1;i<frameMax(e,availableSlots, dists);i++){
				MpExpr lhs = new MpExpr();
				lhs.add(x(i,e));
				
				MpConstraint constraint =problem.add(lhs,"<=",i*dists.get(e)-n_dyn);
				if(debug)System.out.println(constraint);
			}
		}
		
		
		return problem;
	}
	
	public Map<String,List<Integer>> updateSchedule(MpResult result,Schedule schedule, Map<String, Integer> dists, int frameArea){
		
		Map<String,List<Integer>> ecuToFrames =new LinkedHashMap<String, List<Integer>>();
		
		int availableSlots= availableSlots(schedule, frameArea);
		Set<String> ecus = dists.keySet();
		
		for(String e:ecus){
			ecuToFrames.put(e,new ArrayList<Integer>());
			for(int i=1;i<frameMax(e,availableSlots, dists);i++){
				if(!result.containsVar(xb(i,e))){
					throw new IllegalArgumentException("Result does not contain variable " + xb(i,e));
				}
				if(!result.containsVar(x(i,e))){
					throw new IllegalArgumentException("Result does not contain variable " + x(i,e));
				}
				if(result.getBoolean(xb(i,e))){
					ecuToFrames.get(e).add(result.get(x(i,e)).intValue());
				}
			}	
		}
		
		return ecuToFrames;
	}


	private int cycles(int frameArea, Schedule schedule)
	{
		if(schedule.getNumberOfCycles()*schedule.getSlotsPerCycle()>frameArea){
			return 1;
		}else{
			return schedule.getNumberOfCycles();
		}
	}

	private int frameMin(int frameArea, int diste)
	{
		return (int)Math.ceil((double)frameArea/(double)diste);
	}

	private int frameMax(String e, int availableSlots,Map<String,Integer> dists)
	{
		// TODO make more flexible
		return availableSlots;
	}

	public int getN_all(Schedule schedule){
		return schedule.getSlotsPerCycle()+10;	//TODO: remove !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//		return (int)Math.floor(schedule.getCycleDuration()/schedule.getSlotDuration()); //TODO: check if ceil better?
	}
	
	private VarILP x(int i,String e){
		return new VarILP("x_"+i+"_"+e,"int");
	}
	
	private VarILP xb(int i,String e){
		return new VarILP("xb_"+i+"_"+e,"bool");
	}
	
	private Object y(String e, String et, int i, int j)
	{
		return new VarILP("y_x"+i+"_"+e+"_x"+j+"_"+et,"bool");
	}
	
	private Object y(int k,int i,String e)
	{
		return new VarILP("y_"+k+"_x"+i+"_"+e,"bool");
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
		MpSolver solver = new SolverCPLEX();
		solver.setTimeout(timeout);
		solver.add(problem);
		return solver.solve();
	}

}
