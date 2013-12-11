/*
  Copyright (c) 2009 Bonifaz Kaufmann. 
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package dizzy.med.jhu.edu.EMGGraph;

import android.content.Context;
//import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {
	private Paint mPaint = new Paint();
	private int[] mColor = new int[6];
	private float mLastX;
	private float[] mLastValue = new float[36];
	private int[] mvalue = new int[36];
//	private int shimnum;
	private float mWidth, mHeight;
	
	// System.out.println(mWidth);
	
	GraphView(Context context) {
		super(context);
		init();
	}

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init(){
    	mColor[0] = Color.rgb(255, 0, 0);  // RED
    	mColor[1] = Color.rgb(255, 128, 0); // orange, i hope
    	mColor[2] = Color.rgb(255, 255, 0); // YELLOW
    	mColor[3] = Color.rgb(0, 255, 0);  // GREEN
    	mColor[4] = Color.rgb(0,0,255);   // BLUE
    	mColor[5] = Color.rgb(255,0,255); // magenta? (purple)

    	mLastX = 0;
    	mPaint.setStrokeWidth(2);
    }
    
    public void addDataPoint(int[] value, int ShimNum) {
//    	mvalue = value;
//    	shimnum = ShimNum;
    	System.arraycopy(value,  1, mvalue, ShimNum*6, 6);
		invalidate((int)mLastX, 0, (int)mLastX+20, (int)mHeight);
    }

    public void onDraw(Canvas canvas) {
        float newX = mLastX + 1;
        float v;
    
        // Draw all 36 channels.
        for(int i=0; i < 36; ++i) {
//        for(int i=0; i < 6; ++i) {

        	// For now, don't display rate sensor channels, i.e., 3, 4, 5, ...  9, 10, 11, etc. 
        	if((i/3) % 2 == 1)
        		continue;
        	
//        	int ii=Math.min(17,i);
//        	v = mYOffset + ((i/6) * 300 - 300) - (((i/3)%2)*100) + ((i%6)*10) + (float)value[i] * mScale;
//        	v = mHeight/2 + (((float)i/6) * 300 - 300) - ((( (float)i/3)%2)*100) - (float)mvalue[i] * (float)0.2;
//        	v = (float) (mHeight/2 + (shimnum * 180 - 500) + ((( (float)i/3)%2)*70) - (float)mvalue[i] * (float)0.2);

//        	v = (float) (mHeight/2 + (Math.floor((float)i/6) * 180 - 500) + ((( (float)i/3)%2)*70) - (float)mvalue[i] * (float)0.2);
        	v = (float) (mHeight/2 + (Math.floor((float)i/6) * 180 - 500) - (float)mvalue[i] * (float)0.2);
        	
        	mPaint.setColor(mColor[i%6]);
        	canvas.drawLine(mLastX, mLastValue[i], newX, v, mPaint);
        	mLastValue[i] = v;
        }

        mPaint.setColor(Color.rgb(0, 0, 0));
        canvas.drawLine((newX+20)%mWidth, 0, (newX+20)%mWidth, mHeight, mPaint);

        if(newX >= mWidth)
        	newX = 0;
        mLastX = newX;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    
}


/*

public class GraphView extends View {

	private Bitmap  mBitmap;
	private Paint   mPaint = new Paint();
    private Canvas  mCanvas = new Canvas();
    
	private float   mSpeed = 1.0f;
	private float   mLastX;
    private float   mScale = (float)0.2;
    private float[]   mLastValue = new float[18];
    private float   mYOffset;
    private int[]     mColor = new int[6];
    private float   mWidth, mHeight;
    private float   maxValue = 1024f;
    
    public GraphView(Context context) {
        super(context);
        init();
    }
    
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init(){
//    	mColor = Color.argb(192, 64, 128, 64);
    	mColor[0] = Color.rgb(255, 0, 0);  // RED
    	mColor[1] = Color.rgb(0, 255, 0);  // GREEN
    	mColor[2] = Color.rgb(255, 255, 0); // YELLOW
    	mColor[3] = Color.rgb(0,0,255);   // BLUE
    	mColor[4] = Color.rgb(255,0,255); // magenta? (purple)
    	mColor[5] = Color.rgb(255, 128, 0); // orange, i hope
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(2);
    }
    
    public void addDataPoint(int[] value){
        final Paint paint = mPaint;
        float newX = mLastX + mSpeed;
        float v;
        
        // Draw all 18 channels.
        for(int i=0; i < 18; ++i) {
//        	v = mYOffset + ((i/6) * 300 - 300) - (((i/3)%2)*100) + ((i%6)*10) + (float)value[i] * mScale;
        	v = mYOffset + ((i/6) * 300 - 300) - (((i/3)%2)*100) + (float)value[i] * mScale;
            paint.setColor(mColor[i%6]);
        	mCanvas.drawLine(mLastX, mLastValue[i], newX, v, paint);
        	mLastValue[i] = v;
        }

        paint.setColor(Color.rgb(0, 0, 0));
        mCanvas.drawLine((newX+20)%mWidth, 0, (newX+20)%mWidth, mHeight, paint);
        
        mLastX += mSpeed;
		invalidate();
    }
    
    public void setMaxValue(int max){
    	maxValue = max;
    	//mScale = (float)-0.05;// (mYOffset * (1.0f / maxValue));
    }
    
    public void setSpeed(float speed){
    	mSpeed = speed;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(Color.rgb(0, 0, 0));
        mYOffset = h/2;
        //mScale = - (mYOffset * (1.0f / maxValue));
        mWidth = w;
        mHeight = h;
        mLastX = mWidth;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            if (mBitmap != null) {
            	
            	// Clear screen when traces get to right edge.
                if (mLastX >= mWidth) {
                    mLastX = 0;
                    //final Canvas cavas = mCanvas;
                    //cavas.drawColor(0xFFFFFFFF);
                    //mPaint.setColor(0xFF777777);
                    
                    // "Zero" line.
                    //cavas.drawLine(0, mYOffset, mWidth, mYOffset, mPaint);
                }
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        } 
    }
}
*/
