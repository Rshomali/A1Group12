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

import java.util.HashMap;
import java.util.Vector;
import java.io.*;

public class SystemA
{
   public static void main( String argv[])
   {
		/****************************************************************************
		* Here we instantiate three filters.
		****************************************************************************/

		SourceFilter source = new SourceFilter(null, new PipedOutputStream[]{new PipedOutputStream()}, "FlightData.dat", null);
		
		FilterOut filterOut = new FilterOut(new PipedInputStream[]{new PipedInputStream()}, new PipedOutputStream[]{new PipedOutputStream()}, new int[]{ID.TEMP, ID.ATTI});
		
		HashMap<Integer, ConversionFunction> IDsAndFuncs = new HashMap<Integer, ConversionFunction>();
		ConversionFunction F2C = new Fahrenheit2Celsius();
		ConversionFunction Ft2M = new Feet2Meter();
		IDsAndFuncs.put(ID.TEMP, F2C);
		IDsAndFuncs.put(ID.ATTI, Ft2M);
		Converter converter = new Converter(new PipedInputStream[]{new PipedInputStream()}, new PipedOutputStream[]{new PipedOutputStream()}, IDsAndFuncs);
		
		
		SinkFilter sink = new SinkFilter(new PipedInputStream[]{new PipedInputStream()}, null, null, "output.dat");

		/****************************************************************************
		* Here we connect the filters starting with the sink filter (Filter 1) which
		* we connect to Filter2 the middle filter. Then we connect Filter2 to the
		* source filter (Filter3).
		****************************************************************************/

		sink.Connect(converter, 0, 0); // This esstially says, "connect sink input port to converter output port
		converter.Connect(source, 0, 0); // This esstially says, "connect converter intput port to source output port
	//	filterOut.Connect(source, 0, 0); // This esstially says, "connect sink input port to converter output port
		/****************************************************************************
		* Here we start the filters up. All-in-all,... its really kind of boring.
		****************************************************************************/

		
		source.start();
	//	filterOut.start();
		converter.start();
		sink.start();
		
   } // main

} // Plumber