package com.example.arjunasri.myapplication;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.View;

public class dashboardActivity extends AppCompatActivity implements View.OnClickListener{

    private CardView PostureCard, StatsCard, YesterdayCard,SettingsCard ,LogOutCard;



    @Override
    protected void onCreate(Bundle savedInstanceState){ //Bundle requires android.os.Bundle
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //initial activity that we set up
        PostureCard = (CardView)findViewById(R.id.PostureCard); //to access the various features of a page we need to display the activity at the start
        StatsCard = (CardView)findViewById(R.id.StatsCard);
        YesterdayCard = (CardView)findViewById(R.id.YesterdayCard);
        SettingsCard = (CardView)findViewById(R.id.SettingsCard);
        LogOutCard = (CardView)findViewById(R.id.LogOutCard);

        PostureCard.setOnClickListener(this);
        StatsCard.setOnClickListener(this);
        YesterdayCard.setOnClickListener(this);
        SettingsCard.setOnClickListener(this);
        LogOutCard.setOnClickListener(this);

    }

    @Override
    public void onClick(View v){

        Intent intent;
        switch(v.getId()){
            case R.id.PostureCard : intent = new Intent(this,PostureActivity.class);
                startActivity(intent);
                break;
            case R.id.StatsCard : intent = new Intent(this,StatsActivity.class);
                startActivity(intent);
                break;
            case R.id.YesterdayCard : intent = new Intent(this,yesterdayActivity.class);
                startActivity(intent);
                break;
            case R.id.SettingsCard : intent = new Intent(this,settings_activity.class);
                startActivity(intent);
                break;
            case R.id.LogOutCard : intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;

        }

    }

    @Override
    public void onBackPressed(){
        if(true){

        }else{
            super.onBackPressed();
        }
    }
}
