package com.example.arjunasri.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.Object;
import java.util.Timer;
import java.util.TimerTask;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class PostureActivity extends AppCompatActivity {

    TextView postureTextOutput;
    RequestQueue requestQueue;  // This is our requests queue to process our HTTP requests.
    String baseUrl = "http://18.237.236.234:3000/getlastImageClassification";  // API that we want to call to get the current posture
    String url,url2;
    String bedSoreAlertURL = "http://18.237.236.234:3000/getBedSoreAlert";
    String previousPosture = "Bed unoccupied"; //by default the system will be set to bed unoccupied
    Timer udpatePostureTimer;
    ImageView postureImageID;
    Handler handler = new Handler();
    boolean FallWarning = false,fallWarningActive=false;
    boolean bedSoreWarning = false, exitBedWarning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posture_activity);
        this.postureImageID = (ImageView)findViewById(R.id.PostureImage);
        this.postureTextOutput = (TextView) findViewById(R.id.posture_view);
        requestQueue = Volley.newRequestQueue(this);



        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getPostureData();
                handler.postDelayed(this,1000);
            }
        },1000);

    }


    /*This function clears out the space and sets a new image*/
    private void clearPreviousData() {
        String currentText = postureTextOutput.getText().toString();
        if((previousPosture==currentText)) { //if the posture on the screen is the same as before then do not clear
            this.postureTextOutput.setText("");
        }

    }

    private void addToRepoList(String postureName) {

        if(postureName.equals("emptybed")){
            postureName = "Bed unoccupied";
            this.postureImageID.setBackgroundDrawable(getResources().getDrawable(R.drawable.modified_image_bed));
            exitBedWarning = true;
            if(exitBedWarning ){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(PostureActivity.this);
                alertDialog.setCancelable(true);
                alertDialog.setTitle("Bed Exit Warning!");
                alertDialog.setMessage("Bed Occupant may have fallen of the bed");
                exitBedWarning = true;   //Set the fall warning as true, that the warning has been activate
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(2000);
                alertDialog.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        exitBedWarning = false;//reset the fall warning

                        /*Start a Timer and set a flag*/
                    }
                });
                alertDialog.show();
            }
        }else if(postureName.equals("leftlateral")){
            exitBedWarning = false;
            postureName = "Left Lateral";
            this.postureImageID.setBackgroundDrawable(getResources().getDrawable(R.drawable.modified_image_left_lateral));
        }else if (postureName.equals("rightlateral")){
            exitBedWarning = false;
            postureName = "Right Lateral";
            this.postureImageID.setBackgroundDrawable(getResources().getDrawable(R.drawable.modified_image_right_lateral));
        }else if (postureName.equals("faceup")){
            exitBedWarning = false;
            postureName = "Face up";
            this.postureImageID.setBackgroundDrawable(getResources().getDrawable(R.drawable.modified_image_faceup));
        }else if (postureName.equals("facedown")){
            exitBedWarning = false;
            postureName = "Face Down";
            this.postureImageID.setBackgroundDrawable(getResources().getDrawable(R.drawable.modified_image_face_down));
        }else if (postureName.equals("edge")){
            exitBedWarning = false;
            postureName = "About to fall";

            //If the FallWarning has not been generated then alert the user
            if(!FallWarning) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(PostureActivity.this);
                alertDialog.setCancelable(true);
                alertDialog.setTitle("Fall Warning!");
                alertDialog.setMessage("Bed Occupant is about to Fall out of bed");
                FallWarning = true;   //Set the fall warning as true, that the warning has been activated
                fallWarningActive = false; //after creating warning
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(2000);
                alertDialog.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        FallWarning = false;//reset the fall warning
                        fallWarningActive = true;
                        /*Start a Timer and set a flag*/
                    }
                });
                alertDialog.show();

            }
            this.postureImageID.setBackgroundDrawable(getResources().getDrawable(R.drawable.bed_unoccupied));
        }
        String strRow = postureName ;//+ " , " + lastUpdated +" , " + thirdValue + " , " + fourthValue;

        this.postureTextOutput.setText(postureName); //this sets the value in the text box
    }

    private void setTextOnScreen(String str) {


        //this.postureTextOutput.setText(str);
    }
    private void getBedSoreAlert() {
        this.url2 = this.bedSoreAlertURL;
        JsonObjectRequest arrReq = new JsonObjectRequest(Request.Method.GET,url2,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0) {

                            //for (int i = 0; i < response.length(); i++) {
                            try {
                                String flag = response.getString("BedSore");

                                //Toast.makeText(StatsActivity.this,yearInput+"/"+monthInput+"/"+dateInput,Toast.LENGTH_LONG).show();
                                if(!flag.equals("0")) {
                                    bedSoreWarning = true;
                                    if(bedSoreWarning) {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PostureActivity.this);
                                        alertDialog.setCancelable(true);
                                        alertDialog.setTitle("Bed Sore Warning!");
                                        alertDialog.setMessage("Bed Occupant's sleep posture needs to be changed");
                                        bedSoreWarning = true;   //Set the fall warning as true, that the warning has been activated

                                        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                                        vibrator.vibrate(2000);
                                        alertDialog.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                //FallWarning = false;//reset the fall warning
                                                //fallWarningActive = true;
                                                /*Start a Timer and set a flag*/
                                            }
                                        });
                                        alertDialog.show();

                                    }



                                }
                            } catch (JSONException e) {

                                Log.e("Volley", "Invalid JSON Object.");
                            }

                        }
                    }

                    // }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                        Log.e("Volley", error.toString());
                    }
                }

        );
        // The request queue will automatically handle the request queue
        arrReq.setShouldCache(false);
        requestQueue.add(arrReq);
    }

    private void getPostureFromServer() {

        this.url = this.baseUrl;

        //The POST request to get the last classified image
        JsonObjectRequest arrReq = new JsonObjectRequest(Request.Method.GET,url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.length() > 0) {

                            for (int i = 0; i < response.length(); i++) {
                                try {

                                    JSONObject jsonObj = response;
                                    String postureName = jsonObj.get("Classification").toString();
                                    if(postureName.equals("null")){
                                        postureName=previousPosture; //if null then previousPosture
                                    }else{
                                        previousPosture=postureName;
                                    }
                                    //String lastUpdated = jsonObj.get("S1").toString();
                                    //String thirdValue = jsonObj.get("S2").toString();
                                    //String fourthValue = jsonObj.get("S3").toString();

                                    addToRepoList(postureName);
                                } catch (JSONException e) {
                                    // If there is an error then output this to the logs.
                                    Log.e("Volley", "Invalid JSON Object.");
                                }

                            }
                        } else {

                            setTextOnScreen("No Data");
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // In case there is an error calling the API
                        setTextOnScreen("Error calling API");
                        Log.e("Volley", error.toString());
                    }
                }
        );

        // Need to add the request to the request queue for the http request to be made
        getBedSoreAlert();
        requestQueue.add(arrReq);
    }

    public void getPostureData() {

        clearPreviousData();

        getPostureFromServer();
    }
    @Override
    public void onBackPressed(){
        if(true){
            if(!fallWarningActive) {
                Intent intent = new Intent(this, dashboardActivity.class);
                startActivity(intent);
                handler.removeCallbacksAndMessages(null);
            }
        }
    }

}
