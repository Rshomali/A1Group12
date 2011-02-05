/******************************************************************************************************************
* File:Plumber.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example to illstrate how to use the PlumberTemplate to create a main thread that
* instantiates and connects a set of filters. This example consists of three filters: a source, a middle filter
* that acts as a pass-through filter (it does nothing to the data), and a sink filter which illustrates all kinds
* of useful things that you can do with the input stream of data.
*
* Parameters: 		None
*
* Internal Methods:	None
*
******************************************************************************************************************/

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;

public class SystemB
{
	   public static void main( String argv[])
	   {
			/****************************************************************************
			* Here we instantiate three filters.
			****************************************************************************/

		    SourceFilter source = new SourceFilter(null, new PipedOutputStream[]{new PipedOutputStream()}, "FlightData.dat", null);
	
			FilterOut filterOut = new FilterOut(new PipedInputStream[]{new PipedInputStream()}, new PipedOutputStream[]{new PipedOutputStream()}, new int[]{ID.TIME, ID.ALTI, ID.PRES,ID.TEMP});
			
			HashMap<Integer, ConversionFunction> IDsAndFuncs = new HashMap<Integer, ConversionFunction>();
			ConversionFunction F2C = new Fahrenheit2Celsius();
			ConversionFunction Ft2M = new Feet2Meter();
			IDsAndFuncs.put(ID.TEMP, F2C);
			IDsAndFuncs.put(ID.ALTI, Ft2M);
			Converter converter = new Converter(new PipedInputStream[]{new PipedInputStream()}, new PipedOutputStream[]{new PipedOutputStream()}, IDsAndFuncs);

			
			HashMap<Integer, WildPointTest> IDsAndWildPointTests = new HashMap<Integer, WildPointTest>();
			WildPointTest pressureTest = new PressureBWildPointTest();
			IDsAndWildPointTests.put(ID.PRES, pressureTest);
			
			ExtrapolatorFilter extrapolater = new ExtrapolatorFilter(new PipedInputStream[]{new PipedInputStream()}, new PipedOutputStream[]{new PipedOutputStream(),new PipedOutputStream()},IDsAndWildPointTests);
			
			SinkFilter sink = new SinkFilter(new PipedInputStream[]{new PipedInputStream()}, null, new int[]{ID.TIME, ID.ALTI, ID.PRES,ID.TEMP}, "OutputB.dat");
			SinkFilter rejected = new SinkFilter(new PipedInputStream[]{new PipedInputStream()}, null, new int[]{ID.TIME,ID.PRES}, "WildPoints.dat");

			/****************************************************************************
			* Here we connect the filters starting with the sink filter (Filter 1) which
			* we connect to Filter2 the middle filter. Then we connect Filter2 to the
			* source filter (Filter3).
			****************************************************************************/


			sink.Connect(extrapolater,0,0); // This essentially says, "connect sink input port to extrapolater first output port
			extrapolater.Connect(converter,0,0); // This essentially says, "connect extrapolater input port to converter output port
			rejected.Connect(extrapolater,0,1); // This essentially says, "connect rejected input port to extrapolater second output port
			converter.Connect(filterOut, 0, 0); // This essentially says, "connect converter input port to filterOut output port
			filterOut.Connect(source, 0, 0); // This essentially says, "connect filterOut input port to source output port
			
			
			/****************************************************************************
			* Here we start the filters up. All-in-all,... its really kind of boring.
			****************************************************************************/

			
			
			source.start();
			filterOut.start();
			converter.start();
			extrapolater.start();
			sink.start();
			rejected.start();
			
			
	   } // main


} // Plumber