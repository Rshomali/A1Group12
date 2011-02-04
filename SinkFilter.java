import java.util.*;						// This class is used to interpret time words
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.

public class SinkFilter extends FilterFramework
{	
	
	String fileName;
	public SinkFilter(PipedInputStream[] inputReadPort,
			PipedOutputStream[] outputWritePort, int[] idToProcess, String fileName) {
		super(inputReadPort, outputWritePort, idToProcess);
		this.fileName = fileName;
		// TODO Auto-generated constructor stub
	}

	public void run()
    {

		Calendar TimeStamp = Calendar.getInstance();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy::dd::hh:mm:ss");
		DecimalFormat temperaturFormat = new DecimalFormat("000.00000");
		DecimalFormat altitudeFormat = new DecimalFormat("000000.00000");
		DecimalFormat pressureFormat = new DecimalFormat("00.000000");
		
		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		byte databyte = 0;				// This is the data byte read from the stream
		int bytesread = 0;				// This is the number of bytes read from the stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id
		int i;							// This is a loop counter
		
		int byteswritten = 0;				// Number of bytes written to the stream.
		DataOutputStream out = null;			// File stream reference.

		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.println( "\n" + this.getName() + "::Sink Reading ");
		try {
			out = new DataOutputStream(new FileOutputStream(fileName));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		while (true)
		{
			try
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
				
				if ( id == 0 )
				{
					TimeStamp.setTimeInMillis(measurement);
					System.out.println();
					System.out.print( TimeStampFormat.format(TimeStamp.getTime()));
				} 
				
				if(id==2)
					System.out.print("        ID = " + id +"   " +altitudeFormat.format(Double.longBitsToDouble(measurement)));
				
				if(id==3)  
					System.out.print("        ID = " + id +"   " +pressureFormat.format(Double.longBitsToDouble(measurement)));
					
				if(id==4)
					System.out.print("        ID = " + id +"   " +temperaturFormat.format(Double.longBitsToDouble(measurement)));
					
				//System.out.print( "\n" );
				
				
				
				byte[] bt = new byte[12];
				bt[0] = (byte) (0xff & id);
				bt[1] = (byte) (0xff &( id >> 8));
				bt[2] = (byte) (0xff &( id >> 16));
				bt[3] = (byte) (0xff &( id >> 24));		
					
				bt[4] = (byte) (0xff & measurement);
				bt[5] = (byte) (0xff &( measurement >> 8));
				bt[6] = (byte) (0xff &( measurement >> 16));
				bt[7] = (byte) (0xff &( measurement >> 24));
				bt[8] = (byte) (0xff &( measurement >> 32));
				bt[9] = (byte) (0xff &( measurement >> 40));
				bt[10] = (byte) (0xff &( measurement >> 48));
				bt[11] = (byte) (0xff &( measurement >> 56));
				out.writeByte(bt[3]);
				out.writeByte(bt[2]);
				out.writeByte(bt[1]);
				out.writeByte(bt[0]);

				out.writeByte(bt[11]);
				out.writeByte(bt[10]);
				out.writeByte(bt[9]);
				out.writeByte(bt[8]);
				out.writeByte(bt[7]);
				out.writeByte(bt[6]);
				out.writeByte(bt[5]);
				out.writeByte(bt[4]);
							
			} // try

		
						
			
			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
				break;
			} // catch
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
   } // run
} // SingFilter