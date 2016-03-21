## Synopsis

gapIT is an interactive software for gap filling in hydrological time series.

Given as input hydrological discharge data series measures at gauging stations, it performs an automated calculation of missing data using different data-infilling techniques. Donor station(s) are automatically selected based on Dynamic Time Warping, geographical proximity and upstream/downstream relationships among stations. For each gap, the tool computes several flow estimates through various data-infilling techniques, including interpolation, multiple regression, regression trees and neural networks. The visual application provides the possibility for the user to select different donor station(s) w.r.t. those automatically selected.

The results are validated by randomly creating artificial gaps of different lengths and positions along the entire records. Using the Root Mean Squared Error and the Nash-Sutcliffe coefficient as performance measures, the method is evaluated based on a comparison with the actual measured discharge values.

The interactive but automated approach of gapIT, coupled with a visual inspection system for user-defined refinement, allows for a standardized objective infilling, where subjective decisions are allowed but are at the same time traceable.


## Motivation

Missing records in hydrological databases represent a loss of information and a serious drawback in water management (measures of river flow for example). An incomplete time series prevents the computation of hydrological statistics and indicators. Also, records with data gaps are not suitable as input or validation data for hydrological or hydrodynamic modeling.


## Installation

Requirements: Maven (>=3.0.2), JDK (>=1.7).
Compilation: "mvn clean package"
Execution for Windows environment: "run.bat"
Execution for others: "java -jar -Xmx4G target/gapIt.jar"


## Data

Fake data are provided with the distribution.

To get the real data, please contact Ivonne Trebs (ivonne.trebs@list.lu).


## References

Laura Giustarini, Olivier Parisot, Mohammad Ghoniem, Renaud Hostache, Ivonne Trebs, Benoît Otjacques: «gapIT: a user-driven case-based reasoning tool for infilling missing values in daily mean river flow records», «Tag der Hydrologie 2016», Koblenz, Germany, 17/3/2016

Laura Giustarini, Olivier Parisot, Mohammad Ghoniem, Ivonne Trebs, Nicolas Médoc, Olivier Faber, Renaud Hostache, Patrick Matgen, Benoît Otjacques: «Data-infilling in daily mean river flow records: first results using a visual analytics tool (gapIT)», «European Geosciences Union General Assembly 2015 (EGU 2015), Geophysical Research Abstracts Volume 17», Vienna, Austria, 4/2015

Olivier Parisot, Laura Giustarini, Olivier Faber, Renaud Hostache, Ivonne Trebs, Mohammad Ghoniem: «gapIT: Un outil visuel pour l'imputation de valeurs manquantes en hydrologie», «EGC 2015», Luxembourg, 1/2015


## License

GPL v3, see LICENSE.txt.
