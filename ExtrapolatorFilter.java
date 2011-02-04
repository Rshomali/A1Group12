
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


public class ExtrapolatorFilter extends StandardFilter {
	private int[] idToProcess;
	private double[] frame;
	private int measurementsPerFrame;
	private boolean wildPointFrameFound;
	private ArrayList<double[]> cachedFrames;
	private int measurementsRead;
	private double[] lastValidFrame;
	private boolean rejectedValuesFileIsOpen;
	private Calendar timeStamp;
	private SimpleDateFormat timeStampFormat;
	private DataOutputStream rejectedValuesFile;
	
	protected HashMap<Integer, WildPointTest> wildPointTest;
	
	public ExtrapolatorFilter(HashMap<Integer, WildPointTest> IDsAndWildPointTests)
	{
		super(new Vector<Integer>(Arrays.asList(IDsAndWildPointTests.keySet().toArray(new Integer[]{}))));
		
		measurementsPerFrame = 5;
		wildPointFrameFound = false;
		frame = new double[measurementsPerFrame];
		cachedFrames = new ArrayList<double[]>(1);
		measurementsRead = 0;
		rejectedValuesFileIsOpen = false;
		
		timeStamp = Calendar.getInstance();
		timeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");
		
		lastValidFrame = new double[measurementsPerFrame];
		
	}
	
	public void run()
    {

    } // run

	public void processIDAndMeasurement(int id, double measurement_value)
	{
		/* 
		 * If have not obtained a frame
		 * 		Store Next Measurement 
		 * Else
		 * 
		 * 	Foreach Measurement in frame
		 * 		If Measurement should be processed
		 * 			If Measurement value is a Wild point
		 * 				Store Frame
		 * 				Set WildPoint = 1
		 * 			Else 
		 * 				Save Measurement as Last Good Measurement read
		 * 				If WildPoint == 1
		 * 					Extrapolate Measurement
		 * 					Send all stored Frames with extrapolated measurement value
		 * 					Set WildPoint = 0
		 * 				End If
		 * 			End IF
		 * 		End IF
		 * 	End For
		 * End IF
		 */
		
		// Keep storing measurements until we have a full frame
		storeMeasurement(id, measurement_value);
		
		// check if we have a full frame by counting how many frames we already stored
		if ( allMeasurementsRead() )
		{
			// when having more than one extrapolation, this flag will tell us
			// when all of them are true
			boolean wildPointTestPassed = false;
			
			// Go over the measurements in the frame and test for wild points
			for (int i=0; i< this.frame.length; i++) 
			{
				// check if the measurement ID is one of the IDs I should check for wild points
			    if (shouldProcessID(i))
			    {
			    	// Check the measurement if its value is a wild point
			    	// if not all  measurements we need to check are wild points then
			    	// this is a valid frame
			    	if( ! isWildPoint(i,measurement_value) )
			    	{
			    		wildPointTestPassed = true;
			    	} // if
			    } // if
			} // for
			
	    	// Not a wild point

    		// If this is a valid frame and we had a  wild point in previous
			// frames then we need to extrapolate
    		if( wildPointTestPassed)
    		{
    			// if previous frame[s] contained wild points
    			if(this.wildPointFrameFound)
    			{
    				// calculate extrapolation value for each required measurement
	    			double[] extrapolatedValue = extrapolateMeasurements();
	    			
	    			// send cached frames (which contain wild points) with
	    			// the extrapolated value for each instead
	    			processAllCachedFrames(extrapolatedValue);
	    			
	    			// reset the flag since we lready took care of wild points
	    			this.wildPointFrameFound = false;
    			}
    			else
    			{
    				for (int i=0; i< this.frame.length; i++) 
    				{
    					sendToOutport(i, this.frame[i]);
    				}
    			}
    		}
    		else
    		{
    			this.cachedFrames.add(this.frame);
    			
    			// this flag is set so that the next time we get a valid frame
    			// we know that we need to extrapolate measurements
    			this.wildPointFrameFound = true;	
    		}
    		
    		// clear current frame in order to start
    		// collecting measurements to form a new frame
    		clearFrame();
    		
		} // if
	}
	
	private void processAllCachedFrames(double[] extrapolated_value)
	{
		// open rejected file
		if( ! this.rejectedValuesFileIsOpen)
		{
			// Open file, will create it if doesn't exist
			try {
				this.rejectedValuesFile = new DataOutputStream(new FileOutputStream("rejected_file.txt"));
				
				//write header to file
				this.rejectedValuesFile.writeUTF("Time:\t\tTemperature (C):\tAltitude (m):\n");
				this.rejectedValuesFile.writeUTF("___________________________________________________________________\n");
				
				this.rejectedValuesFileIsOpen = true;
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		try {
			
			Iterator<double[]> itr = this.cachedFrames.iterator();
			while(itr.hasNext())
			{
				double[] temp_frame = itr.next();
	            this.timeStamp.setTimeInMillis((long) temp_frame[0]);
	            this.rejectedValuesFile.writeUTF(this.timeStampFormat.format(this.timeStamp.getTime()) +"\t\t");
	        	
				for(int i=0; i< temp_frame.length; i++)
				{
					if(shouldProcessID(i))
					{
						// write to rejected file
						this.rejectedValuesFile.writeUTF(temp_frame[i] +"\t");
						sendToOutport(i, extrapolated_value[i]);
					}
					else
					{
						sendToOutport(i, temp_frame[i]);
					}
				}
				this.rejectedValuesFile.writeUTF("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean shouldProcessID(int id)
	{
		for(int i=0; i < this.idToProcess.length;i++)
		{
			if(this.idToProcess[i] ==id )
			{
				return true;
			}
		}
		return false;
	}
	
	private void sendToOutport(int id, double value)
	{
		long longResult = Double.doubleToLongBits(value);
		writeID(id, currentPort);
		writeMeasurement(longResult, currentPort);
	}
	
	private double[] extrapolateMeasurements()
	{
		double[] extrapolated_values = new double[this.measurementsPerFrame];

		for(int i=0; i<this.measurementsPerFrame;i++)
		{
			if(this.shouldProcessID(i))
			{
				extrapolated_values[i] = (this.frame[i]+this.lastValidFrame[i])/2;
			}
		}
		
		return extrapolated_values;
	}
	
	private void clearFrame()
	{
		this.frame = new double[this.measurementsPerFrame];
	}
	
	private void storeMeasurement(int id, double value)
	{
		this.frame[id] = value;
	}
	
	private boolean allMeasurementsRead()
	{
		if (this.measurementsRead == this.measurementsPerFrame)
			return true;
		else
			return false;
	}
	
	private boolean isWildPoint(int id, double value)
	{
		return this.wildPointTest.get(id).execute(value);
	}

}