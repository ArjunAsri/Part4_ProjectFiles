/*This is a HTTP Server written in NodeJS using the Express framework for POST and Get Requests for SleepMat-e Android App and Microcontroller
*/
var express = require('express'); //Express web framework POST and GET requests
var app = express();
var path = require('path'); 
var bodyParser = require('body-parser');
var mysql = require('mysql');

var previousClassification = 'faceup';
var bedSoreCounter = 0;
var bedSoreAlert = 0;
// Configure MySQL connection
var connection = mysql.createConnection({
	host: 'localhost',
	user: 'root',
	password: '',
    database: 'nodemysql',
    port: '10002'
  })

  /*All functions for post and get requests go after the body parser, functions that need body parser  */
app.use(bodyParser.json())//Parse data from JSON POST and insert into MYSQL

/*Function to print the errors in the console and the error code, this is called in various POST and GET requests whenver there
is an error */
function printErrorsInConsole(err){
    console.log(err);
    console.log(err.code);
}

//Stored Procedure for creating 171 columns
  var create171Columns = "CREATE PROCEDURE create171New()\n" +
                            "BEGIN\n"+
                                "DECLARE x INT; \n" +
                                "DECLARE str VARCHAR(4000);\n"+
                                "SET x = 1;\n" +
                                "REPEAT\n"+
                                    "SET str = '';\n"+
                                    "SET str = CONCAT(str,'Sensor',x);\n"+
                                    "SET x = x + 1;\n"+
                                    "SET @query = 'ALTER TABLE TrainingSensorValues ADD ' + str + 'INT';\n"+
                                    "PREPARE stmt FROM @query;\n"+
                                    "EXECUTE stmt;\n"+
                                    "UNTIL x > 171\n"+
                                "END REPEAT;\n"+
                            "END";


/*Establish connetion with MYSQL database, if the connection is not successfull, the server will terminate with the error */
connection.connect(function(err) {
   if (err) {
       throw err;
   } else {
       console.log('Connected to MySQL');
       app.listen(3000);
       console.log('Server listening on port 3000');
       /*Server is now up and connected with MySQL*/ 
 }
});


/*API /createdb - This API is used for create a database, If the database exists then this function will do nothing */
app.get('/createdb',(req,res)=>{
    let sql = 'CREATE DATABASE IF NOT EXISTS nodemysql';
    connection.query(sql,(err, result)=>{
        if(err) throw err;
        res.send('Database Created!');
    });
});

/*API /createTable - createTable API is for creating Table*/
app.get('/createTable',(req,res)=>{
    //let sql = 'CREATE TABLE IF NOT EXISTS TrainingSensorValues (id INT AUTO_INCREMENT PRIMARY KEY, Time_Stamp TIMESTAMP, S0 INT, S1 INT, S2 INT, S3 INT)';
    let sql = 'CREATE TABLE IF NOT EXISTS TrainingSensorValues171 (id INT AUTO_INCREMENT PRIMARY KEY, Time_Stamp TIMESTAMP, Classification varchar(255))';

    connection.query(sql,(err,result)=>{
        if(err) throw err;
        res.send('Table Created!');
    })
});

/*API /sendValues - This API was only used for small scale testing, when the design with 4 sensors was being tested for feasibility 
    PARAMTER - JSON packet with 4 sensor values, {S1:100,S2:100,S3:100,S4:100}*/
app.post('/sendValues',(req,res)=>{
    let data = req.body;
    let timestamp = new Date().getTime();
    let sql = 'INSERT INTO TrainingSensorValues ( Time_Stamp ?, S0 ?, S1 ?, S2 ?, S3 ?)';
    connection.query(sql,[timestamp,body.S0,body.S1,body.S2,body.S3],(err,result,fields)=>{
        if(err) {
            console.log(err);
            console.log(err.code);
            throw err;
        }
        res.send('Values Successflly added to the database!');
    });

});

/*API /updateColumn was only used for testing mysql update query and for debugging */
app.get('/updateColumn',(req,res)=>{
    let sql = "UPDATE TrainingSensorValues SET S2=50 WHERE id = 1759";
    connection.query(sql,(err, res)=>{
        if(err){
            printErrorsInConsole(err);
            throw err;
        }else{
            res.send("Field Update procedure called successfully!");
        }
    });
})


