package dizzy.med.jhu.edu.EMGGraph;

import java.util.Timer;
import java.util.TimerTask;
//import java.util.Random;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
//import java.io.BufferedInputStream;
import java.util.Date;
//import java.text.DateFormat;

//import dizzy.med.jhu.edu.EMGGraph.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
//import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.view.MotionEvent;

import android.util.Log;
//import android.Manifest;

/**
 * Receive data from multiple Shimmers, for display and saving to data file.
 *  
 * @author Dale Roberts 11/2012
 *
 */
public class EMGGraph extends Activity {
//    private static final int REQUEST_ENABLE_BT = 3;
    private static final String TAG = "EMGGraph";
	
	private GraphView mGraph; 
	private TextView mValueTV;
	private Timer UITimer;

	private String dataFileName = null;
	private OutputStream dataStream = null;

	private Button ButtonNewFile;
	
//	private Shimmer[] shimmer = new Shimmer[6];
	
	// Create the array of Shimmer devices.
	// Parameters:   MAC_ADDRESS, sample period in mSec, #channels
	private static Shimmer[] shimmers = {

		/*
		// Six Accel @256 to test synchronization.
		new Shimmer("00:06:66:46:9A:9A", 4, 3),
		new Shimmer("00:06:66:46:B5:C7", 4, 3),
		new Shimmer("00:06:66:43:A8:53", 4, 3),
		new Shimmer("00:06:66:46:BC:9D", 4, 3),
		new Shimmer("00:06:66:46:BD:6D", 4, 3),
		new Shimmer("00:06:66:46:BC:A0", 4, 3),
	*/
		
			// Two Accel @100Hz
			new Shimmer("00:06:66:46:9A:9A", 20, 6),
			new Shimmer("00:06:66:46:B5:C7", 20, 6),
//			new Shimmer("00:06:66:43:A8:53", 20, 6),
 			
 			// Four EMG @500Hz
			new Shimmer("00:06:66:46:BC:9D", 2, 1),
			new Shimmer("00:06:66:46:BD:6D", 2, 1),
//			new Shimmer("00:06:66:46:BC:A0", 2, 1),
//			new Shimmer("00:06:66:46:BD:15", 2, 4),

//			new Shimmer("00:06:66:43:B4:58"),
//			new Shimmer("00:06:66:43:A8:5A"),
//			,new Shimmer("00:06:66:43:A8:4D")
			};

	// VIDEO
	VideoRecord VRecord = null;
	
	// Debug printing - adds the Shimmer MAC address to the debug string.
	private void dprint(String format, Object ... args) {
		// Prepend our Shimmer address to the debug strings.
		format = String.format("MAIN: %s", format);
		Log.d(TAG, String.format(format, args));
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dprint("In onCreate().");
        
        setContentView(R.layout.main);
        
        // get handles to Views defined in our layout file
        mGraph = (GraphView)findViewById(R.id.graph);
        mValueTV = (TextView) findViewById(R.id.value);

//        mValueTV.setText(String.format("Test number %d", 123));
        // year_month|day_hr|min|sec
        mValueTV.setText("<No File>");
        
//        mValueTV.setText(String.format("%4d_%02d%02d_%02d%02d%02d",
//        		date.getYear()+1900, date.getMonth()+1, date.getDate(),
//        		date.getHours(), date.getMinutes(), date.getSeconds() ));
        ButtonNewFile = (Button)findViewById(R.id.button_NewFile);
        ButtonNewFile.setOnTouchListener(ButtonNewFileListener);
        
        // VIDEO
        dprint("Creating VideoRecord instance.");
        VRecord = new VideoRecord();
        VRecord.onCreate(this);
        }

    // Dump some of our configuration strings into the header.
    private void WriteDataFileHeader() {
    	if(dataStream == null)
    		return;
    	
    	PrintStream ps = new PrintStream(dataStream);
		ps.println(String.format("BYTES_PER_SAMPLE %d", Shimmer.BYTES_PER_SAMPLE));
		ps.println("Shimmers: MAC, SamplePeriod, NChannels");
		int shimnum = 1;
		for(Shimmer s: shimmers) {
			ps.println(String.format("Shimmer%d: %s, %d, %d", shimnum, s.MACAddress, s.ShimmerSamplePeriod_ms, s.NChannels));
			++shimnum;
		}

		// Terminate header string with zero byte.
		try{dataStream.write(0);}
		catch(IOException e) {}
    }
    
	private void NewDataFile() {
		
		// First close old file, if it is open.
		if(dataStream != null) {
			CloseDataFile();
		}
		
		mValueTV.setText("<No File>");

        Date date = new Date();
		dataFileName = String.format("%4d_%02d%02d_%02d%02d%02d_EMGdata",
				date.getYear()+1900, date.getMonth()+1, date.getDate(),
				date.getHours(), date.getMinutes(), date.getSeconds());

//		File newdir = new File(Environment.getExternalStorageDirectory()
//				+ File.separator + "NASA");
//				newdir.mkdir();

		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "NASA"
				+ File.separator + dataFileName);

