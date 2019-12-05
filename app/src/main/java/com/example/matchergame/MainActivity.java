package com.example.matchergame;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedList;
import java.util.Random;
class Media{
    public int image, sound;

    public Media(int image, int sound) {
        this.image = image;
        this.sound = sound;
    }
}
public class MainActivity extends AppCompatActivity {
    CountDownTimer timer;
    final String highScoreKey = "highScoreKey";
    public final String SPKey = "SPkey";
    final int TIMERSECONDS = 60 / 2;
    final int LEVELSCORE = 10;
    final int MISSSCORE = 2;
    ImageView[] imageViews;
    Boolean[] hidden;
    Boolean[] removed;
    boolean running = false;
    boolean timeout = false;
    Media[] media = {
            new Media(R.drawable.image1, R.raw.audio1),
            new Media(R.drawable.image2, R.raw.audio2),
            new Media(R.drawable.image3, R.raw.audio3),
            new Media(R.drawable.image4, R.raw.audio4),
            new Media(R.drawable.image5, R.raw.audio5),
            new Media(R.drawable.image6, R.raw.audio6),
            new Media(R.drawable.image7, R.raw.audio7)};
//    Integer[] images = {R.drawable.image1,R.drawable.image2,R.drawable.image3,R.drawable.image4,R.drawable.image5,R.drawable.image6,R.drawable.image7};
//    Integer[] sounds = {R.raw.audio1,R.raw.audio2,R.raw.audio3,R.raw.audio4,R.raw.audio5,R.raw.audio6,R.raw.audio7};
    MediaPlayer mp;
    int visiblecnt = 0;
    int curScore = 0;
    int first = 0, second = 0;
    LinkedList<Integer> removable = new LinkedList<>();
    ImageButton ib;
    TextView scoreView;
    TextView timerView;
    TextView highScoreView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        assignImageViews();
        initImagesViews();
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
            }
        });
        for(int i = 0; i < imageViews.length; i++){
            hideImageView(i);
            imageViews[i].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    int index = getImageViewIndex((ImageView) view);
                    if(hidden[index] && !removed[index] && !timeout){
                        if(!running){running = true; timer.start();}
                        Log.d("INDEX", "" + index);
                        switch(visiblecnt){
                            case 0:
                                showImageView(index);
                                playSound(index);
                                first = index;
                                visiblecnt++;
                                break;
                            case 1:
                                showImageView(index);
                                playSound(index);
                                second = index;
                                visiblecnt++;
                                if(second / 2 == first / 2){//if this was a successful hit
                                    visiblecnt = 0;
                                    removed[first] = removed[second] = true;
                                    removable.add(first);
                                    removable.add(second);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            removeImageView(removable.remove());
                                            removeImageView(removable.remove());
                                            if(removable.isEmpty() && countRemoved() == imageViews.length) {
                                                curScore += LEVELSCORE;
                                                updateScore();
                                                initImagesViews();
                                            }
                                        }
                                    }, 500);
                                }
                                else{
                                    curScore -= MISSSCORE;
                                    if(curScore < 0) curScore = 0;
                                    updateScore();
                                }
                                break;
                            case 2:
                                for(int j = 0; j < imageViews.length; j++)if (!removed[j])hideImageView(j);
                                showImageView(index);
                                playSound(index);
                                first = index;
                                visiblecnt = 1;
                                break;
                        }
                    }
                    else{}
                }
            });
        }

    }
    private void updateScore(){
        scoreView.setText("" + curScore);
    }
    private void updateHighScore(){
        int score = getSharedPreferences(SPKey,MODE_PRIVATE).getInt(highScoreKey,0);
        highScoreView.setText("" + score);
    }
    private void updateTime(long seconds){
        String ans = "";
        long min = seconds / 60;
        if(min < 10)
            ans += "0";
        ans += min;
        ans += ":";
        long sec = seconds % 60;
        if(sec < 10)
            ans += "0";
        ans += sec;
        timerView.setText(ans);
    }
    synchronized private void playSound(int index){
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
        mp = MediaPlayer.create(getBaseContext(), media[index / 2].sound);
        mp.start();
    }
    private void hideImageView(int index){
        if(!hidden[index]) {
            hidden[index] = true;
            imageViews[index].setImageResource(R.drawable.question_mark);
        }
    }
    private void removeImageView(int index){
        imageViews[index].setVisibility(View.INVISIBLE);
    }
    private int countRemoved(){
        int cnt = 0;
        for (Boolean b : removed)if(b)cnt++;
        return cnt;
    }
    private void showImageView(int index){
        if(hidden[index]) {
            hidden[index] = false;
            imageViews[index].setImageResource(media[index / 2].image);
        }
    }
    private int getImageViewIndex(ImageView iv){
        for(int i = 0; i < imageViews.length; i++){
            if (iv == imageViews[i]){
                return i;
            }
        }
        return -1;
    }
    private void resetGame(){
        resetTimer();
        curScore = 0;
        for(int i = 0; i < imageViews.length; i++)removeImageView(i);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initImagesViews();
            }
        }, 500);
    }
    private void resetTimer(){
        running = false;
        updateTime(0);
        if(timer != null) timer.cancel();
        timer = new CountDownTimer(TIMERSECONDS * 1000, 1000) {
            @Override
            public void onTick(long l) {
                updateTime(l / 1000);
            }

            @Override
            public void onFinish() {
                timeout = true;
                makeToast("Game over");
                checkForHighScore();
            }
        };
    }
    private void initImagesViews(){
        timeout = false;

        updateHighScore();
        updateScore();

        scrambleArray(imageViews);
        scrambleArray(media);
        for(int i = 0; i < hidden.length; i++)removed[i] = hidden[i] = false;
        visiblecnt = 0;
        for(int i = 0; i < imageViews.length; i++){
            hideImageView(i);
            imageViews[i].setVisibility(View.VISIBLE);
            //images are shared if i1 / 2 = i2 / 2
        }
    }
    private void scrambleArray(Object[] objects){
        int seed = new Random().nextInt(objects.length);
        for(int i = 0; i < objects.length; i++){
            seed = (seed + i) % objects.length;
            Object tmp = objects[i];
            objects[i] = objects[seed];
            objects[seed] = tmp;
        }
    }
    private void assignImageViews(){
        imageViews = new ImageView[8];
        hidden = new Boolean[8];
        removed = new Boolean[8];
        try {
            imageViews[0] = findViewById(R.id.image1);
            ib = findViewById(R.id.reset);
            scoreView = findViewById(R.id.score);
            timerView = findViewById(R.id.time);
            highScoreView = findViewById(R.id.highscore);
        }
        catch (Exception e){
            Log.d("IMAGEVIEWNOTFOUND", "failed to bind image view to array");
            return;
        }
        imageViews[1] = findViewById(R.id.image2);
        imageViews[2] = findViewById(R.id.image3);
        imageViews[3] = findViewById(R.id.image4);
        imageViews[4] = findViewById(R.id.image5);
        imageViews[5] = findViewById(R.id.image6);
        imageViews[6] = findViewById(R.id.image7);
        imageViews[7] = findViewById(R.id.image8);
        resetTimer();
    }
    private void checkForHighScore(){
        SharedPreferences prefs = getSharedPreferences(SPKey,MODE_PRIVATE);
        int bestScore = prefs.getInt(highScoreKey,0);
        if(curScore > bestScore){
            bestScore = curScore;
            SharedPreferences.Editor e = prefs.edit();
            e.putInt(highScoreKey,bestScore);
            Log.d("HIGHSCORE", " trying to log" + bestScore);
            e.commit();
            Log.d("HIGHSCORE", " should have logged" + prefs.getInt(highScoreKey,0));
        }
        updateHighScore();
    }
    private void makeToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
