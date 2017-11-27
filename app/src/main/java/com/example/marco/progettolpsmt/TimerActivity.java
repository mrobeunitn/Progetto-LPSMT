package com.example.marco.progettolpsmt;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

//import com.example.marco.progettolpsmt.backend.Log;
import com.example.marco.progettolpsmt.backend.Course;
import com.example.marco.progettolpsmt.backend.TimerSettingsSingleton;
import com.example.marco.progettolpsmt.managers.DBManager;

import android.text.format.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.iwgang.countdownview.CountdownView;
import devlight.io.library.ArcProgressStackView;
import static devlight.io.library.ArcProgressStackView.Model;


public class TimerActivity extends AppCompatActivity {

    private int mCounter = 0;
    public final static int MODEL_COUNT = 3;
    private ArcProgressStackView mArcProgressStackView;
    private Button startbutton;
    private Button pause;
    private Button settings;
    private CountdownView  countdownview;
    private long animationstate=0,animationstatesecondarch =0,thirdarchanimationstate=0;
    private long n_session = 4;
    private long studytimetimer;
    private Spinner coursespinner;
    private Spinner argumentspinner;
    private long breaktimetimer;
    private boolean isdialogsetted = false;
    private Course courses;
    //circle creation
    private ArrayList<Model> models;
    //animator declaration and initialization
    final ValueAnimator firstarc = ValueAnimator.ofFloat(100);
    final ValueAnimator secondarc = ValueAnimator.ofFloat(100);
    final ValueAnimator thirdarc = ValueAnimator.ofFloat(100);
    //on finish animation declaration
    final ValueAnimator reversefirstarc = ValueAnimator.ofFloat(100);
    final ValueAnimator reversesecondarc = ValueAnimator.ofFloat(100);
    final ValueAnimator reversethirdarc = ValueAnimator.ofFloat(100);
    //backends classes
    private Course course;

    //Notification
    TimerNotification timerNotification;

