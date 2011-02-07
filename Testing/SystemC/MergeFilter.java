import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MergeFilter extends FilterFramework {

	public MergeFilter(PipedInputStream[] inputReadPort,
			PipedOutputStream[] outputWritePort, int idToProcess[]) {
		super(inputReadPort, outputWritePort, idToProcess);
	}

	public void run() {
		int byteswritten = 0; 
		int bytesread = 0;
		
		byte databyte1 = 0; 
		byte databyte2 = 0;

		int MeasurementLength = 8;	
		long measurement1;
		long measurement2;
		int IdLength = 4;
		int id1 = 0;
		int id2 = 0;		
		int i; 

		System.out.println("\n" + this.getName() + "::Merge Reading ");

		//start comparison. Set the flag needReadBuffer1 and needReadBuffer2 as true at first. When the flag is true,
		//the program will read the next time id and measurement from the input port. Compare the time measurement of 
		//port 1 and port 2, write the frame of the smaller time measurement to the output port and then set the flag
		//of the sent frame as true, then the program will read the next 
		boolean needReadBuffer1 = true;
		boolean needReadBuffer2 = true;
		measurement1 = 0;
		measurement2 = 0;
		while (true) {
			try {			
				//if the flag needReadBuffer is true, the program will read the next id and measurement of time
				//from the input port
				if (needReadBuffer1) {
					id1 = getID(0);
					measurement1 = getMeasurement(0);
				}
				if (needReadBuffer2) {
					id2 = getID(1);
					measurement2 = getMeasurement(1);
				}			

				//compare the time of two input ports. write the whole frame of the smaller time to the output port 
				//and mark the needReadBuffer as true. 
				if (id1 == 0 && id2 == 0 && measurement1 <= measurement2) {
					for(i = IdLength-1; i>=0; --i)
					{
						WriteFilterOutputPort((byte) (id1 >> (8*i)), 0);
						++byteswritten;
					}
					for(i = MeasurementLength-1; i>=0; --i)
					{
						WriteFilterOutputPort((byte) (measurement1 >> (8*i)), 0);
						++byteswritten;
					}
					
					for (i = 0; i < 60; i++) {
						databyte1 = ReadFilterInputPort(0);
						WriteFilterOutputPort(databyte1, 0);
						byteswritten++;
					}
					needReadBuffer2 = false;
					needReadBuffer1 = true;
				}

				if (id1 == 0 && id2 == 0 && measurement1 > measurement2) {
					for(i = IdLength-1; i>=0; --i)
					{
						WriteFilterOutputPort((byte) (id2 >> (8*i)), 0);
						++byteswritten;
					}
					for(i = MeasurementLength-1; i>=0; --i)
					{
						WriteFilterOutputPort((byte) (measurement2 >> (8*i)), 0);
						++byteswritten;
					}

					for (i = 0; i < 60; i++) {
						databyte2 = ReadFilterInputPort(1);
						WriteFilterOutputPort(databyte2, 0);
						byteswritten++;
					}
					needReadBuffer1 = false;
					needReadBuffer2 = true;
				}
			}

			catch (EndOfStreamException e) {
				ClosePorts();
				System.out.print("\n" + this.getName()
						+ "::Middle Exiting; bytes read: " + bytesread
						+ " bytes written: " + byteswritten);
				break;
			} // catch
		}
		
		//When one of the input stream is empty, write the all the other input port's frame 
		//to the output port.
		while (true) {
			try {
				WriteFilterOutputPort(ReadFilterInputPort(0), 0);
				byteswritten++;
			} // try
			catch (EndOfStreamException e) {
				ClosePorts();
				System.out.print("\n" + this.getName()  + "::Merge Exiting; bytes read: " + bytesread  + " bytes written: " + byteswritten);
				break;
			} // catch
		} // while
		while (true) {
			try {
				WriteFilterOutputPort(ReadFilterInputPort(1), 0);
				byteswritten++;
			} // try
			catch (EndOfStreamException e) {
				ClosePorts();
				System.out.print("\n" + this.getName()  + "::Merge Exiting; bytes read: " + bytesread  + " bytes written: " + byteswritten);
				break;
			} // catch
		} // while

	}

	private int getID(int portNum) {
		byte databyte;
		int i = 0;
		int IdLength = 4;
		int id = 0;
		for (i = 0; i < IdLength; i++) {
			try {
				databyte = ReadFilterInputPort(portNum);
				id = id | (databyte & 0xFF);
				if (i != IdLength - 1) {
					id = id << 8;
				}
			} catch (EndOfStreamException e) {
				break;
			}
		}
		return id;
	}

	
	private long getMeasurement(int portNum) {
		int i = 0;
		byte databyte;
		long measurement = 0;
		int MeasurementLength = 8;

		for (i = 0; i < MeasurementLength; i++) {
			try {
				databyte = ReadFilterInputPort(portNum);
				measurement = measurement | (databyte & 0xFF);
				if (i != MeasurementLength - 1) {
					measurement = measurement << 8;
				}
			} catch (EndOfStreamException e) {
				break;
			}
		}
		return measurement;
	}
}