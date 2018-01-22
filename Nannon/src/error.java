import java.util.*;
public class error
{
	public static void main(String[] args)
	{
		double fea = 3;
		double weight = 4;
		double predicted = 0;
		
		double wsum = (fea*weight) + (-1*1.5);
		double output = 1/(1 + Math.pow(2.71828, -1*wsum));
		System.out.println(output);
		double error = (output - predicted) * (output - predicted);
		System.out.println(error);
	}
}