		Log.w(TAG, "Trying to write out to file.");

		try {
	    	file.createNewFile();
    		dataStream = new FileOutputStream(file);
			//PrintStream ps = new PrintStream(fo);
    		//ps.println("TEST string to file?? Oh… Hello World!");
			//fo.close();
		}
	    catch(FileNotFoundException e) {
	    	Log.e(TAG, "File Not Found!!!", e);
	    	return;}
	    catch(IOException e) {
	    	Log.e(TAG, "IO Exception writing to file.", e);
	    	return;}

		mValueTV.setText(dataFileName);
		WriteDataFileHeader();
	}

	private void CloseDataFile()
	{
		if(dataStream == null)
			return;
		
		try {dataStream.close();}
		catch(IOException e) {}
		dataStream = null;
		dataFileName = null;
		mValueTV.setText("<No File>");
		
		// Trying to get it to flush the file out so it shows up immediately on the PC.
		// THIS WORKS! Files are updated immediately in Windows.
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://"
                        + Environment.getExternalStorageDirectory()
                        + "/NASA")));
	}
	
	// For Open New File button.
	//
	// This will STOP streaming when the button is pressed down, and START streaming
	// when the button is released. Pretty cool. By returning FALSE instead of TRUE,
	// it allows the Android OS to do the button highlighting when it is pressed.
	// If we use "false" it will assume we "consumed" the event, and the button will
	// not be highlighted.
	private View.OnTouchListener ButtonNewFileListener = new View.OnTouchListener() {
		public boolean onTouch(View view, MotionEvent motion) {
			switch (motion.getAction()) {
	    		case MotionEvent.ACTION_DOWN:
	    	  		//Shimmer.onStopStreaming();
	  	    		break;
	  	    	case MotionEvent.ACTION_UP:
	  	    		NewDataFile();
	  	    		//Shimmer.onStartStreaming();
	  	    		break;
	  	    	}
	  	    return false;
	  	}
	};	

	public void onButtonCloseFile(View view)
    {
    	Log.w(TAG, "Button Close File!!!");
    	CloseDataFile();
    }
    
    private void TimerMethod() {
		this.runOnUiThread(UITimerMethod);
    }

    // Reformat the Packet[] integers into swapped bytes for writing out to the data file.
    byte[] bytes = new byte[Shimmer.BYTES_PER_SAMPLE];
    byte[] GetPacketBytes(int[] Packet, int UnitNum)
    {
    	for(int b=0; b<14; b+=2) {
    		bytes[b] = (byte)Packet[b/2];
    		bytes[b+1] = (byte)(Packet[b/2] >> 8);
    	}
    	// Save Shimmer unit number in last slot.
    	bytes[15] = (byte)UnitNum;
    	bytes[14] = 0;
    	return bytes;
    }
    
    // The timer method is called 
    private Runnable UITimerMethod = new Runnable() {
    	public void run() {
        	int shimnum=0;
        	int[] Packet;
        	
        	for(Shimmer s: shimmers) {
        		if(s.PacketQueue.size() > 0) {
        			do {
        				Packet = s.PacketQueue.poll();
        				
						// For debugging the queue.
        				//Packet[4] = s.PacketQueue.size();
						
        				if(dataStream != null) {
        					try{dataStream.write(GetPacketBytes(Packet, shimnum));}
        					catch(IOException e){}
        				}
        			}
        			while(s.PacketQueue.size() > 0);
        				
            		if(shimnum >= 6)
            			break;
        			mGraph.addDataPoint(Packet,shimnum);
        		}
        		shimnum = shimnum+1;
        	}
    	}
    };

	@Override
	protected void onStart() {
		super.onStart();
		dprint("In onStart()...");
		Shimmer.onStart(shimmers);
		dprint("done onStart().");
	}

	@Override
	public void onResume() {
		dprint("In onResume(), before super.onResume()...");
		super.onResume();
		dprint("In onResume()...");
		Shimmer.onResume();
		
		dprint("Setting up timer...");
		// Set up a timer to update the strip-chart graph.
        UITimer = new Timer();
        UITimer.scheduleAtFixedRate(
        		new TimerTask() {public void run() { TimerMethod();}},
        		0, 20);

        VRecord.startRecording();
        dprint("onResume() completed.");
	}

	@Override
	public void onPause() {
		dprint("onPause() entered...");
		super.onPause();
		Shimmer.onPause();
		UITimer.cancel();		
        VRecord.stopRecording();
		dprint("onPause() completed.");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Shimmer.onStop();
	}
}
