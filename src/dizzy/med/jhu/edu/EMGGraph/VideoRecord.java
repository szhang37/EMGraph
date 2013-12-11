package dizzy.med.jhu.edu.EMGGraph;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

public class VideoRecord implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = null;
    private Button startRecording = null;
    //private Button stopRecording = null;
    File video;
    private Camera mCamera = null;

	// Debug printing.
	void dprint(String format, Object ... args) {
		// Prepend our Shimmer address to the debug strings.
		format = String.format("VIDEO %s:", format);
		Log.d("EMGGraph", String.format(format, args));
	}
    
    public void onCreate(Activity activity) {
        dprint("Video starting");
        startRecording = (Button)activity.findViewById(R.id.ButtonVideoRecord);
        mCamera = Camera.open();
        
        surfaceView = (SurfaceView) activity.findViewById(R.id.surface_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public void onResume() {
    	
    }

    public void onPause() {
    	
    }
    
    protected void startRecording()
    {
    	dprint("startRecording()");
    	
    	mrec = new MediaRecorder();  // Works well
        mCamera.unlock();

        mrec.setCamera(mCamera);

        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC); 

        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile("/sdcard/zzzz.3gp"); 

		// Send the StartStreaming command.
		try {
	        mrec.prepare();
		} catch (IOException e) {
			dprint("startRecording(), mrec.prepare() failed.");
		}
        
        mrec.start();
    }

    protected void stopRecording() {
        mrec.stop();
        mrec.release();
        mCamera.release();
    }

    private void releaseMediaRecorder(){
        if (mrec != null) {
            mrec.reset();   // clear recorder configuration
            mrec.release(); // release the recorder object
            mrec = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera != null){
            Parameters params = mCamera.getParameters();
            mCamera.setParameters(params);
        }
        else {
        	Log.i(null , "Camera not available.");
        	//Toast.makeText(getApplicationContext(), "VideoRecord not available!", Toast.LENGTH_LONG).show();
            //finish();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }
}