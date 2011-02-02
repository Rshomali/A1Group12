abstract public class StandardFilter extends FilterFramework
{
	protected int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
	protected int IdLength = 4;				// This is the length of IDs in the byte stream

	protected byte databyte = 0;				// This is the data byte read from the stream
	protected int bytesread = 0;				// This is the number of bytes read from the stream
	protected int byteswritten = 0;				// This is the number of bytes writen to the stream
	
	public void run()
	{
		int id;
		long measurement;
		
		while(true)
		{
			try
			{
			
				id = readNextID();
				measurement = readNextMeasurement();
				processIDAndMeasurement(id, Double.longBitsToDouble(measurement));
			}
			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
				break;
			}
		}
		
	}
	
	abstract public void processIDAndMeasurement(int id, double measurement);
	
	public int readNextID() throws EndOfStreamException
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
					databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

					id = id | (databyte & 0xFF);		// We append the byte on to ID...

					if (i != IdLength-1)				// If this is not the last byte, then slide the
					{									// previously appended byte to the left by one byte
						id = id << 8;					// to make room for the next byte we append to the ID

					} // if

					bytesread++;						// Increment the byte count

				} // for

			
		return id;	
	}
	
	public void	writeID(int ID)
	{
		int i;
		
		for(i = IdLength-1; i>=0; --i)
		{
			databyte = (byte) (ID >> (8*i));
			WriteFilterOutputPort(databyte);
			++byteswritten;
		}
	}
	
	public void	writeMeasurement(long measure)
	{
		int i;
		
		for(i = MeasurementLength-1; i>=0; --i)
		{
			databyte = (byte) (measure >> (8*i));
			WriteFilterOutputPort(databyte);
			++byteswritten;
		}
	}
	
	public long readNextMeasurement() throws EndOfStreamException
	{
		long measurement=0;				// This is the word used to store all measurements - conversions are illustrated.
		int i;
		
		for (i=0; i<MeasurementLength; i++ )
				{
					databyte = ReadFilterInputPort();
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