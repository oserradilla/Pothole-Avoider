package com.oscarsc.potholeavoider;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oscarsc.potholeavoider.incidences.Curve;
import com.oscarsc.potholeavoider.incidences.Incidence;
import com.oscarsc.potholeavoider.incidences.IncidenceDistance;
import com.oscarsc.potholeavoider.incidences.Pothole;
import com.oscarsc.potholeavoider.incidences.Slope;

import java.text.DecimalFormat;

public class MainActivityHandler extends Handler {
    private ImageView imageIncidence;
    private FrameLayout frameLayout;
    private TextView textDistance;
    private TextView textSpeed;
    private LinearLayout layoutDistance;
    private LinearLayout layoutSpeed;
	private Activity activity;
    private boolean finish=false;

	public MainActivityHandler(Activity activity){
		this.activity=activity;
		frameLayout= (FrameLayout) activity.findViewById(R.id.content_frame);
	}
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
        if(!finish) {
            Bundle bundle = msg.getData();
            handleIncidence(bundle);
            handleWeather(bundle);
        }
	}
	private void handleIncidence(Bundle bundle) {
		IncidenceDistance incidenceDistance = (IncidenceDistance) bundle.getSerializable("incidenceDistance");
		if(incidenceDistance!=null){
			if(incidenceDistance.getPosition()<0)
                removeIncidence(incidenceDistance.getPosition());
            else{
				Incidence incidence=incidenceDistance.getIncidence();
				int distance=incidenceDistance.getDistance();
				int imageId=0;
                int slope=0;
				if (incidence instanceof Pothole)
					imageId=((Pothole) incidence).getImage();
				else if (incidence instanceof Curve)
					imageId=((Curve) incidence).getImage();
				else if (incidence instanceof Slope) {
                    imageId = ((Slope) incidence).getImage();
                    slope=((Slope) incidence).getSlope();
                }
				printIncidence(slope,distance,incidenceDistance.getPosition(),imageId);
			}
		}
	}
	private void handleWeather(Bundle bundle){
		Weather weather=(Weather) bundle.getSerializable("weather");
		if(weather!=null){
			drawWeather(weather);
		}
	}
	public void drawWeather(Weather weather){
        DecimalFormat df = new DecimalFormat("0.00");
        RelativeLayout weatherLayout = (RelativeLayout) activity.findViewById(R.id.mainLayout);
        if(weatherLayout!=null) {
            weatherLayout.setVisibility(View.VISIBLE);
            String imageName = weather.getImageId();
            if(CurrentThemeHolder.isNight())
                imageName+='n';//Add 'n' character before the extension
            int imageResource = activity.getResources().getIdentifier(imageName, null, activity.getPackageName());
            ImageView imageView = (ImageView) activity.findViewById(R.id.myImageView);
            Drawable image = activity.getResources().getDrawable(imageResource);
            imageView.setImageDrawable(image);
            TextView tv = (TextView) activity.findViewById(R.id.weatherTemperature);
            tv.setText(df.format(weather.getTempCelsius()) + " ºC");
            tv = (TextView) activity.findViewById(R.id.weatherHumidity);
            tv.setText(String.valueOf(weather.getHumidity()) + " %");
            tv = (TextView) activity.findViewById(R.id.weatherWindSpeed);
            tv.setText(String.valueOf(weather.getWindSpeed()) + "  Km/h");
        }
	}
	private void removeIncidence(int position){
		position=-position;
		switch(position){
		case 1: imageIncidence=(ImageView) frameLayout.findViewById(R.id.imageIncidenceBig);
		removeIncidenceInfo();
		break;
		case 2: imageIncidence=(ImageView) frameLayout.findViewById(R.id.imageIncidenceMedium);
		break;
		default: imageIncidence=null;
		break;
		}
		imageIncidence.setImageResource(0);
	}
	private void printIncidence(int slope,int distance, int position,int imageId){
		Log.v("Handler", "Printing incidence nº " + position+" distance: "+distance);
		switch(position){
		case 1:
            setSlope(slope);
			setIncidenceInfo(distance);
			imageIncidence=(ImageView) frameLayout.findViewById(R.id.imageIncidenceBig);
			break;
		case 2: imageIncidence=(ImageView) frameLayout.findViewById(R.id.imageIncidenceMedium);
		break;
		default: imageIncidence=null;
		break;
		}
        if(imageIncidence!=null)
		    imageIncidence.setImageResource(imageId);
	}
    private void setSlope(int slope){
        TextView tvSlope= (TextView) activity.findViewById(R.id.textViewSlope);
        if(slope!=0){
            int padding_in_dp = 10;  // 6 dps
            final float scale = activity.getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
            if(slope>0) {
                tvSlope.setPadding(0,0,padding_in_px,0);
                tvSlope.setRotation(-28f);
            }else {
                tvSlope.setPadding(padding_in_px,0,0,0);
                tvSlope.setRotation(28f);
                slope = -slope;
            }
            tvSlope.setText(String.valueOf(slope)+"%");
        }else
            tvSlope.setText("");
    }
	private void setIncidenceInfo(int distance){
        LinearLayout bottomLayout = (LinearLayout) activity.findViewById(R.id.buttomLayout);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bottomLayout.getLayoutParams();
        params.weight = 60f;
        bottomLayout.setLayoutParams(params);
        LinearLayout buttomLayout=(LinearLayout)frameLayout.findViewById(R.id.buttomLayout);
        buttomLayout.setVisibility(View.VISIBLE);
		textDistance=(TextView) frameLayout.findViewById(R.id.textDistance);
		textDistance.setText(distance+"  m");
		textSpeed=(TextView) frameLayout.findViewById(R.id.textSpeed);
		textSpeed.setText("90 km/h");
		DrawView drawSpeed = new DrawView("speed",activity);
		DrawView drawDistance = new DrawView("distance",activity);
		layoutDistance=(LinearLayout) frameLayout.findViewById(R.id.layoutDistance);
		layoutSpeed=(LinearLayout) frameLayout.findViewById(R.id.layoutSpeed);
		layoutDistance.addView(drawDistance);  
		layoutSpeed.addView(drawSpeed);
	}
	private void removeIncidenceInfo(){
        TextView tvSlope= (TextView) activity.findViewById(R.id.textViewSlope);
        tvSlope.setText("");
        LinearLayout bottomLayout = (LinearLayout) activity.findViewById(R.id.buttomLayout);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bottomLayout.getLayoutParams();
        params.weight = 0f;
        bottomLayout.setLayoutParams(params);
		textDistance=(TextView) frameLayout.findViewById(R.id.textDistance);
		textDistance.setText("");
		textSpeed=(TextView) frameLayout.findViewById(R.id.textSpeed);
		textSpeed.setText("");
		layoutDistance=(LinearLayout) frameLayout.findViewById(R.id.layoutDistance);
		layoutSpeed=(LinearLayout) frameLayout.findViewById(R.id.layoutSpeed);
		layoutDistance.removeAllViews();
		layoutSpeed.removeAllViews();
	}

	class DrawView extends View {
		Paint paint = new Paint();
		String signalDesc;
		public DrawView(String signalDesc,Context context) {
			super(context);
			this.signalDesc=signalDesc;
		}

		@Override
		public void onDraw(Canvas canvas) {
			switch(signalDesc){
			case "speed": drawSpeed(canvas);
			break;
			case "distance": drawDistance(canvas);
			break;
			}
		}
		public void drawSpeed(Canvas canvas){
			int width=canvas.getWidth();
			int height=canvas.getHeight();
			Paint paint=new Paint();

			paint.setARGB(255,0,0,255);
			canvas.drawRect(0,0,width,height,paint);

			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(15);
			paint.setARGB(255,255,255,255);
			canvas.drawRect(0,0,width,height,paint);

			paint.setStrokeWidth(3);
			paint.setARGB(255,0,0,0);
			canvas.drawRect(0,0,width,height,paint);
		}
		public void drawDistance(Canvas canvas){
			int width=canvas.getWidth();
			int height=canvas.getHeight();
			Paint paint=new Paint();

			paint.setARGB(255,255,255,255);
			canvas.drawRect(0,0,width,height,paint);

			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(10);
			paint.setARGB(255,0,0,0);
			canvas.drawRect(20,20,width-20,height-20,paint);
		}
	}
    public void finish(){
        finish=true;
    }
}
