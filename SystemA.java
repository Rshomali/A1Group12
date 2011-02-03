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

public class SystemA
{
   public static void main( String argv[])
   {
		/****************************************************************************
		* Here we instantiate three filters.
		****************************************************************************/

		SourceFilter source = new SourceFilter("FlightData.dat");
		
		Vector<Integer> keep = new Vector<Integer>();
		keep.add(ID.TEMP);
		keep.add(ID.ATTI);
		FilterOut filterOut = new FilterOut(keep);
		
		HashMap<Integer, ConversionFunction> IDsAndFuncs = new HashMap<Integer, ConversionFunction>();
		ConversionFunction F2C = new Fahrenheit2Celsius();
		ConversionFunction Ft2M = new Feet2Meter();
		IDsAndFuncs.put(ID.TEMP, F2C);
		IDsAndFuncs.put(ID.ATTI, Ft2M);
		Converter converter = new Converter(IDsAndFuncs);
		
		
		SinkFilter sink = new SinkFilter(null);

		/****************************************************************************
		* Here we connect the filters starting with the sink filter (Filter 1) which
		* we connect to Filter2 the middle filter. Then we connect Filter2 to the
		* source filter (Filter3).
		****************************************************************************/

		sink.Connect(converter); // This esstially says, "connect sink input port to converter output port
		converter.Connect(source); // This esstially says, "connect converter intput port to source output port

		/****************************************************************************
		* Here we start the filters up. All-in-all,... its really kind of boring.
		****************************************************************************/

		
		source.start();
		converter.start();
		sink.start();
		
   } // main

} // Plumber