/*API /sendData - This API is used by Microcontroller to send 171 Values of Data, the system */
app.post('/sendData',(req,res)=>{
console.log("/sendData API called");
    var x = 0;
    let data = req.body;
    /*Console Log print for TESTING 
    console.log("First Sensor Value is: "+ req.body.S0);
    console.log("Sensor171 value is: "+req.body.S170);
    console.log("Length of the string is "+ req.body.length);//the length function is not working
    */
    //We add the first line to the TrainingSensorValues
	
	console.log(req.body);
    let sql = 'INSERT INTO TrainingSensorValues171 (S0) VALUES (?)';
    connection.query(sql,req.body.S[0],(err,result,fields)=>{
            if(err) {
                printErrorsInConsole(err);
                throw err;
            }else{
                //console.log("First Sensor value successfully added!");
            }
        });

    let getID_sql = "SELECT id FROM TrainingSensorValues171 ORDER BY id DESC LIMIT 1";
    var ID = 0;
    connection.query(getID_sql,(err,result)=>{
        if(err){
            printErrorsInConsole(err);
        }else{
            ID = result;
            ID = ID[0].id;
            console.log(ID);

            for(var i = 1; i < 171; i++){//loop has to have 171 values
                let index = "S"+i;
               // console.log("ID is: "+ ID);
                let sql_update = "UPDATE TrainingSensorValues171 SET " + "S"+i +   "=?"  + " WHERE id = "+ID ;
                //console.log(sql_update);
        //let sql1 = 'INSERT INTO TrainingSensorValues S0 ?';
                connection.query(sql_update,data.S[i],(err,result,fields)=>{
                if(err) {
                    printErrorsInConsole(err);
                    throw err;
                        }
                 });
            };
            //console.log(ID[0].id); //This is needed to access the data in the return packet from Mysql
        }

    });
    
    var fs = require('fs');
    fs.writeFile("/home/ubuntu/GitCloneRepo/uoa_compsys_700_a_-_b_2018/text.txt", "run", function(err) {
        if(err) {
            return console.log(err);
        }
    
        //console.log("File saved");
    });

 
    res.send('All 171 Values Successflly added to the database!');
    //res.send("1 Value Added!");
});

/*API - getlastImageClassification returns the image classification of 
last posture data received by the system */
app.get('/getlastImageClassification',(req,res)=>{
    let getID_sql = "SELECT id FROM TrainingSensorValues171 ORDER BY id DESC LIMIT 1";
    var ID = 0;
    var posture = "0";
    connection.query(getID_sql,(err,result)=>{
        if(err){
            printErrorsInConsole(err);
        }else{
            ID = result;
            ID = ID[0].id;//extracting info from the received data from the database
            let sql_update = "SELECT Classification FROM TrainingSensorValues171 WHERE id = ?" ;
 
                connection.query(sql_update,[ID],(err,result,fields)=>{//the arguments have to be specified in square brackets
                if(err) {
                    printErrorsInConsole(err);
                    throw err;
                        }else{
                            posture = result[0].Classification; //access the classification information		    
			    
			    console.log(posture);
			    res.setHeader('Content-Type','application/json');
                            res.send(JSON.stringify({"Classification":posture}));
                        }
                 });
            };
            //console.log(ID[0].id); //This is needed to access the data in the return packet from Mysql
        });
    
});

/*API: getPostureRatio provides the Posture Ratio based on the client ID and the date-time arguments
that are passed, the output is a json string of all the postures and the associated value of how long the person 
has slept in those postures and the ratio along side 
Example: http://localhost:3000/getPostureRatio/10/12/2018 */
app.get("/getPostureRatio/:day/:Month/:Year",(req,res)=>{
	var day = req.params.day;
	var Month = req.params.Month;
	var year = req.params.Year;

	var startDate = year+"-"+Month+"-"+day;
	var endDate = year+"-"+Month+"-"+(parseInt(day)+1);
	var FaceUpCount = 0, FaceDownCount = 0, rightLateralCount = 0, leftLateralCount = 0;
	console.log(startDate);
	let getID_sql = "SELECT * FROM TrainingSensorValues171 WHERE Time_Stamp >= ('"+startDate+" 12:00:00') AND Time_Stamp < ('"+endDate+" 12:00:00')";
connection.query(getID_sql,(err,result)=>{
	if(err){
		printErrorsInConsole(err);	
	}else{
		for(var i =0; i < (result.length);i++){

			var strcmp = result[i].Classification;
			strcmp = String(strcmp)
			if(strcmp=="faceup"){
				FaceUpCount++;
			}else if(strcmp == "facedown"){
				FaceDownCount++;
			}else if(strcmp=="rightlateral"){
				rightLateralCount++;
			}else if(strcmp=="leftlateral"){

				leftLateralCount++;
			}
		}

	}

	let totalCount = FaceUpCount+FaceDownCount+rightLateralCount+leftLateralCount;
	var faceUpRatio = (FaceUpCount/totalCount).toFixed(2);
	var faceDownRatio = (FaceDownCount/totalCount).toFixed(2);
	var rightlateralRatio = (rightLateralCount/totalCount).toFixed(2);
	var leftLateralRatio = (leftLateralCount/totalCount).toFixed(2);
	var RatioSum = (faceUpRatio+faceDownRatio+rightlateralRatio+leftLateralRatio);
	console.log("Posture Distribution");
	res.setHeader('Content-Type','application/json');
	res.send(JSON.stringify({"FaceUpRatio":String(faceUpRatio),"FaceDownRatio":String(faceDownRatio),"RightLateralRatio":String(rightlateralRatio),"LeftLateralRatio":String(leftLateralRatio)}));
});


});

