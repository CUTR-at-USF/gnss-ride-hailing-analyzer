# GNSS Ride Hailing Analyser [![Java CI with Maven](https://github.com/CUTR-at-USF/gnss-ride-hailing-analyzer/actions/workflows/maven.yml/badge.svg)](https://github.com/CUTR-at-USF/gnss-ride-hailing-analyzer/actions/workflows/maven.yml)

Java Application for analysing GPS data points and additional datasets for identifying ride sharing and ride hailing trips.

## Datasets

The application currently supports the Chicago Ride Hailing Data.

The ultimate goal is to use the ride-hailing ground-truth data to identify ride-hailing trips in the raw GNSS traces.

The preliminary ride-hailing data is from
the [Chicago open TNC dataset](https://data.cityofchicago.org/Transportation/Transportation-Network-Providers-Trips/m6dm-c72p/data):

* Transportation_Network_Providers_reduced_records.csv (small subset of data)

## Prerequisites

Before building the program, make sure you have the Open Trip Planner installed and running. If you have trouble
installing the software, follow the steps to run the OTP server:

* Download the OTP Jar file from
  the [OTP-Maven-Repository](https://repo1.maven.org/maven2/org/opentripplanner/otp/2.0.0/otp-2.0.0-shaded.jar)
* Make sure you have the GTFS dataset you want to use and the OSM data of the location in the same folder
* Run the following in your command-line:-
    ```
    java -Xmx3G -jar otp-2.0.0-shaded.jar --build --serve path/to/folder
    ```

## Build

To build the application use `mvn clean package` command. This command will create a jar file (i.e.,
gnss-ride-hailing-analyzer-1.0.0-SNAPSHOT.jar)
under the target folder.

## Run

To run the application use `java -jar` command.

The first command-line parameter should be the filename of the Chicago dataset, 2nd parameter should be the folder where
you'd like to export your GTFS dataset (Optional), and the 3rd parameter should be the number of request you'd like to
process concurrently (Optional).

```
java -jar target/gnss-ride-hailing-analyzer-1.0.0-SNAPSHOT.jar path/to/file/filename2.csv export/to/folder(Optional) 10(Optional)
```

An `output.csv` file will be generated in the project root.

## License
```
/*
 * Copyright (C) 2021 University of South Florida
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```