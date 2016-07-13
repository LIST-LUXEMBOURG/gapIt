# gapIt

Copyright 2014 - 2016 Luxembourg Institute of Science and Technology (LIST - [http://www.list.lu/](http://www.list.lu/)). Any use of this software constitutes full acceptance of all terms of the software's license.


## Synopsis

gapIT is an interactive software for gap filling in hydrological time series.

Given as input hydrological discharge data series measured at gauging stations, it performs an automated calculation of missing data using different data-infilling techniques. Donor station(s) are automatically selected based on Dynamic Time Warping, geographical proximity and upstream/downstream relationships among stations. For each gap, the tool computes several flow estimates through various data-infilling techniques, including interpolation, multiple regression, regression trees and neural networks. The visual application provides the possibility for the user to select different donor station(s) w.r.t. those automatically selected.

The results are validated by randomly creating artificial gaps of different lengths and positions along the entire records. Using the Root Mean Squared Error and the Nash-Sutcliffe coefficient as performance measures, the method is evaluated based on a comparison with the actual measured discharge values.

The interactive but automated approach of gapIT, coupled with a visual inspection system for user-defined refinement, allows for a standardized objective infilling, where subjective decisions are allowed but are at the same time traceable.


## Motivation

Missing records in hydrological databases represent a loss of information and a serious drawback in water management (measures of river flow for example). An incomplete time series prevents the computation of hydrological statistics and indicators. Also, records with data gaps are not suitable as input or validation data for hydrological or hydrodynamic modeling.


## Installation / execution

### Requirements

Hardware: RAM>=4Gb.

Software: 
* JDK (>=1.7 - [http://www.oracle.com/technetwork/java/javase/downloads/] (http://www.oracle.com/technetwork/java/javase/downloads/)).
* Maven (>=3.0.2 - [https://maven.apache.org/] (https://maven.apache.org/))

### Compilation

launch 'mvn clean package -Dmaven.test.skip=true'  
(*some tests are currently not ok, and the build fails without the '-Dmaven.test.skip=true' option*)

### Data preparation

unzip [data_fake2.zip] (./data_fake2.zip) in the same directory

### Execution for Windows OS

launch 'run.bat'

### Execution for other OS

launch 'java -jar -Xmx4G target/gapIt.jar'


## Data

gapIt was used to fill gaps in water discharge time series at Luxembourg (see the 'References' section).
During tests phases, it was used too for the processing of water level time series.

Here, fake data are provided within the distribution [data_fake2.zip] (./data_fake2.zip). These data were derived from real data using a random method. To get the real data or to integrate your own data, please contact Olivier Parisot (olivier.parisot@list.lu) or Ivonne Trebs (ivonne.trebs@list.lu).

In this archive ([data_fake2.zip] (./data_fake2.zip)), the required data files are:
* *all_valid_q_series_complete2.arff*: the time series representing the measured discharge for each station, under the [ARFF format] (https://weka.wikispaces.com/ARFF).
* *knowledgeDB20-discharge.arff*: the knowledge database containing the Case-Based Reasoning data, under the ARFF format too.
* *stations_coordinates.txt*: the coordinates of each station.
* *stations_relationships_1.xml*: the upstream/downstreams relationships among the stations (file 1/2).
* *stations_relationships_2.xml*: the upstream/downstreams relationships among the stations (file 2/2).
* *shapeCountry.jpg*: a picture to show the shape of the studied country.


## Screenshots

![pic1](/pictures/picture0.png)

![pic2](/pictures/picture2.png)

![pic3](/pictures/picture3.png)


## Video

[gapIt short demo] (/video/GapIT_Video2.wmv)


## References

This section lists scientific publications in which gapIt is used. 

 * Laura Giustarini, Olivier Parisot, Mohammad Ghoniem, Renaud Hostache, Ivonne Trebs, Benoît Otjacques: **«A user-driven case-based reasoning tool for infilling missing values in daily mean river flow records»**, «Environmental Modelling and Software», Elsevier, 5/2016 [(link)] (http://www.sciencedirect.com/science/article/pii/S1364815216301050)

 * Laura Giustarini, Olivier Parisot, Mohammad Ghoniem, Renaud Hostache, Ivonne Trebs, Benoît Otjacques:**«gapIT: a user-driven case-based reasoning tool for infilling missing values in daily mean river flow records»**, «Tag der Hydrologie 2016», Koblenz, Germany, 17/3/2016 [(link)] (https://scholar.google.lu/citations?view_op=view_citation&hl=fr&user=OeqhaZ4AAAAJ&sortby=pubdate&citation_for_view=OeqhaZ4AAAAJ:4TOpqqG69KYC)

 * Laura Giustarini, Olivier Parisot, Mohammad Ghoniem, Ivonne Trebs, Nicolas Médoc, Olivier Faber, Renaud Hostache, Patrick Matgen, Benoît Otjacques: **«Data-infilling in daily mean river flow records: first results using a visual analytics tool (gapIT)»**, «European Geosciences Union General Assembly 2015 (EGU 2015), Geophysical Research Abstracts Volume 17», Vienna, Austria, 4/2015 [(link)] (http://adsabs.harvard.edu/abs/2015EGUGA..1710462G)

 * Olivier Parisot, Laura Giustarini, Olivier Faber, Renaud Hostache, Ivonne Trebs, Mohammad Ghoniem: **«gapIT: Un outil visuel pour l'imputation de valeurs manquantes en hydrologie»**, «15ème conférence internationale sur l'extraction et la gestion des connaissances (EGC 2015)», Luxembourg, 1/2015 [(link)]  (http://editions-rnti.fr/?inprocid=1002107&PHPSESSID=ks64gh4ktvuilvujuu9lcva5i4&lg=en&PHPSESSID=ks64gh4ktvuilvujuu9lcva5i4)

If you also have employed gapIt, send an email to olivier.parisot@list.lu to have your publication listed here.


## License

Licensed under GNU General Public License version 3, see [LICENSE.txt] (./LICENSE.txt).