var query = [];
query.push(create171Columns)

/*API /getTimeInBed = This API requires arguments in the url that are day, month and year
Example: http://localhost:3000/getTimeInBed/10/12/2018 
If there is no data for that date, this will be the output, The output is in JSON format
Output: {
    "FaceUpRatio": "NaNf",
    "FaceDownRatio": "NaNf",
    "RightLateralRatio": "NaNf",
    "LeftLateralRatio": "NaNf"
}*/
app.get("/getTimeInBed/:day/:Month/:Year",(req,res)=>{
    var day = req.params.day;
    var Month = req.params.Month;
    var Year = req.params.Year;

 var startDate = Year+"-"+Month+"-"+day;
    console.log(startDate);
    var endDate = Year+"-"+Month+"-"+(parseInt(day)+3);
    //console.log(endDate);
    var FaceUpCount = 0, FaceDownCount = 0, rightLateralCount = 0, leftLateralCount =0;
    var TotalTime = 0, counter = 0;
    var addedTime = 0;

let getID_sql = "SELECT * FROM TrainingSensorValues171 WHERE Time_Stamp >= ('"+startDate+" 12:00:00') AND Time_Stamp < ('"+endDate+" 12:00:00')";

 connection.query(getID_sql,(err,result)=>{
        if(err){
            printErrorsInConsole(err);
        }else{
if(result.length>20) {
  var StartTime = result[0].Time_Stamp;
            StartTime = Math.round(new Date(String(StartTime)).getTime()/1000);
            //console.log("StartTime: "+StartTime);
            var EndTime = result[result.length-1].Time_Stamp;
            EndTime =   Math.round(new Date(String(EndTime)).getTime()/1000);
            //console.log("EndTime: "+EndTime);
var Start_EndTimeDifference = EndTime - StartTime;
            var PreviousStartTime = StartTime;
for(var i = 0; i < (result.length);i++){
                
                   // console.log(result[i].Time_Stamp);
                    //console.log(result[i].Classification);
                    var strcmp = result[i].Time_Stamp;
                    strcmp = String(strcmp);
 if(String(result[i].Classification)!="emptybed"){
                        //console.log(result[i].Classification);
                        let UnixTimeStamp = Math.round(new Date(String(result[i].Time_Stamp)).getTime()/1000);
                        addedTime += UnixTimeStamp-PreviousStartTime;
PreviousStartTime = UnixTimeStamp;

 }else{
                        //Empty Bed Condition Found ..Reset Time to count for the next block of time and then add back
                        TotalTime += addedTime;
                        //console.log("Total Time:"+ TotalTime);
                        addedTime = 0; //addedTime has been reset
                        PreviousStartTime = Math.round(new Date(String(result[i].Time_Stamp)).getTime()/1000);
 }
                    //console.log("ConvertedTimeStamp:"+UnixTimeStamp);
            }
            //console.log("Total time: "+TotalTime);
        }
}
 var Hours =0, Minutes = 0; Seconds = 0;

    /*Conversion of the collected Time data to Hours, Minutes and Seconds */
    Minutes = parseInt(TotalTime/ 60);
    Hours = parseInt(Minutes / 60);
    Seconds = TotalTime % 60;
 Minutes = parseInt(Minutes% 60);
    console.log("Sleep Duration");
    res.setHeader('Content-Type','application/json');
    res.send(JSON.stringify({"OverallTimeInSeconds":String(TotalTime),"Hours":String(Hours),"Minutes":String(Minutes),"Seconds":String(Seconds)}));
    });
});


