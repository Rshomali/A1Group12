import java.util.*;
import java.io.*;

public class Converter extends StandardFilter
{
	protected HashMap<Integer, ConversionFunction> conversionFunction;

	public Converter(PipedInputStream inputReadPort[], PipedOutputStream outputWritePort[], HashMap<Integer, ConversionFunction> IDsAndFuncs)
	{
		super(inputReadPort, outputWritePort, (new ArrayList<Integer>(IDsAndFuncs.keySet())).toArray(new Integer[]{}) );//, Arrays.asList(IDsAndFuncs.keySet()).toArray(new Integer[]{}));
		conversionFunction = IDsAndFuncs;
	}

	public void processIDAndMeasurement(int id, double measurement)
	{
		ConversionFunction func = conversionFunction.get(id);
		double result;
		if(func == null)
			result =  measurement;
		else
			result = func.execute(measurement);
		
		long longResult = Double.doubleToLongBits(result);
		
		writeID(id, currentPort);
		writeMeasurement(longResult, currentPort);
	}
}
