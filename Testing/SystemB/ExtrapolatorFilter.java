
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


public class ExtrapolatorFilter extends StandardFilter {
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
	private boolean endofFile;
	private boolean validValueFound;
	private boolean[] validIds;
	private int measurementsToRead;
	private boolean print_star;
	private boolean received_star;
	private int star_id;
	private int measIdToAddStar;
	
	protected HashMap<Integer, WildPointTest> wildPointTest;
	
	public ExtrapolatorFilter(PipedInputStream inputReadPort[], PipedOutputStream outputWritePort[], HashMap<Integer, WildPointTest> IDsAndWildPointTests, int idsPerFrame)
	{
		super(inputReadPort, outputWritePort, (new ArrayList<Integer>(IDsAndWildPointTests.keySet())).toArray(new Integer[]{}) );
		
		wildPointTest = IDsAndWildPointTests;
		star_id = 42;
		measurementsPerFrame = 6;
		measurementsToRead = idsPerFrame;
		wildPointFrameFound = false;
		frame = new double[measurementsPerFrame];
		this.validIds = new boolean[measurementsPerFrame];
		for(int i=0; i<this.measurementsPerFrame;i++)
			this.validIds[i] = false;
		
		cachedFrames = new ArrayList<double[]>(1);
		measurementsRead = 0;
		rejectedValuesFileIsOpen = false;
		
		timeStamp = Calendar.getInstance();
		timeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");
		
		lastValidFrame = new double[measurementsPerFrame];
		
		endofFile = false;
		validValueFound = false;
		print_star = false;
		received_star = false;
		
	}
	

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
		
		if(id == -1)
		{
			this.endofFile = true;
			System.out.println(this.getName() + "::EndOfFile FOUND " + id + "\n");
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
		}
		else if(id == this.star_id)
		{
			// skip and add "*" when printing value
			this.print_star = true;
			this.received_star = true;
			System.out.println(this.getName() + ":: Received>> " + id + "\t" + measurement_value + "\n");
		}
		else
		{
			this.validIds[id] = true;
			// Keep storing measurements until we have a full frame
			storeMeasurement(id, measurement_value);
			System.out.println(this.getName() + ":: Received=> " + id + "\t" + measurement_value + "\n");
		}
		
