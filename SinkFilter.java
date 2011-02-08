
import java.util.*;						// This class is used to interpret time words
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.

public class SinkFilter extends FilterFramework
{	
	
	DataOutputStream out = null;			// File stream reference.
	String fileName;
	public SinkFilter(PipedInputStream[] inputReadPort,
			PipedOutputStream[] outputWritePort, int[] idToProcess, String fileName) {
		super(inputReadPort, outputWritePort, idToProcess);
		this.fileName = fileName;
	}
	
	public void writePlain(Vector<Integer> IDs,	Vector<Long> measurements)
	{
	Calendar TimeStamp = Calendar.getInstance();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy::dd::hh:mm:ss");
		DecimalFormat temperaturFormat = new DecimalFormat("000.00000");
		DecimalFormat altitudeFormat = new DecimalFormat("000000.00000");
		DecimalFormat pressureFormat = new DecimalFormat("00.000000");
		DecimalFormat otherFormat = new DecimalFormat("00000.000000");
		
	boolean print_star = false;
	int star_id = 42;
	int id;
	long measurement;
	for(int j=0; j<IDs.size(); ++j)
	{
	try{
				id = IDs.elementAt(j);
				measurement = measurements.elementAt(j);
		if ( id == 0 )
				{
					TimeStamp.setTimeInMillis(measurement);
					System.out.println();
					System.out.print( TimeStampFormat.format(TimeStamp.getTime()));
					if(print_star)
					{
						out.writeUTF("\n"+TimeStampFormat.format(TimeStamp.getTime())+"*\t\t\t\t\t\t");
						print_star = false;
					}
					else
					{
						out.writeUTF("\n"+TimeStampFormat.format(TimeStamp.getTime())+"\t\t\t\t\t\t");
					}
				} 				
				if(id==2)
				{
					System.out.print("      ID = " + id +"   " +altitudeFormat.format(Double.longBitsToDouble(measurement)));
					if(print_star)
					{
						out.writeUTF(altitudeFormat.format(Double.longBitsToDouble(measurement))+"*\t\t\t\t\t\t");
						print_star = false;
					}
					else
					{
						out.writeUTF(altitudeFormat.format(Double.longBitsToDouble(measurement))+"\t\t\t\t\t\t");
					}
				}				
				if(id==3)  
				{
					System.out.print("      ID = " + id +"   " +pressureFormat.format(Double.longBitsToDouble(measurement)));
					if(print_star)
					{
						out.writeUTF(pressureFormat.format(Double.longBitsToDouble(measurement))+"*\t\t\t\t\t\t");
						print_star = false;
					}
					else
					{
						out.writeUTF(pressureFormat.format(Double.longBitsToDouble(measurement))+"\t\t\t\t\t\t");
					}
					
				}	
				if(id==4)
				{
					System.out.print("      ID = " + id +"   " +temperaturFormat.format(Double.longBitsToDouble(measurement)));
					if(print_star)
					{
						out.writeUTF(temperaturFormat.format(Double.longBitsToDouble(measurement))+"*\t\t\t\t\t\t");
						print_star = false;
					}
					else
					{
						out.writeUTF(temperaturFormat.format(Double.longBitsToDouble(measurement))+"\t\t\t\t\t\t");
					}
				}
				if(id==1|id==5)
				{
					System.out.print("      ID = " + id +"   " +otherFormat.format(Double.longBitsToDouble(measurement)));
					if(print_star)
					{
						out.writeUTF(otherFormat.format(Double.longBitsToDouble(measurement))+"*\t\t\t\t\t\t");
						print_star = false;
					}
					else
					{
						out.writeUTF(otherFormat.format(Double.longBitsToDouble(measurement))+"\t\t\t\t\t\t");
					}
				}
				if(id == star_id)
				{
					print_star = true;
				}
				}
				catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				}
	}

