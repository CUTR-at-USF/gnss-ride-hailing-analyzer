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
package edu.usf.cutr.grha.model

import com.univocity.parsers.annotations.Parsed

data class ChicagoTncData
(
    @Parsed val tripId: String = "",
    @Parsed val tripStartTimeStamp: String = "",
    @Parsed val tripEndTimeStamp: String = "",
    @Parsed val tripSeconds:  Int = 0,
    @Parsed val tripMiles: Double = 0.0,
    @Parsed val pickupCensusTract: String = "",
    @Parsed val dropoffCensusTract: String = "",
    @Parsed val pickupCommunityArea: Int = 0,
    @Parsed val dropoffCommunityArea: Int = 0,
    @Parsed val fare: Double = 0.0,
    @Parsed val tip: Double = 0.0,
    @Parsed val additionalCharges: Double = 0.0,
    @Parsed val tripTotal: Double = 0.0,
    @Parsed val sharedTripAuthorized: String = "",
    @Parsed val tripsPooled: Int = 0,
    @Parsed val pickupCentroidLatitude: Double = 0.0,
    @Parsed val pickupCentroidLongitude: Double = 0.0,
    @Parsed val dropoffCentroidLatitude: Double = 0.0,
    @Parsed val dropoffCentroidLongitude: Double = 0.0,

    @Parsed var optimizeType: String? = null,

    @Parsed var totalTravelTime1: Int? = 0,
    @Parsed var totalDistance1: Double? = 0.0,
    @Parsed var totalWaitTime1: Int? = 0,
    @Parsed var altitudeChange1: Double? = 0.0,
    @Parsed var transfers1: Int? = 0,
    @Parsed var Modes1: String? = "",

    @Parsed var walkTime1: Double? = 0.0,
    @Parsed var busTime1: Double? = 0.0,
    @Parsed var transitTime1: Double? = 0.0,
    @Parsed var bicycleTime1: Double? = 0.0,
    @Parsed var bicycleRentTime1: Double? = 0.0,
    @Parsed var bicycleParkTime1: Double? = 0.0,
    @Parsed var carTime1: Double? = 0.0,
    @Parsed var carParkTime1: Double? = 0.0,
    @Parsed var tramTime1: Double? = 0.0,
    @Parsed var subwayTime1: Double? = 0.0,
    @Parsed var railTime1: Double? = 0.0,


    @Parsed var walkDistance1: Double? = 0.0,
    @Parsed var busDistance1: Double? = 0.0,
    @Parsed var transitDistance1: Double? = 0.0,
    @Parsed var bicycleDistance1: Double? = 0.0,
    @Parsed var bicycleRentDistance1: Double? = 0.0,
    @Parsed var bicycleParkDistance1: Double? = 0.0,
    @Parsed var carDistance1: Double? = 0.0,
    @Parsed var carParkDistance1: Double? = 0.0,
    @Parsed var tramDistance1: Double? = 0.0,
    @Parsed var subwayDistance1: Double? = 0.0,
    @Parsed var railDistance1: Double? = 0.0,


    @Parsed var totalTravelTime2: Int? = 0,
    @Parsed var totalDistance2: Double? = 0.0,
    @Parsed var totalWaitTime2: Int? = 0,
    @Parsed var altitudeChange2: Double? = 0.0,
    @Parsed var transfers2: Int? = 0,
    @Parsed var Modes2: String? = "",

    @Parsed var walkTime2: Double? = 0.0,
    @Parsed var busTime2: Double? = 0.0,
    @Parsed var transitTime2: Double? = 0.0,
    @Parsed var bicycleTime2: Double? = 0.0,
    @Parsed var bicycleRentTime2: Double? = 0.0,
    @Parsed var bicycleParkTime2: Double? = 0.0,
    @Parsed var carTime2: Double? = 0.0,
    @Parsed var carParkTime2: Double? = 0.0,
    @Parsed var tramTime2: Double? = 0.0,
    @Parsed var subwayTime2: Double? = 0.0,
    @Parsed var railTime2: Double? = 0.0,


    @Parsed var walkDistance2: Double? = 0.0,
    @Parsed var busDistance2: Double? = 0.0,
    @Parsed var transitDistance2: Double? = 0.0,
    @Parsed var bicycleDistance2: Double? = 0.0,
    @Parsed var bicycleRentDistance2: Double? = 0.0,
    @Parsed var bicycleParkDistance2: Double? = 0.0,
    @Parsed var carDistance2: Double? = 0.0,
    @Parsed var carParkDistance2: Double? = 0.0,
    @Parsed var tramDistance2: Double? = 0.0,
    @Parsed var subwayDistance2: Double? = 0.0,
    @Parsed var railDistance2: Double? = 0.0,

    @Parsed var totalTravelTime3: Int? = 0,
    @Parsed var totalDistance3: Double? = 0.0,
    @Parsed var totalWaitTime3: Int? = 0,
    @Parsed var altitudeChange3: Double? = 0.0,
    @Parsed var transfers3: Int? = 0,
    @Parsed var Modes3: String? = "",

    @Parsed var walkTime3: Double? = 0.0,
    @Parsed var busTime3: Double? = 0.0,
    @Parsed var transitTime3: Double? = 0.0,
    @Parsed var bicycleTime3: Double? = 0.0,
    @Parsed var bicycleRentTime3: Double? = 0.0,
    @Parsed var bicycleParkTime3: Double? = 0.0,
    @Parsed var carTime3: Double? = 0.0,
    @Parsed var carParkTime3: Double? = 0.0,
    @Parsed var tramTime3: Double? = 0.0,
    @Parsed var subwayTime3: Double? = 0.0,
    @Parsed var railTime3: Double? = 0.0,


    @Parsed var walkDistance3: Double? = 0.0,
    @Parsed var busDistance3: Double? = 0.0,
    @Parsed var transitDistance3: Double? = 0.0,
    @Parsed var bicycleDistance3: Double? = 0.0,
    @Parsed var bicycleRentDistance3: Double? = 0.0,
    @Parsed var bicycleParkDistance3: Double? = 0.0,
    @Parsed var carDistance3: Double? = 0.0,
    @Parsed var carParkDistance3: Double? = 0.0,
    @Parsed var tramDistance3: Double? = 0.0,
    @Parsed var subwayDistance3: Double? = 0.0,
    @Parsed var railDistance3: Double? = 0.0,


    //junk
    @Parsed var cableCarDistance1: Double? = 0.0,
    @Parsed var ferryDistance1: Double? = 0.0,
    @Parsed var gondolaDistance1: Double? = 0.0,
    @Parsed var funicularDistance1: Double? = 0.0,
    @Parsed var airplaneDistance1: Double? = 0.0,
    @Parsed var cableCarTime1: Double? = 0.0,
    @Parsed var ferryTime1: Double? = 0.0,
    @Parsed var gondolaTime1: Double? = 0.0,
    @Parsed var funicularTime1: Double? = 0.0,
    @Parsed var airplaneTime1: Double? = 0.0,

    @Parsed var cableCarTime2: Double? = 0.0,
    @Parsed var ferryTime2: Double? = 0.0,
    @Parsed var gondolaTime2: Double? = 0.0,
    @Parsed var funicularTime2: Double? = 0.0,
    @Parsed var airplaneTime2: Double? = 0.0,
    @Parsed var cableCarDistance2: Double? = 0.0,
    @Parsed var ferryDistance2: Double? = 0.0,
    @Parsed var gondolaDistance2: Double? = 0.0,
    @Parsed var funicularDistance2: Double? = 0.0,
    @Parsed var airplaneDistance2: Double? = 0.0,

    @Parsed var cableCarTime3: Double? = 0.0,
    @Parsed var ferryTime3: Double? = 0.0,
    @Parsed var gondolaTime3: Double? = 0.0,
    @Parsed var funicularTime3: Double? = 0.0,
    @Parsed var airplaneTime3: Double? = 0.0,
    @Parsed var cableCarDistance3: Double? = 0.0,
    @Parsed var ferryDistance3: Double? = 0.0,
    @Parsed var gondolaDistance3: Double? = 0.0,
    @Parsed var funicularDistance3: Double? = 0.0,
    @Parsed var airplaneDistance3: Double? = 0.0
)
