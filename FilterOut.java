import java.util.Vector;
import java.io.*;

public class FilterOut extends StandardFilter
{	
	public FilterOut(PipedInputStream inputReadPort[], PipedOutputStream outputWritePort[], int idToProcess[])
	{
		super(inputReadPort, outputWritePort, idToProcess);
	}

	public void processIDAndMeasurement(int id, double measurement)
	{
		boolean contains = false;
		for(int i=0; i<idToProcess.length; ++i)
			if(idToProcess[i] == id)
			{
				contains = true;
				break;
			}
			
		if(contains)
		{
			writeID(id, currentPort);
			writeMeasurement(Double.doubleToLongBits(measurement), currentPort);
		}	
	}
}