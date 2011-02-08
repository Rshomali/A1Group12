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
			//42 is the ID that indicates that a measure was extrapolated, therefore we need to keep it
			if(idToProcess[i] == id || id == 42)
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