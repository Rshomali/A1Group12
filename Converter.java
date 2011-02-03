import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;

public class Converter extends StandardFilter
{
	protected HashMap<Integer,ConversionFunction> conversionFunction;

	public Converter(HashMap<Integer, ConversionFunction> IDsAndFuncs)
	{
		super(new Vector(Arrays.asList(IDsAndFuncs.keySet().toArray(new Integer[]{}))));
		conversionFunction = IDsAndFuncs;
	}

	public void processIDAndMeasurement(int id, double measurement)
	{
		System.out.println("\n" + this.getName() + "YYYESSSSS !!!!   ID: " + id + " value: " + measurement );
		ConversionFunction func = conversionFunction.get(id);
		double result;
		if(func == null)
			result =  measurement;
		else
			result = (double)func.execute(measurement);
			
		long longResult = Double.doubleToLongBits(result);
		writeID(id, currentPort);
		writeMeasurement(longResult, currentPort);
	}
}
