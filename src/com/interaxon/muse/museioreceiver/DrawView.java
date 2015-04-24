package com.interaxon.muse.museioreceiver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View {

	private static final int movementAmt = 5;
	
	private int objWidth = 120;
	private int posX = 0;
	private int posY = 0;
	
	private boolean bInit = false;
	private int origX = 0;
	private int origY = 0;
	
	private int cWidth = 0;
	private int cHeight = 0;
	
    Paint paint = new Paint();
    
    public DrawView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

    @Override
    public void onDraw(Canvas canvas) {
    	
    	cWidth = canvas.getWidth();
    	cHeight = canvas.getHeight();
    	paint.setColor(Color.argb(120, 150, 150, 150));
    	paint.setStyle(Paint.Style.FILL);
    	canvas.drawRect(0, 0, cWidth, cHeight, paint);
    	
    	if(!bInit && posX == 0 && posY == 0)
    	{
    		posX = (cWidth - objWidth)/2;
    		posY = (cHeight - objWidth);
    		
    		bInit = true;
    		origX = posX;
    		origY = posY;
    	}
    	
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(posX, posY,
        		(posX+objWidth), (posY+objWidth), paint);
    }
    
    public int getPosX() {
    	return posX;
    }
    
    public int getPosY() {
    	return posY;
    }
    
    public void resetBoxPos() {
    	if(bInit)
    	{
    		posX = origX;
    		posY = origY;
    		postInvalidate();
    	}
    }
    
    public void moveUp() {
    	if(bInit)
    	{
    		if(posY > 0)
    		{
    			posY -= movementAmt;
    			postInvalidate();
    		}
    	}
    }
    
    public void moveLeft() {
    	if(bInit)
    	{
    		if(posX > 0)
    		{
    			posX -= movementAmt;
    			postInvalidate();
    		}
    	}
    }
    
    public void moveRight() {
    	if(bInit)
    	{
    		if((posX + objWidth) < cWidth)
    		{
    			posX += movementAmt;
    			postInvalidate();
    		}
    	}
    }

}