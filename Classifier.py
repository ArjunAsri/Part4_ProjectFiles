#Classifier.py is used for the classification of the data received by the HTTP Server
import os #to handle file access
from matplotlib.colors import LogNorm #matplotlib required for generating pressure image 
import tensorflow as tf				#TensorFlow library for classification of data
from pathlib import Path			#Path library for getting the directory path
import time							
import cv2							#for reading image
import PIL							#for converting image from png to jpeg, which is then read for TensorFlow Classifier
from PIL import Image				#Again this is for png to JPEG
import sys #this is for inputs and outputs from the NodeJS server
import numpy as np 					#used to generate the pressure images
import matplotlib					#the matplotlib is used to generate pressure image
matplotlib.use('agg')				
import matplotlib.pyplot as plt		
import MySQLdb 						#Mysql db library for connecting to the database
from matplotlib.mlab import bivariate_normal

ImageCounter = 0			#This variable for used when images were stoerd for testing
RetrainedLabels = os.getcwd() + "/" + "retrained_labels.txt" #necessary to keep
testImageDirectory = os.getcwd() + "/TestData/Image_Saved_Nodejs.jpg"#Image_Saved_Nodejs
RetrainedGraph = os.getcwd() + "/" + "retrained_graph.pb"          #necessary to keep
PostureClassification = ""
classifications = []
#First Step we get the classifications
#RetrainedLabels contains the label names for the files
for currentLine in tf.gfile.GFile(RetrainedLabels):
    classifications.append(currentLine.rstrip()) #remove the extra stuff from the string


with tf.gfile.FastGFile(RetrainedGraph, 'rb') as retrainedGraphFile:
    graphObject = tf.GraphDef() #create a graphObject
    graphObject.ParseFromString(retrainedGraphFile.read())#read data from graph file into graph object
    _ = tf.import_graph_def(graphObject, name='') #import our graph as the current graph for the system
with tf.Session() as sess:
	filePath = Path('/home/ubuntu/GitCloneRepo/uoa_compsys_700_a_-_b_2018/text.txt')
	while True:
	#Kepp Checking for the arguments from NodeJS
	
		if(filePath.is_file()):
		    text_from_file = open('/home/ubuntu/GitCloneRepo/uoa_compsys_700_a_-_b_2018/text.txt','r')
		    line = text_from_file.readlines()
		    if(line.__len__()!=0):
			if(line[0]=="run"):
				print(line[0])
				text_from_file.close()
				os.remove('/home/ubuntu/GitCloneRepo/uoa_compsys_700_a_-_b_2018/text.txt')

				time.sleep(1)
				db = MySQLdb.connect(#host="localhost",
						     user="root",
						     passwd="",
						     db="nodemysql",
						     unix_socket="/opt/lampp/var/mysql/mysql.sock")
						     #port=10002)
				cur = db.cursor()

				cur.execute("SELECT * FROM TrainingSensorValues171 ORDER BY Time_Stamp DESC LIMIT 1")

				

				#index starts from row
				result = cur.fetchall()
				#for x in range(172):
				  #  print(row[x])

				db.close()
				SQL_Query_ID = result[0][0]
				rows = ["1", "2", "3", "4",
					      "5", "6", "7", "8" , "9" , "10" , "11" , "12" , "13", "14", "15"
				    , "16", "17", "18", "19"]
				cols = ["col 1", "col 2", "col 3", "col 4", "col 5", "col 6", "col 7", "col 8", "col 9"]

					#sensor array of 19*9 Soldier Posture
				sensors = np.array([
				[0,0,0,0,0,0,0,0,0],
				[0,0,0,0,0,0,37,15,0],
				[0,0,0,0,0,0,0,0,0],
				[0,5,0,16,12,6,263,513,5],
				[0,2,0,7,4,11,138,320,0],
				[0,3,0,14,15,5,164,470,1],
				[0,0,0,1,1,0,48,141,0],
				[0,0,0,0,0,0,17,48,0],
				[0,3,2,12,15,7,141,412,1],
				[0,14,0,41,10,4,303,466,2],
				[0,0,0,0,2,0,53,15,0],
				[0,0,0,0,0,0,0,0,12],
				[0,0,0,0,0,0,0,0,0],
				[0,0,0,0,0,320,0,1,0],
				[0,0,0,0,0,76,0,0,0],
				[0,0,0,0,0,0,0,0,0],
				[0,0,0,0,0,0,0,0,0],
				[0,0,0,377,0,0,2,5,0],
				[0,0,0,0,0,0,0,0,0]
				])
				for i,x in enumerate(sensors):
					for j,y in enumerate(x):
						sensors[i,j] = result[0][(3+(9*i)+j)]
				fig, ax = plt.subplots()
				fig.set_figheight(8)
				fig.set_figwidth(5)
				im = ax.imshow(sensors)

				ax.set_xticklabels([])
				ax.set_yticklabels([])
				#ImageCounter = ImageCounter+1;
				ax.tick_params(axis=u'both', which=u'both',length=0) #hide all the label ticks from the image

				#These paths will need to be changed for the python script to work and for the system to function appropriately
				plt.savefig("/home/ubuntu/GitCloneRepo/uoa_compsys_700_a_-_b_2018/TestData/Image_Saved_Nodejs.png",bbox_inches='tight')
				im = Image.open('/home/ubuntu/GitCloneRepo/uoa_compsys_700_a_-_b_2018/TestData/Image_Saved_Nodejs.png')
				im = im.convert("RGB");
				im.save('/home/ubuntu/GitCloneRepo/uoa_compsys_700_a_-_b_2018/TestData/Image_Saved_Nodejs.jpg','JPEG')
				os.remove("/home/ubuntu/GitCloneRepo/uoa_compsys_700_a_-_b_2018/TestData/Image_Saved_Nodejs.png")
				# Rotate the tick labels and set their alignment.
				plt.setp(ax.get_xticklabels(), rotation=45, ha="right", #column labels are at an angle
					 rotation_mode="anchor")
	
				ax.set_xticks(np.arange(len(cols)))
				ax.set_yticks(np.arange(len(rows)))
		
				ax.set_xticklabels(cols)
				ax.set_yticklabels(rows)
		
				for i in range(19): 
				    for j in range(9): 
					text = ax.text(j, i, sensors[i, j],
						       ha="center", va="center", color="w")

				ax.set_title("Pressure Sensors")
				fig.tight_layout()
				openCVImage = cv2.imread(testImageDirectory)
				finalTensor = sess.graph.get_tensor_by_name('final_result:0')
				tfImage = np.array(openCVImage)[:, :, 0:3]
				predictions = sess.run(finalTensor, {'DecodeJpeg:0': tfImage})
				sortedPredictions = predictions[0].argsort()[-len(predictions[0]):][::-1] 
			 	PostureClassification = classifications[sortedPredictions[0]]
			 	#print(sortedPredictions)
	
					#print(predictions[0][sortedPredictions[0]])  # this gives us the confidence number, that can be stored in the array
					#print(classifications[sortedPredictions[0]])
				print(str(PostureClassification));
				db = MySQLdb.connect(#host="localhost",
						     user="root",
						     passwd="",
						     db="nodemysql",
						     unix_socket="/opt/lampp/var/mysql/mysql.sock")
						     #port=10002)
				cur = db.cursor()

				sql_query = "UPDATE TrainingSensorValues171 SET Classification ='"+str(PostureClassification)+"' WHERE id ="+ str(SQL_Query_ID)

				#print(sql_query)
				cur.execute(sql_query)
				db.commit()
				#print("db.commit()")
				# print all the first cell of all the rows

				# index starts from row

				db.close()
