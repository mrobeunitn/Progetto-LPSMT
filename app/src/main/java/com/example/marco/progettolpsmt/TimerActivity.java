package com.example.marco.progettolpsmt;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

//import com.example.marco.progettolpsmt.backend.Log;
import com.example.marco.progettolpsmt.backend.Course;
import com.example.marco.progettolpsmt.backend.TimerSettingsSingleton;
import com.example.marco.progettolpsmt.managers.DBManager;

import java.util.ArrayList;

import cn.iwgang.countdownview.CountdownView;
import devlight.io.library.ArcProgressStackView;
import static devlight.io.library.ArcProgressStackView.Model;


public class
TimerActivity extends AppCompatActivity {

    private int mCounter = 0;
    public final static int MODEL_COUNT = 3;
    private ArcProgressStackView mArcProgressStackView;
    private Button startButton;
    private Button pause;
    private Button settings;
    private CountdownView countdownView;
    private long animationStateThirdArch =0, animationStateSecondArch =0, thirdArchAnimationState =0;
    private long nSession = 4;
    private long studyTimeTimer;
    private Spinner courseSpinner;
    private Spinner argumentSpinner;
    private long breakTimeTimer;
    private boolean isDialogSetted = false;
    private Course courses;
    //circle creation
    private ArrayList<Model> models;
    //animator declaration and initialization
    final ValueAnimator firstArch = ValueAnimator.ofFloat(100);
    final ValueAnimator secondArch = ValueAnimator.ofFloat(100);
    final ValueAnimator thirdArch = ValueAnimator.ofFloat(100);
    //on finish animation declaration
    final ValueAnimator reverseFirstArch = ValueAnimator.ofFloat(100);
    final ValueAnimator reverseSecondArch = ValueAnimator.ofFloat(100);
    final ValueAnimator reverseThirdArch = ValueAnimator.ofFloat(100);
    //backends classes
    private Course course;

    //Notification
    TimerNotification timerNotification;

    @Override
    protected void onCreate(final Bundle extras) {
        super.onCreate(extras);
        setContentView(R.layout.activity_timer);

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


        Button confirmTimerTemporaryChanges = dialog.findViewById(R.id.button);
        Button cancelTimerTemporaryChanges  = dialog.findViewById(R.id.cancelbutton);
        //textbox of the dialog
        final EditText sessions = dialog.findViewById(R.id.editText2);
        final EditText studyTime = dialog.findViewById(R.id.editText3);
        final EditText breakTime = dialog.findViewById(R.id.editText4);
        //spinners
        courseSpinner = findViewById(R.id.coursespinner);
        argumentSpinner = findViewById(R.id.argumentspinner);

        //buttons
        startButton = (Button) findViewById(R.id.startbtn);
        pause =(Button) findViewById(R.id.pausebtn);
        settings = (Button) findViewById(R.id.settings);
        //testual timer
        countdownView = findViewById(R.id.countdownview);
        //arch model
        mArcProgressStackView = (ArcProgressStackView) findViewById(R.id.apsv_presentation);
        //model array
        models = new ArrayList<>();

        //adding listener to buttons
        confirmTimerTemporaryChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !((sessions.getText()).toString().equals("")) && !((studyTime.getText()).toString().equals("")) && !((breakTime.getText()).toString().equals(""))) {
                    //go on here and dismiss dialog
                    nSession = Integer.parseInt(sessions.getText().toString());
                    studyTimeTimer = Long.parseLong(studyTime.getText().toString())*60000;
                    breakTimeTimer = Long.parseLong(breakTime.getText().toString())*60000;
                    isDialogSetted = true;
                    countdownView.updateShow(studyTimeTimer);
                    initializeTimerView(mArcProgressStackView);
                    initializeArcModel(nSession, studyTimeTimer, breakTimeTimer);
                    dialog.dismiss();
                }
            }
        });

        cancelTimerTemporaryChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    dialog.dismiss();
            }
        });


         /*
           if user doesn't set values, the system will use default setted values
         */
         if(isDialogSetted == false) {
             nSession = TimerSettingsSingleton.getInstance().getNumberOfStudySessions(this);
             studyTimeTimer = TimerSettingsSingleton.getInstance().getNumberOfStudyDuration(this);
             breakTimeTimer = TimerSettingsSingleton.getInstance().getNumberOfBreakDuration(this);
             countdownView.updateShow(studyTimeTimer);
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

        this.initializeArcModel(nSession, studyTimeTimer, breakTimeTimer);
        firstArch.setInterpolator(new LinearInterpolator());
        secondArch.setInterpolator(new LinearInterpolator());
        thirdArch.setInterpolator(new LinearInterpolator());

        /**
         * setting up on finish animation
         */
        reverseFirstArch.setDuration(2000);
        reverseSecondArch.setDuration(2000);
        reverseThirdArch.setDuration(2000);
        /**
         * On end listeners. This listeners are used in order to allow graphic sync between circles.
         * When a circle animation end, the onEndListener update
         */
        firstArch.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                mCounter = 0;
                animationStateThirdArch = 0;
                countdownView.start(breakTimeTimer);
                secondArch.start();
            }
            @Override
            public void onAnimationRepeat(final Animator animation) {
                mCounter++;
            }
        });

        secondArch.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                animationStateSecondArch = 0;
                thirdArch.pause();
               reverseFirstArch.reverse();
               reverseSecondArch.reverse();
            }
            @Override
            public void onAnimationRepeat(final Animator animation) {

            }
        });

        thirdArch.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                reverseThirdArch.reverse();
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


        firstArch.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mArcProgressStackView.getModels().get(MODEL_COUNT-3)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                animationStateThirdArch = animation.getCurrentPlayTime();
                mArcProgressStackView.postInvalidate();

            }
        });

        secondArch.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mArcProgressStackView.getModels().get(MODEL_COUNT-2)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                animationStateSecondArch = animation.getCurrentPlayTime();
                mArcProgressStackView.postInvalidate();
            }
        });

        thirdArch.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //mArcProgressStackView.getModels().get(MODEL_COUNT - 1).setProgress(session);
                mArcProgressStackView.getModels().get(MODEL_COUNT-1)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                thirdArchAnimationState = animation.getCurrentPlayTime();
                mArcProgressStackView.postInvalidate();
            }
        });
        /**
         * reverse animation on finish
         */
        reverseFirstArch.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //mArcProgressStackView.getModels().get(MODEL_COUNT - 1).setProgress(session);
                mArcProgressStackView.getModels().get(MODEL_COUNT-3)
                        .setProgress((Float) animation.getAnimatedValue());
                mArcProgressStackView.postInvalidate();
            }
        });
        reverseSecondArch.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //mArcProgressStackView.getModels().get(MODEL_COUNT - 1).setProgress(session);
                mArcProgressStackView.getModels().get(MODEL_COUNT-2)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                mArcProgressStackView.postInvalidate();
            }
        });
        reverseThirdArch.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //mArcProgressStackView.getModels().get(MODEL_COUNT - 1).setProgress(session);
                mArcProgressStackView.getModels().get(MODEL_COUNT-1)  //Math.min(mCounter, MODEL_COUNT - 2)
                        .setProgress((Float) animation.getAnimatedValue());
                mArcProgressStackView.postInvalidate();
            }
        });

        //revers animation onEnd listener

        reverseSecondArch.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                startButton.setClickable(true);
            }
            @Override
            public void onAnimationRepeat(final Animator animation) {

            }
        });

        /**
         * Button listeners
         */
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.KITKAT)
            public void onClick(View v) {
                timerNotification.notify(getBaseContext(),"Studying",1);
                final NotificationCompat.Builder mNotifyBuilder = timerNotification.getBuilder();
                if(animationStateThirdArch != 0) {
                    firstArch.resume();
                    thirdArch.resume();
                    countdownView.restart();
                    startButton.setClickable(false);
                    return;
                }
                if(animationStateSecondArch != 0){
                    secondArch.resume();
                    thirdArch.resume();
                    countdownView.restart();
                    startButton.setClickable(false);
                    return;
                }
                if(thirdArchAnimationState != 0 ){
                    firstArch.start();
                    thirdArch.resume();
                    countdownView.restart();
                    startButton.setClickable(false);
                    return;
                }
                countdownView.start(studyTimeTimer);
                firstArch.start();
                thirdArch.start();
                //cambiare il colore del bottone
                startButton.setClickable(false);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                timerNotification.notify(getBaseContext(),"Break Time",1);
                final NotificationCompat.Builder mNotifyBuilder = timerNotification.getBuilder();
                firstArch.pause();
                secondArch.pause();
                thirdArch.pause();
                countdownView.pause();
                startButton.setClickable(true);

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
        breaktime = breakTimeTimer;
        firstArch.setDuration(studytime);
        Log.d("breaktimemtimer--->",""+breaktime);
        secondArch.setDuration(breaktime);
        thirdArch.setDuration((studytime+breaktime)*numberofsessions);
    }

    private void initializeTimerView(ArcProgressStackView stackView){
        firstArch.cancel();
        secondArch.cancel();
        thirdArch.cancel();
        animationStateSecondArch = 0;
        thirdArchAnimationState = 0;
        animationStateThirdArch = 0;
        stackView.getModels().get(MODEL_COUNT-2)  //Math.min(mCounter, MODEL_COUNT - 2)
                .setProgress(0);
        stackView.getModels().get(MODEL_COUNT-1)  //Math.min(mCounter, MODEL_COUNT - 2)
                .setProgress(0);
        stackView.getModels().get(MODEL_COUNT-3)  //Math.min(mCounter, MODEL_COUNT - 2)
                .setProgress(0);
        startButton.setClickable(true);
        countdownView.stop();
        countdownView.updateShow(studyTimeTimer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerNotification.cancel(this);
    }
}
