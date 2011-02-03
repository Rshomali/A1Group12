import java.util.Vector;

public class FilterOut extends StandardFilter
{
	public FilterOut(Vector<Integer> idToProcess)
	{
		super(idToProcess);
	}
		

	public void processIDAndMeasurement(int id, double measurement)
	{
		if(idToProcess.contains(id))
		{
			writeID(id, currentPort);
			writeMeasurement(Double.doubleToLongBits(measurement), currentPort);
		}	
	}
}