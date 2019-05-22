package uoit.ca.attendme;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LocationActivity extends AppCompatActivity {

    Button confirmAttendance;
    TextView locationStatus;
    LocationTracker locationTracker;
    double currentLongitude, currentLatitude;
    boolean isAppRunning = true;
    public TextView timer;
    public long timeLeft= 10000; //in milliseconds = 1 minute
    public boolean isTimerOn;
    public String CHANNEL_ID = "13";
    public int notificationId = 1;
    public Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        locationStatus = (TextView) findViewById(R.id.locationTxt);
        confirmAttendance = (Button) findViewById(R.id.attendanceBtn);
        confirmAttendance.setEnabled(false);
        locationTracker = new LocationTracker(this);
        timer = (TextView) findViewById(R.id.timerTxt);


        currentLatitude = locationTracker.getLatitude();
        currentLongitude = locationTracker.getLongitude();

        if (currentLongitude <= Constants.uoitLongitude + 0.0040 && currentLongitude >= Constants.uoitLongitude - 0.0040) {
            if (currentLatitude <= Constants.uoitLatitude + 0.0040 && currentLatitude >= Constants.uoitLatitude - 0.0040) {
                locationTracker.onCampus = true;
                locationStatus.setText("You are on campus, please stay within your lecture hall and prepare to verify your attendance in 1 minute. You will be notified when ready.");
                locationStatus.setTextColor(Color.parseColor("#00FF00"));
                startTimer();
            }
            } else {
                locationTracker.onCampus = false;
                locationStatus.setText("You are not on campus, please go to your lecture hall & try again!");
                locationStatus.setTextColor(Color.parseColor("#FF0000"));
                }
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void startTimer()
    {
        Constants.countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft=millisUntilFinished;
                updateTimerTxt();
            }

            @Override
            public void onFinish() {
                    if(Constants.stayedOnCampus)
                    {
                        //Notification building
                        Intent intent = new Intent(mContext, QuestionActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                        createNotificationChannel();
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle("Your attendance is ready!")
                                .setContentText("Please return to the application to verify your attendance")
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                        notificationManager.notify(notificationId, mBuilder.build());
                        locationStatus.setText("Please click the below button and answer at least 1 question to register your attendance");

                        confirmAttendance.setEnabled(true);

                    }
                    else
                    {
                        locationStatus.setText("You left the campus, please go back to your lecture hall & try again!");
                    }
            }
        }.start();
        isTimerOn=true;
    }

    public void updateTimerTxt()
    {
        int minutes = (int) timeLeft/60000;
        int seconds = (int) timeLeft%60000 / 1000;
        String timeLeft;
        timeLeft=String.valueOf(minutes);
        timeLeft=":";
        if(seconds<10) timeLeft += "0";
        timeLeft+=String.valueOf(seconds);
        timer.setText(timeLeft);
    }
    public void attend(View v)
    {
        Intent intent = new Intent (this, QuestionActivity.class);
        startActivity(intent);
    }
}