/*API /getBedSoreAlert - API used to provide Bed Sore Alert, the api will return true if a time threshold 
has been reached for a given posture
This API returns a JSON output {"BedSore": 0} '0' indicates no bedsore warning, '1' indicates bed sore warning*/
app.get("/getBedSoreAlert",(req,res)=>{
	let getID_sql = "SELECT id FROM TrainingSensorValues171 ORDER BY id DESC LIMIT 1";
	var ID = 0;
	var Classification = "bedUnoccupied";
	connection.query(getID_sql,(err,result)=>{
		if(err){
			printErrorsInConsole(err);
		}else{
			ID = result;
			ID = ID[0].id;
			let sql_update = "SELECT Classification FROM TrainingSensorValues171 WHERE id = ?";
 			connection.query(sql_update,[ID],(err,result,fields)=>{
		if(err){
			printErrorsInConsole(err);
		}else{
			if((result[0].Classification == 'emptybed' )||(result[0].Classification=='undefined')){
				bedSoreAlert = 0;
				bedSoreCounter = 0;
			}else{
				if(bedSoreCounter < 50){
					if(previousClassification == result[0].Classification){
						previousClassification = result[0].Classification;
						bedSoreAlert = 0;
						bedSoreCounter++;
					}else{
						previousClassification = result[0].Classification;
						bedSoreCounter = 0;
						bedSoreAlert = 1;
					}
				}else{
					bedSoreCounter = 0;
					bedSoreAlert = 1;
			}
		}
		console.log(bedSoreCounter);
		console.log(result[0].classification);
		console.log("Bed Sore API called");
		res.setHeader('Content-Type','application/json');
		res.send(JSON.stringify({"BedSore":bedSoreAlert}));
		}
		});
	};
	});
});	



/*API /createTableProcedure - This API allowed to create the the Table procedure that was defined around the start of this file*/
app.get('/createTableProcedure',(req,res)=>{
    connection.query(query[0],function(err, rows, fields){
        if(err){
            console.log(err.code);
            console.log(err.message);
            res.send("Error creating stored Procedure");
        }else{
            res.send('Success!');
        }
    })
})


/* API /addColumn - This API was used to add 171 columns to the table which is used to store all the data that is sent by the microcontorller
The API works by using a for loop to create and run query "ALTER TABLE" 171 times */
app.get('/addColumn',(req,res)=>{
   
  var x= 0;
  for(var i = 0; i < 171 ;i++){
    let sql = "ALTER TABLE TrainingSensorValues171 ADD ("+ "S"+x +" INT)" ; 
    connection.query(sql,(err,result)=>{
        if(err){
            console.log(err.code);
            console.log(err.message);
        }
    })
    x = x+1;
  }
  res.send('Column Added!');
    
})

/*API /getLastestData - Returns a JSON string of all the readings from the alst frame taht was sent by the microcontroller,
along with the classification of the data, it may be undefined if it has not been classified yet. Again this API was for debugging*/
app.get('/getLatestData',(req,res)=>{

	let sql = 'SELECT * FROM TrainingSensorValues171 ORDER BY Time_Stamp DESC LIMIT 1';
	connection.query(sql,(err,result,fields) =>{
	
	if(err) throw err;
	console.log(result);
	res.send(result);
	})


});

/*API /create171Table - This API was meant to use the Stored Procedure to create a table with 171 columns */
app.get('/create171Table',(req,res)=>{
    let sql = 'CALL create171New' //call the function
    connection.query(sql, true, (err, resutls, fields)=>{
        if(err){
            console.log(err.code);
            console.log(err.message);
            res.send("Error when calling stored Procedure");
        }else{
            res.send("Success!")
        }
    })
})

/*---------------------------------------------------WEB PAGE APIS---------------------------------------- */
/*We initially had plans to create a website that could also provide information and may be more detailed Statistical data. 
However, due to time limitation we were unable to have a web server */
/*Login Page for the APP and development Page */
app.get('/', function(req, res) {
  res.sendFile(path.join(__dirname+ '/myfile.html'));
});


/*Developer page when logged in */
app.get('/dev',function(req,res){
    res.send("This will be the developer page for SleepMat-e");
})