		// check if we have a full frame by counting how many frames we already stored
		if ( (id != this.star_id) && (allMeasurementsRead()) )
		{
			
			Calendar TimeStamp = Calendar.getInstance();
			SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy::dd::hh:mm:ss");
			TimeStamp.setTimeInMillis(Double.doubleToLongBits(this.frame[0]));
			System.out.println(this.getName() + ":: TIME=" + TimeStampFormat.format(TimeStamp.getTime())+"\n");
			
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
			    	if( ! isWildPoint(i,this.frame[i]) )
			    	{
			    		wildPointTestPassed = true;
			    	} // if
			    	else
			    	{
			    		this.print_star = false;
			    	} //else
			    } // if
			} // for
			
	    	// Not a wild point

    		// If this is a valid frame and we had a  wild point in previous
			// frames then we need to extrapolate
    		if( wildPointTestPassed)
    		{
    			System.out.println(this.getName() + "::Tests PASSED Pressure=" + this.frame[3] + "\n");
    			for(int i=0; i<this.lastValidFrame.length;i++)
    				this.lastValidFrame[i] = this.frame[i];
    			
    			// if previous frame[s] contained wild points
    			if(this.wildPointFrameFound)
    			{
    				System.out.println(this.getName() + "::WildPointFrame FOUND " + "\n");
    				
    				// calculate extrapolation value for each required measurement
	    			double[] extrapolatedValue = extrapolateMeasurements();
	    			
	    			// send cached frames (which contain wild points) with
	    			// the extrapolated value for each instead
	    			processAllCachedFrames(extrapolatedValue);
	    			
	    			// send current frame
	   				for (int i=0; i< this.frame.length; i++) 
    				{
	   					
	   					if(this.validIds[i])
	   					{
		   					if(this.print_star && (this.measIdToAddStar == i))
		   					{
		   						sendToOutport(this.star_id, (Double) 0.0 );
		   						this.print_star = false;
		   					}
    						sendToOutport(i, this.frame[i]);
	   					}
    				}
	    			
	    			// reset the flag since we already took care of wild points
	    			this.wildPointFrameFound = false;
	    			
	    			clearCachedFrames();
    			}
    			else
    			{
    				for (int i=0; i< this.frame.length; i++) 
    				{
	   					
    					if(this.validIds[i])
    					{
    	   					if(this.print_star && (this.measIdToAddStar == i))
    	   					{
    	   						System.out.println(this.getName() + "::Sending * before <==> " + i + "\n");
    	   						sendToOutport(this.star_id, (Double) 0.0 );
    	   						this.print_star = false;
    	   					}
    						sendToOutport(i, this.frame[i]);
    					}
    				}
    			}
    		}
    		else
    		{
    			double[] tmp_frame = new double[this.measurementsPerFrame];
    			for (int i=0; i< this.frame.length; i++)
    					tmp_frame[i] = this.frame[i];
  

    			//TimeStamp.setTimeInMillis(Double.doubleToLongBits(tmp_frame[0]));
    			//System.out.println(this.getName() + ":: TIME2=" + TimeStampFormat.format(TimeStamp.getTime())+"\n");
    			
    			this.cachedFrames.add(tmp_frame);
    			
    			Iterator<double[]> itr = this.cachedFrames.iterator();
    			while(itr.hasNext())
    			{
    				double[] temp_frame = itr.next();
    				TimeStamp.setTimeInMillis(Double.doubleToLongBits(temp_frame[0]));
    				System.out.println(this.getName() + ":: TIME2=" + TimeStampFormat.format(TimeStamp.getTime())+"\n");
    				
    			}
    			

    			System.out.println(this.getName() + "::Tests FAILED Pressure=" + this.frame[3] +"\n");
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
		
		Iterator<double[]> itr = this.cachedFrames.iterator();
		while(itr.hasNext())
		{
			double[] temp_frame = itr.next();
			    
			// send timestamp
            sendToRejectedPort(0, temp_frame[0]);
            sendToOutport(0, temp_frame[0]);
            
			for(int i=1; i< temp_frame.length; i++)
			{
				
				if(this.validIds[i])
				{
					if(shouldProcessID(i) )
					{
						// write to rejected file
						sendToRejectedPort(i, temp_frame[i]);
						System.out.println(this.getName() + "::Sending * before ==> " + i + "\n");
						sendToOutport(this.star_id, (Double) 0.0 );
						sendToOutport(i, extrapolated_value[i]);
					}
					else
					{
						sendToOutport(i, temp_frame[i]);
					}
				}
			}
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
		System.out.println(this.getName() + "::Sending==> " + id + "\t" + value + "\n");
		long longResult = Double.doubleToLongBits(value);
		writeID(id, currentPort);
		writeMeasurement(longResult, currentPort);
	}

	private void sendToRejectedPort(int id, double value)
	{
		long longResult = Double.doubleToLongBits(value);
		writeID(id, 1);
		writeMeasurement(longResult, 1);
		
		//System.out.println(this.getName() + "::Rejecting==> " + id + "\t" + value + "\n");
	}
	
	private double[] extrapolateMeasurements()
	{
		double[] extrapolated_values = new double[this.measurementsPerFrame];

		// skip timestamp
		for(int i=1; i<this.measurementsPerFrame;i++)
		{
			if(this.shouldProcessID(i))
			{
				if(this.endofFile)
				{
					extrapolated_values[i] = this.lastValidFrame[i];
				}
				else if(! this.validValueFound)
				{
					extrapolated_values[i] = this.frame[i];
				}
				else
				{
					extrapolated_values[i] = (this.frame[i]+this.lastValidFrame[i])/2;
				}
				System.out.println(this.getName() + ":: Extrapolated Before=" + this.frame[i] + "\tAfter=" + extrapolated_values[i] + "Last Valid Measurement="+ this.lastValidFrame[i] + "\n");
			}
			else
			{
				extrapolated_values[i] = this.lastValidFrame[i];
			}
		}
		
		return extrapolated_values;
	}
	
	private void clearFrame()
	{
		this.frame = new double[this.measurementsPerFrame];
		for(int i=0; i< this.measurementsPerFrame; i++)
			this.frame[i] = -1;
		this.measurementsRead = 0;
	}

	private void clearCachedFrames()
	{
		cachedFrames = new ArrayList<double[]>(1);
	}
	
	private void storeMeasurement(int id, double value)
	{
		this.frame[id] = value;
		this.measurementsRead++;
		if(this.received_star)
		{
			this.measIdToAddStar = id;
			this.received_star = false;
		}
	}
	
	private boolean allMeasurementsRead()
	{
		if (this.measurementsRead == this.measurementsToRead)
			return true;
		else
			return false;
	}
	
	private boolean isWildPoint(int id, double value)
	{

		return this.wildPointTest.get(id).execute(value);
		//return false;
	}

}