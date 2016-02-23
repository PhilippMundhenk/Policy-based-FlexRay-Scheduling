package flex;



public class Tester {
	
	public static void main(String[] args){

		
		FlexRayVerifier<String> verifier = new FlexRayVerifier<String>(62, 64, 5, 0.067, 41);
		for(int i=0;i<64;i++){
			verifier.addSlotAtCycle(1, i);
			//verifier.addSlotAtCycle(23, (2*i+1)%64);
			//verifier.addSlotAtCycle(45, (4*i)%64);
		}
		
		verifier.addMessage("m1", 2, 30, 5, "m1");
		verifier.addMessage("m2", 1, 3, 5, "m2");
		//verifier.addMessage("m3", 3, 50, 10);
		
		verifier.calculate();
		
		System.out.println("m1"+" "+verifier.getDelay("m1"));
		System.out.println("m2"+" "+verifier.getDelay("m2"));
		//System.out.println("m3"+" "+verifier.getDelay("m3"));
		
	}
	
	

}