	public void run()
    {

		
		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		byte databyte = 0;				// This is the data byte read from the stream
		int bytesread = 0;				// This is the number of bytes read from the stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id
		int i;							// This is a loop counter
		

		

		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.println( "\n" + this.getName() + "::Sink Reading ");
		try {
			out = new DataOutputStream(new FileOutputStream(fileName));
			
				//sort the idToProcess array in order to 
				//print the title of the column in the same order with the contents
				int temp=0;
				for(i=0; i<this.getIdToProcess().length;i++)
				for(int j=i+1; j<this.getIdToProcess().length; j++)
				{
					if(this.getIdToProcess()[i]>this.getIdToProcess()[j])
					{
						temp = this.getIdToProcess()[i];
						this.getIdToProcess()[i] = this.getIdToProcess()[j];
						this.getIdToProcess()[j] = temp;
					}
				}
		
			for(i=0; i<this.getIdToProcess().length; i++)
				if(this.getIdToProcess()[i]==0)
				{
					out.writeUTF("Time:\t\t\t\t\t\t");
					break;
				}
				
			for(i=0; i<this.getIdToProcess().length;i++)
				if(this.getIdToProcess()[i]==4)
				{
					out.writeUTF("Temperature(C):\t\t\t\t\t\t");
					break;
				}
				
				//write the title of each coloum
				for(i=0; i<this.getIdToProcess().length;i++)
				{

				if(this.getIdToProcess()[i]==1)
					out.writeUTF("Velocity:\t\t\t\t\t\t");
				if(this.getIdToProcess()[i]==2)
					out.writeUTF("Altitude(m):\t\t\t\t\t\t");
				if(this.getIdToProcess()[i]==3)
					out.writeUTF("Pressure(psi):\t\t\t\t\t\t");
				if(this.getIdToProcess()[i]==5)
					out.writeUTF("Attitude:\t\t\t\t\t\t");
			}
			out.writeUTF("\n----------------------------------" +
					"---------------------------------------------" +
					"---------------------------------------------------\n");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Vector<Integer> IDs = new Vector<Integer>();
		Vector<Long> measurements = new Vector<Long>();
		int lastID=-1;
		long lastMeasurement = -1;
		
		
		while (true)
		{
			IDs.clear();
			measurements.clear();
			if(lastID != -1)
				IDs.add(new Integer(lastID));
			if(lastMeasurement != -1)
				measurements.add(new Long(lastMeasurement));
			try
			{
				
				while(true)
				{
					
					id = 0;
					for (i=0; i<IdLength; i++ )
					{
						databyte = ReadFilterInputPort(0);	// This is where we read the byte from the stream...
						id = id | (databyte & 0xFF);		// We append the byte on to ID...
						if (i != IdLength-1)				// If this is not the last byte, then slide the
						{									// previously appended byte to the left by one byte
							id = id << 8;					// to make room for the next byte we append to the ID
						} // if
						bytesread++;						// Increment the byte count
					} // for

					measurement = 0;
					for (i=0; i<MeasurementLength; i++ )
					{
						databyte = ReadFilterInputPort(0);
						measurement = measurement | (databyte & 0xFF);

						if (i != MeasurementLength-1)																
							measurement = measurement << 8;				
					
						bytesread++;				
					} 
					if(id != 42 && IDs.contains(id))
					{
						lastID = id;
						lastMeasurement = measurement;
						break;
					}
					else
					{
						IDs.add(new Integer(id));
						measurements.add(new Long(measurement));
					}
				}
				//put temperature in front of all other measures except time
				if(IDs.contains(ID.TEMP) && IDs.size() > 1)
				{
					System.out.println("index of temp is: "+ IDs.indexOf(ID.TEMP));
					System.out.println("measurements: "+ measurements);
					Integer tmp = IDs.elementAt(1);
					Long tmp0 = measurements.elementAt(1);
					int index = IDs.indexOf(ID.TEMP);
					
					IDs.set(1, IDs.elementAt(index));
					measurements.set(1, measurements.elementAt(index));
					IDs.set(index, tmp);
					measurements.set(index, tmp0);
					
					
					//Collections.swap(IDs, 1, );
					//Collections.swap(measurements, 1, IDs.indexOf(ID.TEMP));
										System.out.println("measurements@@@: "+ measurements);
				}
					
				writePlain(IDs, measurements);
				
				
			} // try
			
			catch (EndOfStreamException e)
			{
			if(lastID != -1)
				IDs.add(new Integer(lastID));
			if(lastMeasurement != -1)
				measurements.add(new Long(lastMeasurement));
			if(IDs.contains(ID.TEMP) && IDs.size() > 1)
				{
					System.out.println("index of temp is: "+ IDs.indexOf(ID.TEMP));
					System.out.println("measurements: "+ measurements);
					Integer tmp = IDs.elementAt(1);
					Long tmp0 = measurements.elementAt(1);
					int index = IDs.indexOf(ID.TEMP);
					
					IDs.set(1, IDs.elementAt(index));
					measurements.set(1, measurements.elementAt(index));
					IDs.set(index, tmp);
					measurements.set(index, tmp0);
					
					
					//Collections.swap(IDs, 1, );
					//Collections.swap(measurements, 1, IDs.indexOf(ID.TEMP));
										System.out.println("measurements@@@: "+ measurements);
				}
			writePlain(IDs, measurements);
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
				break;
			} // catch
				
		}
   } // run
} // SingFilter