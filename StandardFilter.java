import java.util.Vector;
import java.io.*;


public class StandardFilter extends FilterFramework
{
	protected int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
	protected int IdLength = 4;				// This is the length of IDs in the byte stream

	protected byte databyte = 0;				// This is the data byte read from the stream
	protected int bytesread = 0;				// This is the number of bytes read from the stream
	protected int byteswritten = 0;				// This is the number of bytes writen to the stream
	
	public static int g = 0;
	
	public StandardFilter(PipedInputStream inputReadPort[], PipedOutputStream outputWritePort[], int idToProcess[])
	{
		super(inputReadPort, outputWritePort, idToProcess);
	}
	
	public StandardFilter(PipedInputStream inputReadPort[], PipedOutputStream outputWritePort[], Integer idToProcess[])
	{		
		super(inputReadPort, outputWritePort, idToProcess);
	}
	
	public void run()
	{
		int id;
		long measurement;
		
		while(true)
		{
			currentPort = 0;
			//for(currentPort = 0; currentPort < inputReadPort.length; ++currentPort)
				try
				{
					id = readNextID(currentPort);
					System.out.println("ID: " + id);
					measurement = readNextMeasurement(currentPort);
					++g;
					System.out.println("Measurement: " + Double.longBitsToDouble(measurement));	
					processIDAndMeasurement(id, Double.longBitsToDouble(measurement));
					
				}
				catch (EndOfStreamException e)
				{
					ClosePorts();
																	System.out.println("g: " + g);
					System.out.print( "\n" + this.getName() + "::StandardFilter Exiting; bytes read: " + bytesread );

					return;
				}
		}

		
	}
	
	public void processIDAndMeasurement(int id, double measurement)
	{
	}
	
	public int readNextID(int portNo) throws EndOfStreamException
	{
		
		int id;							// This is the measurement id
		int i;							// This is a loop counter
		
				/***************************************************************************
				// We know that the first data coming to this filter is going to be an ID and
				// that it is IdLength long. So we first decommutate the ID bytes.
				****************************************************************************/

				id = 0;
	
				for (i=0; i<IdLength; i++ )
				{
					databyte = ReadFilterInputPort(portNo);	// This is where we read the byte from the stream...
//System.out.println(String.format("ID %x", databyte));
					id = id | (databyte & 0xFF);		// We append the byte on to ID...

					if (i != IdLength-1)				// If this is not the last byte, then slide the
					{									// previously appended byte to the left by one byte
						id = id << 8;					// to make room for the next byte we append to the ID

					} // if

					bytesread++;						// Increment the byte count

				} // for

			
		return id;	
	}
	
	public void	writeID(int ID, int portNo)
	{
		int i;
		
		for(i = IdLength-1; i>=0; --i)
		{
			databyte = (byte) (ID >> (8*i));
			WriteFilterOutputPort(databyte, portNo);
			++byteswritten;
		}
	}
	
	public void	writeMeasurement(long measure, int portNo)
	{
		int i;
		
		for(i = MeasurementLength-1; i>=0; --i)
		{
			databyte = (byte) (measure >> (8*i));
			WriteFilterOutputPort(databyte, portNo);
			++byteswritten;
		}
	}
	
	public long readNextMeasurement(int portNo) throws EndOfStreamException
	{
		long measurement=0;				// This is the word used to store all measurements - conversions are illustrated.
		int i;
		
		for (i=0; i<MeasurementLength; i++ )
				{
					databyte = ReadFilterInputPort(portNo);
//System.out.println(String.format("measure %x", databyte));
					measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...

					if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
					{												// previously appended byte to the left by one byte
						measurement = measurement << 8;				// to make room for the next byte we append to the
																	// measurement
					} // if

					bytesread++;									// Increment the byte count

				} // for
			return measurement;	
	}
	
}