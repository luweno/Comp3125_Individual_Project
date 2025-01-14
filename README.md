# Hypixel Skyblock Economy Analysis
This was a project completed for a course project.
My goal was to provide insight into how the economy works, and various aspects of it. Primarily, I wanted to apply this knowledge while I am away studying since I have limited time to revisit it.

## Dependencies / Packages: 
XGBoost, Pandas, Numpy, Sklearn, Graphvis
  > With Graphvis you may need to configure environment variables to view graphs.


## How to Run:
  You can find the primary notebook in the 'Code/Python' folder
  So long as you have the necessary dependencies and have a kernel setup with Python 3+ you can run all and it will work.
  If you're interested in the API part, you can view the 'Code/Java' folder
    In terms of running this, head to https://developer.hypixel.net/dashboard to get a key for testing
    It is compilable with Maven, you will have to create a project for it if you want to test it (I have removed all things relating to that)
    Essentially, Set the api key ('HYPIXEL_API_KEY') = your api key, and then run the compiled Jar.

## Navigation:
You can find all the code in the /Code folder, I have it seperated from Python to Java
All the graphs have been captured in case there is any error in /Graphs 
In the Data folder, there are 2 data sets. One is the original ('Skyblock.csv')
and the other has the feature engineered points ('test.csv')

## Concerns:
If there are any concerns, reach out please!
