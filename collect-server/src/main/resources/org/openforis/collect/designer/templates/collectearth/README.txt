This CEP (Collect Earth Project) file has been created with Open Foris Collect Survey Designer ( http://www.openforis.org/tools/collect.html )



 --- ABOUT THE CEP FILE --- 
This CEP file is simply a ZIP file whose extension was changed to CEP in order to be able to "link" it to Collect Earth so it could be opened by double-clicking on it.
It contains all the necessary files that would make it work in Collect Earth.
You might uncompress the contents of this file into a directory, perform changes in hte files and them compress(zip) them back onto a file, renaming its file extension to CEP. 
When building this ZIP ( or rather CEP) file make sure that the files in the folder end up in the root, so that placemark.idm.xml, project_definition.properties and the 
rest are on the root of the ZIP file. 



 --- YOUR OWN SAMPLING DESIGN --- 
This project includes a "dummy" sampling design (called test_plots.ced) with 15 plots located around the world. 
This file is set as the default value of the "csv" property in the "project_definition.properties" file, meaning that 
when a user open the CEP file for the first time, these dummy plots will be shown in Google Earth.

In order to create your own sampling design for this project you need to follow these steps

1. Using Quantum GIS or Google Earth Engine to generate the sampling design ( the grid of plots ) that you want to assess. 
   Follow this manual if you want to use  QGis : http://www.openforis.org/tools/collect-earth/tutorials/qgis.html
   Links Google Earth Engine scripts (you will need to be a Google Trusted Tester) :
   		- Systematic sampling design on a polygon : https://ee-api.appspot.com/806951f46d102f7b77cb54b9ab79a821 
   		- Random sampling design : https://ee-api.appspot.com/b255dbc07dcdc59ebd99f8a364aadda3  
   		- Example for Spain provinces : https://ee-api.appspot.com/806951f46d102f7b77cb54b9ab79a821
   		

2. Make sure that the Comma Separated Values (CSV) file that you generated has THE EXACT SAME structure than the test_plots.ced file. This is very important.
   In order for Collect Earth to work this is completely necessary. Make sure that your sampling design file looks like the test_plots.ced contents (you don't 
   need to worry about the double-quotes in the columns though)

3. Place the CSV file (or file) inside the folder where the CEP files were uncompressed. So for instance the "sampling_grid.ced" file will be in the same folder as "test_plots.ced".  

3. Edit the "project_definition.properties" file. This file contains the basic configuration for a project that will be loaded when the CEP file is double clicked.
   Find the line where it says : csv=${project_path}/test_plots.ced . Edit it so that if you for instance placed a CED file in the root folder of the project called 
   "sampling_grid.csv" then the result would be :   
   csv=${project_path}/sampling_grid.csv



 --- ASSIGNING AREAS TO REGION ( AREA CALCULATIONS IN SAIKU) --- 
In order for Saiku to be able to show the user Area calculations it is first necessary to define the areas (in hectares) that are being assessed.
Please see the answer to this support issue to understand how to achieve area calculations :

http://www.openforis.org/support/questions/219/areas-calculated-on-saiku

  
  
  