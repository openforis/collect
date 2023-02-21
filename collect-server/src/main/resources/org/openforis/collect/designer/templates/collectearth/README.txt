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

In order to create your own sampling design for this project you can use the Google Earth Engine app to Generate Grids:
https://collectearth.users.earthengine.app/view/collect-earth-grid-generator

See this video for instructions on how to use it : https://www.youtube.com/watch?v=Nad3gPLV9DU

 --- ASSIGNING AREAS TO REGION ( AREA CALCULATIONS IN SAIKU) --- 
In order for Saiku to be able to show the user Area calculations it is first necessary to define the areas (in hectares) that are being assessed.
Please see the answer to this support issue to understand how to achieve area calculations :

https://www.openforis.support//questions/219/areas-calculated-on-saiku#gsc.tab=0

  
  
  