    @Override
    protected void onCreate(final Bundle extras) {
        setContentView(R.layout.activity_timer);
        super.onCreate(extras);
        //backend example
        course = new Course();

        Course courseToStudy = null;
        try {
            if (extras != null) {
                courseToStudy = DBManager.getCourse(extras.getInt("courseID"));
            }
        }
        catch (NullPointerException e) {}
        if (courseToStudy != null) {
            /*TODO initialize the timer with the course: courseToStudy*/
        }

        //Dialog used in order to take data from user, that we need in order to initializate timer
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.timerinitializationpopup);
        final AlertDialog confirmchangecourseargumentdialog = new AlertDialog.Builder(this)
                .setTitle("Change Course or Argument")
                .setMessage("Are you sure that you want to change argument or course?\n You will lose current progress..")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }})
                .setNegativeButton(android.R.string.no, null).create();;


        Button yourButton = dialog.findViewById(R.id.button);
        //textbox of the dialog
        final EditText sessions = dialog.findViewById(R.id.editText2);
        final EditText studytime = dialog.findViewById(R.id.editText3);
        final EditText breaktime = dialog.findViewById(R.id.editText4);
        //spinners
        coursespinner = findViewById(R.id.coursespinner);
        argumentspinner = findViewById(R.id.argumentspinner);

       // coursespinner.set(course.getName());
        ;
        //buttons
        startbutton = (Button) findViewById(R.id.startbtn);
        pause =(Button) findViewById(R.id.pausebtn);
        settings = (Button) findViewById(R.id.settings);
        //testual timer
        countdownview = findViewById(R.id.countdownview);
        //arch model
        mArcProgressStackView = (ArcProgressStackView) findViewById(R.id.apsv_presentation);
        //model array
        models = new ArrayList<>();

        //adding listener to buttons
        yourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !((sessions.getText()).toString().equals("")) && !((studytime.getText()).toString().equals("")) && !((breaktime.getText()).toString().equals(""))) {
                    //go on here and dismiss dialog
                    n_session = Integer.parseInt(sessions.getText().toString());
                    studytimetimer = Long.parseLong(studytime.getText().toString())*60000;
                    breaktimetimer = Long.parseLong(breaktime.getText().toString())*60000;
                    isdialogsetted = true;
                    countdownview.updateShow(studytimetimer);
                    initializeArcModel(n_session,studytimetimer,breaktimetimer);
                    initializeTimerView(thirdarc,secondarc,firstarc,mArcProgressStackView);
                    dialog.dismiss();
                }
            }
        });
         /*
           if user doesn't set values, the system will use default setted values
         */
         if(isdialogsetted == false) {
             n_session = TimerSettingsSingleton.getInstance().getNumberOfStudySessions(this);
             studytimetimer = TimerSettingsSingleton.getInstance().getNumberOfStudyDuration(this);
             breaktimetimer = TimerSettingsSingleton.getInstance().getNumberOfBreakDuration(this);
             countdownview.updateShow(studytimetimer);
         }

        //ArcProgressView initialization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            models.add(new Model("Study Time", 0,getColor(R.color.colorPrimary),getColor(R.color.colorAccent)));
            models.add(new Model("Break Time", 0,getColor(R.color.colorPrimary) , getColor(R.color.colorAccent)));
            models.add(new Model("Session Progress", 0, getColor(R.color.colorPrimary), getColor(R.color.colorAccent)));

        }
        else{
            models.add(new Model("Study Time", 0,Color.parseColor("#00bcd4"),Color.parseColor("#ff5722")));
            models.add(new Model("Break Time", 0,Color.parseColor("#00bcd4") , Color.parseColor("#ff5722")));
            models.add(new Model("Session Progress", 0,Color.parseColor("#00bcd4"), Color.parseColor("#ff5722")));
        }
        mArcProgressStackView.setModels(models);
        mArcProgressStackView.setSweepAngle(270);
        /**
         * firstarc == external arch
         * secondarc = middle arch
         * thirdarc = internal arch
         * Setting up Animators
         */
        this.initializeArcModel(n_session,studytimetimer,breaktimetimer);
        firstarc.setInterpolator(new LinearInterpolator());
        secondarc.setInterpolator(new LinearInterpolator());
        thirdarc.setInterpolator(new LinearInterpolator());

        /**
         * setting up on finish animation
         */
        reversefirstarc.setDuration(2000);
        reversesecondarc.setDuration(2000);
        reversethirdarc.setDuration(2000);
        /**
         * On end listeners. This listeners are used in order to allow graphic sync between circles.
         * When a circle animation end, the onEndListener update
         */
        firstarc.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                mCounter = 0;
                animationstate = 0;
                countdownview.start(breaktimetimer);
                secondarc.start();
            }
            @Override
            public void onAnimationRepeat(final Animator animation) {
                mCounter++;
            }
        });

        secondarc.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                animationstatesecondarch= 0;
                thirdarc.pause();
               /* mArcProgressStackView.getModels().get(MODEL_COUNT-2).setProgress(0);
                mArcProgressStackView.getModels().get(MODEL_COUNT-3).setProgress(0);*/
               reversefirstarc.reverse();
               reversesecondarc.reverse();
            }
            @Override
            public void onAnimationRepeat(final Animator animation) {

            }
        });

        thirdarc.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                reversethirdarc.reverse();
            }
            @Override
            public void onAnimationRepeat(final Animator animation) {

            }
        });

        final NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Sets an ID for the notification, so it can be updated
        final int notifyID = 1;
        timerNotification = new TimerNotification();


        /**
         * Animator update listener. This methods are used to update graphics animation of
         * the ArchModel. Every circle own a method that update graphics valued differently as the other
         * due to different values setted by user before the animation starts.
         */


        firstarc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mArcProgressStackView.getModels().get(MODEL_COUNT-3)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                animationstate = animation.getCurrentPlayTime();
                mArcProgressStackView.postInvalidate();

            }
        });

        secondarc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mArcProgressStackView.getModels().get(MODEL_COUNT-2)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                animationstatesecondarch = animation.getCurrentPlayTime();
                mArcProgressStackView.postInvalidate();
            }
        });

        thirdarc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //mArcProgressStackView.getModels().get(MODEL_COUNT - 1).setProgress(session);
                mArcProgressStackView.getModels().get(MODEL_COUNT-1)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                thirdarchanimationstate = animation.getCurrentPlayTime();
                mArcProgressStackView.postInvalidate();
            }
        });
        /**
         * reverse animation on finish
         */
        reversefirstarc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //mArcProgressStackView.getModels().get(MODEL_COUNT - 1).setProgress(session);
                mArcProgressStackView.getModels().get(MODEL_COUNT-3)
                        .setProgress((Float) animation.getAnimatedValue());
                thirdarchanimationstate = animation.getCurrentPlayTime();
                mArcProgressStackView.postInvalidate();
            }
        });
        reversesecondarc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //mArcProgressStackView.getModels().get(MODEL_COUNT - 1).setProgress(session);
                mArcProgressStackView.getModels().get(MODEL_COUNT-2)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                thirdarchanimationstate = animation.getCurrentPlayTime();
                mArcProgressStackView.postInvalidate();
            }
        });
        reversethirdarc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //mArcProgressStackView.getModels().get(MODEL_COUNT - 1).setProgress(session);
                mArcProgressStackView.getModels().get(MODEL_COUNT-2)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                thirdarchanimationstate = animation.getCurrentPlayTime();
                mArcProgressStackView.postInvalidate();
            }
        });

        //revers animation onEnd listener

        reversesecondarc.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                startbutton.setClickable(true);
            }
            @Override
            public void onAnimationRepeat(final Animator animation) {

            }
        });

        /**
         * Button listeners
         */
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.KITKAT)
            public void onClick(View v) {
                timerNotification.notify(getBaseContext(),"Studying",1);
                final NotificationCompat.Builder mNotifyBuilder = timerNotification.getBuilder();
                if(animationstate != 0) {
                    firstarc.resume();
                    thirdarc.resume();
                    countdownview.restart();
                    startbutton.setClickable(false);

                    return;
                }
                if(animationstatesecondarch != 0){
                    secondarc.resume();
                    thirdarc.resume();
                    countdownview.restart();
                    startbutton.setClickable(false);
                    return;
                }
                if(thirdarchanimationstate != 0 ){
                    firstarc.start();
                    thirdarc.resume();
                    startbutton.setClickable(false);
                    return;
                }
                firstarc.start();
                thirdarc.start();
                countdownview.start(studytimetimer);
                //cambiare il colore del bottone
                startbutton.setClickable(false);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                timerNotification.notify(getBaseContext(),"Break Time",1);
                final NotificationCompat.Builder mNotifyBuilder = timerNotification.getBuilder();
                firstarc.pause();
                secondarc.pause();
                thirdarc.pause();
                countdownview.pause();
                startbutton.setClickable(true);

            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        //spinners onchange listeners


    }

    private void initializeArcModel(long numberofsessions , long studytime, long breaktime){
        firstarc.setDuration(studytime);
        secondarc.setDuration(breaktime);
        thirdarc.setDuration((studytime+breaktime)*numberofsessions);
    }

    private void initializeTimerView(ValueAnimator session, ValueAnimator study, ValueAnimator breakt, ArcProgressStackView stackView){
        firstarc.cancel();
        secondarc.cancel();
        thirdarc.cancel();
        animationstatesecondarch = 0;
        thirdarchanimationstate = 0;
        animationstate = 0;
        stackView.getModels().get(MODEL_COUNT-2)  //Math.min(mCounter, MODEL_COUNT - 2)
                .setProgress(0);
        stackView.getModels().get(MODEL_COUNT-1)  //Math.min(mCounter, MODEL_COUNT - 2)
                .setProgress(0);
        stackView.getModels().get(MODEL_COUNT-3)  //Math.min(mCounter, MODEL_COUNT - 2)
                .setProgress(0);
        startbutton.setClickable(true);
        countdownview.stop();
        countdownview.updateShow(studytimetimer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
