package com.example.arjunasri.myapplication;
import java.util.*; //for ArrayList

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.data.*;//the following are needed
import com.github.mikephil.charting.charts.PieChart;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



/*The Stats Activity class is activated through the dashboard intent, we show the pie chart and the sleep duration
* through this intent. There is a calendar that the user can use to input the date to get the data for a specific date*/
public class StatsActivity extends AppCompatActivity {

    float variousPostureData[] ={0.0f,0.0f,0.0f,0.0f}; //This will display a black circle
    String baseUrl = "http://18.237.236.234:3000/getPostureRatio";  // API to be called from the cloud server
    String sleepDurationURL = "http://18.237.236.234:3000/getTimeInBed"; //API to get sleep duration Time 34.221.49.168
    RequestQueue requestQueue;
    RequestQueue requestSleepDuration;
    String url;
    String url2;
    //String array with Posture Names
    String postureNames[] = {"Face Up","Right Lateral","Left Lateral","Face Down"};

    Button selectDateButton;
    TextView sleepDurationText;
    int dateInput=0, monthInput=0, yearInput = 0;

    static final int DIALOG_ID = 0;

    public void createDialogOnButtonClick(){
        selectDateButton = (Button)findViewById(R.id.SelectDateButton);

        selectDateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(DIALOG_ID);
                    }
                }
        );
    }

    private DatePickerDialog.OnDateSetListener dpickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            yearInput = year;
            monthInput = month+1; //because months are indexed from 0
            dateInput = dayOfMonth;
            Toast.makeText(StatsActivity.this,yearInput+"/"+monthInput+"/"+dateInput,Toast.LENGTH_LONG).show();
            dateInput = dayOfMonth-1;//one day behind form input value
            baseUrl = baseUrl + "/" + dateInput +"/" + monthInput + "/"+ yearInput;
            sleepDurationURL = sleepDurationURL + "/" + dateInput +"/" + monthInput + "/"+ yearInput;
            getPostureFromServer();

            //Call the getPostureDistribution API here
        }
    };
    @Override
    protected Dialog onCreateDialog(int id){
        if(id==DIALOG_ID){
            return new DatePickerDialog(this,dpickerListener,yearInput,monthInput,dateInput);
        }
        return null;
    }



    /*Function for creating Pie Graph*/

    private void createPieChart(){
        List<PieEntry> pieEntries = new ArrayList<>();
        for(int i = 0; i < variousPostureData.length;i++){
            pieEntries.add(new PieEntry(variousPostureData[i],postureNames[i]));

        }

        PieDataSet dataSet = new PieDataSet(pieEntries,"Posture Distribution for Sleep");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData(dataSet);

        PieChart chart = (PieChart) findViewById(R.id.chart);
        chart.setData(data);
        chart.invalidate();
    }



    private void getSleepDurationFromServer() {
        this.url2 = this.sleepDurationURL;
        JsonObjectRequest arrReq = new JsonObjectRequest(Request.Method.GET,url2,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0) {

                            //for (int i = 0; i < response.length(); i++) {
                                try {
                                    String Hours = response.getString("Hours");
                                    String Minutes = response.getString("Minutes");
                                    String Seconds = response.getString("Seconds");
                                    //Toast.makeText(StatsActivity.this,yearInput+"/"+monthInput+"/"+dateInput,Toast.LENGTH_LONG).show();
                                    if(!Minutes.equals("NaN")) {

                                        sleepDurationText.setText(Hours + ":" + Minutes + ":"+ Seconds);


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

/*-------------------------------------------------------------------------------------------------*/

    private void getPostureFromServer() {

        this.url = this.baseUrl;


        JsonObjectRequest arrReq = new JsonObjectRequest(Request.Method.GET,url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.length() > 0) {

                            //for (int i = 0; i < response.length(); i++) {
                            try {
                                // For each repo, add a new line to our repo list.
                                String FaceUpRatio = response.getString("FaceUpRatio");
                                String FaceDownRatio = response.getString("FaceDownRatio");
                                String RightLateralRatio = response.getString("RightLateralRatio");
                                String LeftLateralRatio = response.getString("LeftLateralRatio");
                                //Toast.makeText(StatsActivity.this,yearInput+"/"+monthInput+"/"+dateInput,Toast.LENGTH_LONG).show();
                                if(!FaceUpRatio.equals("NaN")) { // Create the graph for the system if the face up is not equal to NaN
                                    variousPostureData[0] = Float.parseFloat(FaceUpRatio);
                                    variousPostureData[1] = Float.parseFloat(FaceDownRatio);
                                    variousPostureData[2] = Float.parseFloat(RightLateralRatio);
                                    variousPostureData[3] = Float.parseFloat(LeftLateralRatio);

                                    postureNames[0] = postureNames[0]+ " "  +(variousPostureData[0]*100)+"%";
                                    postureNames[1] = postureNames[1]+ " "  +(variousPostureData[1]*100)+"%";
                                    postureNames[2] = postureNames[2]+ " "  +(variousPostureData[2]*100)+"%";
                                    postureNames[3] = postureNames[3]+ " "  +(variousPostureData[3]*100)+"%";


                                    createPieChart();
                                }else{
                                    variousPostureData[0] = Float.parseFloat("0.0");
                                    variousPostureData[1] = Float.parseFloat("0.0");
                                    variousPostureData[2] = Float.parseFloat("0.0");
                                    variousPostureData[3] = Float.parseFloat("0.0");
                                    //No Data provided
                                    createPieChart();
                                }
                            } catch (JSONException e) {
                                // If there is an error then output that
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
        // The request queue will automatically handle the request as soon as it can.
        arrReq.setShouldCache(false);
        getSleepDurationFromServer();
        requestQueue.add(arrReq);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_activity);
        final Calendar cal = Calendar.getInstance();
        yearInput = cal.get(Calendar.YEAR);
        monthInput = cal.get(Calendar.MONTH);
        dateInput = cal.get(Calendar.DAY_OF_MONTH);
        createDialogOnButtonClick();
        //createPieChart();
        sleepDurationText = (TextView)findViewById(R.id.TimeText);

        requestQueue = Volley.newRequestQueue(this);
        //requestSleepDuration = Volley.newRequestQueue(this);
    }
}
