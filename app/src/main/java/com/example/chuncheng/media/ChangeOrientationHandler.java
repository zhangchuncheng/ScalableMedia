package com.example.chuncheng.media;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

public class ChangeOrientationHandler extends Handler {
	//是否是反方向横屏 true——是
    public  static  boolean AGAINST_LANDSCAPE = false;
	private Activity activity;

	public ChangeOrientationHandler(Activity ac) {
		super();
		activity = ac;
	}
	
	@Override
	public void handleMessage(Message msg) {
		if (msg.what==888) {
			int orientation = msg.arg1;
			AGAINST_LANDSCAPE = orientation > 45 && orientation < 135;
//			else if (orientation>135&&orientation<225){
//				activity.setRequestedOrientation(9);
//			}else if (orientation>225&&orientation<315){
//				activity.setRequestedOrientation(0);
//			}else if ((orientation>315&&orientation<360)||(orientation>0&&orientation<45)){
//				activity.setRequestedOrientation(1);
//			}
		}
		
		super.handleMessage(msg);        		
	}
	
}
