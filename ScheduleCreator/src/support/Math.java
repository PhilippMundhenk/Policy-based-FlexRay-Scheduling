package support;

public class Math {
	public static long gcd(long[] input)
	{
	    long result = input[0];
	    for(int i = 1; i < input.length; i++) 
    	{
	    	result = gcd(result, input[i]);
    	}
	    return result;
	}
	
	public static long gcd(long a, long b)
	{
		//Euclide's algorithm
	    while (b > 0)
	    {
	        long temp = b;
	        b = a % b;
	        a = temp;
	    }
	    return a;
	}
	
	public static long lcm(long[] input)
	{
		long result = input[0];
	    for(int i = 1; i < input.length; i++)
    	{
	    	result = lcm(result, input[i]);
    	}
	    return result;
	}
	
	public static long lcm(long a, long b)
	{
	    return a * (b / gcd(a, b));
	}
	
	public static int gcd(int[] input)
	{
		int result = input[0];
	    for(int i = 1; i < input.length; i++) 
    	{
	    	result = gcd(result, input[i]);
    	}
	    return result;
	}
	
	public static int gcd(int a, int b)
	{
		//Euclide's algorithm
	    while (b > 0)
	    {
	    	int temp = b;
	        b = a % b;
	        a = temp;
	    }
	    return a;
	}
	
	public static int lcm(int[] input)
	{
		int result = input[0];
	    for(int i = 1; i < input.length; i++)
    	{
	    	result = lcm(result, input[i]);
    	}
	    return result;
	}
	
	public static int lcm(int a, int b)
	{
	    return a * (b / gcd(a, b));
	}
}
