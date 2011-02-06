
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

public class SystemCMerge
{
	   public static void main( String argv[])
	   {
			/****************************************************************************
			* Here we instantiate three filters.
			****************************************************************************/

		    SourceFilter sourceA = new SourceFilter(null, new PipedOutputStream[]{new PipedOutputStream()}, "/Users/nanchen/Desktop/filter/SubSetA.dat", null);
		    SourceFilter sourceB = new SourceFilter(null, new PipedOutputStream[]{new PipedOutputStream()}, "/Users/nanchen/Desktop/filter/SubSetB.dat", null);
		    
		    //there is two input ports for the merge filter, one port is 0 and another is 1. 
			//there is one output ports for the merge filter, the output port is 0
			PipedInputStream mergeInputPort[] = new PipedInputStream[2];
			mergeInputPort[0] = new PipedInputStream();
			mergeInputPort[1] = new PipedInputStream();			
			MergeFilter mergeFilter = new MergeFilter(mergeInputPort, new PipedOutputStream[]{new PipedOutputStream()}, null);
		    
			
			//HashMap<Integer, WildPointTest> IDsAndWildPointTests = new HashMap<Integer, WildPointTest>();
			//WildPointTest pressureTest = new PressureCWildPointTest();
			//IDsAndWildPointTests.put(ID.PRES, pressureTest);
			//ExtrapolatorFilter extrapolater1 = new ExtrapolatorFilter(new PipedInputStream[]{new PipedInputStream()}, new PipedOutputStream[]{new PipedOutputStream(),new PipedOutputStream()},IDsAndWildPointTests);
			
			SinkFilter sink = new SinkFilter(new PipedInputStream[]{new PipedInputStream()}, null, new int[]{ID.TIME, ID.ALTI, ID.PRES,ID.TEMP, ID.ATTI, ID.VELO}, "/Users/nanchen/Desktop/filter/outputC.txt");
			//SinkFilter rejected = new SinkFilter(new PipedInputStream[]{new PipedInputStream()}, null, new int[]{ID.TIME,ID.PRES}, "/Users/nanchen/Desktop/filter/WildPointsC.txt");

			/****************************************************************************
			* Here we connect the filters starting with the sink filter (Filter 1) which
			* we connect to Filter2 the middle filter. Then we connect Filter2 to the
			* source filter (Filter3).
			****************************************************************************/

			sink.Connect(mergeFilter,0,0); 
			mergeFilter.Connect(sourceA, 0, 0);
			mergeFilter.Connect(sourceB, 1, 0); 			
			
			/****************************************************************************
			* Here we start the filters up. All-in-all,... its really kind of boring.
			****************************************************************************/			
			
			sourceA.start();
			sourceB.start();
			mergeFilter.start();
			sink.start();
			
	   } // main


} // Plumber