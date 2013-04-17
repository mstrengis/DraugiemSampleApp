package com.example.draugiemsampleapp;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import draugiem.lv.api.AuthCallback;
import draugiem.lv.api.DraugiemAuth;
import draugiem.lv.api.User;

public class MainActivity extends Activity {
	private static final String APP = "f73d8e754e72e4f89833949823bf0e51";
	
	private Button mAuthorize, mLogout;
	private TextView mUsername, mNick;
	private ImageView mIcon;
	private RelativeLayout mUserWrap;
	private ProgressBar mProgress;
	
	private DraugiemAuth mDraugiemAuth;
	private AuthCallback mAuthCallback;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 
		mDraugiemAuth = new DraugiemAuth(APP, this);
		
		mAuthCallback = new AuthCallback(){
			@Override
			public void onLogin(User u, String apikey) { 
				mProgress.setVisibility(View.GONE);
				StringBuilder name = new StringBuilder(u.name).append(" ").append(u.surname);
				if(u.age > 0){
					name.append("( ").append(u.age).append(" )");
				}
				
				new BitmapDownloader(mIcon).execute(u.imageIcon);
				
				mUsername.setText(name.toString());
				mNick.setText(new StringBuilder(u.nick).append(" ").append(u.city).toString());
				mUserWrap.setVisibility(View.VISIBLE);
				mAuthorize.setVisibility(View.GONE);
				mLogout.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onError() {
				mProgress.setVisibility(View.GONE);
			}
			@Override
			public void onNoApp() {
				mProgress.setVisibility(View.GONE);
				try {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.draugiem2")));
				} catch (android.content.ActivityNotFoundException anfe) {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.draugiem2")));
				}
			}
		};
		
		mAuthorize = (Button) findViewById(R.id.authorize);
		mLogout = (Button) findViewById(R.id.logout);
		mLogout.setVisibility(View.GONE);
		mLogout.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				mDraugiemAuth.logout();
				mLogout.setVisibility(View.GONE);
				mAuthorize.setVisibility(View.VISIBLE);
				mUserWrap.setVisibility(View.GONE);
				mIcon.setImageBitmap(null);
				mUsername.setText(null);
				mNick.setText(null);
			}
		});
		mUsername = (TextView) findViewById(R.id.username);
		mNick = (TextView) findViewById(R.id.nick);
		mIcon = (ImageView) findViewById(R.id.icon);
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mProgress.setVisibility(View.GONE);
		mUserWrap = (RelativeLayout) findViewById(R.id.user);
		
		mAuthorize.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mProgress.setVisibility(View.VISIBLE);
				mDraugiemAuth.authorize(mAuthCallback);
			}
		});
		
		mDraugiemAuth.authorizeFromCache(mAuthCallback);
		
	}
	
	private static class BitmapDownloader extends AsyncTask<String, Void, Bitmap>{
		private ImageView mImageView;
		public BitmapDownloader(ImageView view){
			mImageView = view;
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			try{
				URL u = new URL(params[0]);
		        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		        conn.setReadTimeout(30000);
		        conn.setConnectTimeout(15000);
		        conn.connect();
		        InputStream inputStream = conn.getInputStream();
		        Bitmap bitmap = null;
	        	BitmapFactory.Options o = new BitmapFactory.Options();
	        	
	        	o.inPurgeable = true;
	        	Rect rect = new Rect(0, 0, 0, 0);
	            bitmap = BitmapFactory.decodeStream(inputStream, rect, o);
	            if (inputStream != null) {
	                inputStream.close();  
	            }
				return bitmap;
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Bitmap b){
			if(mImageView != null && b != null){
				mImageView.setImageBitmap(b);
			}
		}
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(mDraugiemAuth.onActivityResult(requestCode, resultCode, data)){
			//some of draugiem intentns were captured
		}
	}
}
