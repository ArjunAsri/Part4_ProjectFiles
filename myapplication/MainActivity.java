package com.example.arjunasri.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.animation.Animator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/*This is the Login Screen Activity */
public class MainActivity extends AppCompatActivity {

    CardView PostureCard, StatsCard, YesterdayCard,SettingsCard ,LogOutCard;
    //global variables
    String name=null, password=null;
    EditText usernameField;
    EditText passwordField;
    Button loginButton;
        CardView mycard ;
        Intent i ;
        LinearLayout ll;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sign_up); //initial activity that we set up
            ll = (LinearLayout) findViewById(R.id.ll);


            usernameField = (EditText)findViewById(R.id.username); //we find the object on the basis of their id in the xml file
            passwordField = (EditText)findViewById(R.id.password);

            loginButton = (Button)findViewById(R.id.button1);
            loginButton.setOnClickListener(new View.OnClickListener(){
                @Override
                    public void onClick(View v){
                        name = usernameField.getText().toString();
                        password = passwordField.getText().toString();

                        boolean result = authenticateUser(name,password);
                        if(result){
                            showText(name);
                            dashBoard(); //call the desktop function
                        }
                }
            });


        }


        /*On Click Listener Method*/ //Switching to different activity
        private void dashBoard (){
            Intent intent = new Intent(this, dashboardActivity.class);
            startActivity(intent);
         }

        private boolean authenticateUser(String name, String Password){

            if((name.equals(""))&&(Password.equals(""))){
                return true;
            }else {
                return false;
            }
        }


        //To disable data once the user has logged in
        private void showText(String text){
            Toast.makeText(MainActivity.this, text,Toast.LENGTH_SHORT).show();
        }
//When the back button is pressed the app will not go back to the dashboard
    @Override
    public void onBackPressed(){
        if(true){
            //Do nothing //Homepage will remain that way
        }else{
            super.onBackPressed();
        }
    }
    }