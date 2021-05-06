package edu.usf.cutr.grha.otp

import edu.usf.cutr.grha.io.ChicagoTncWriter
import edu.usf.cutr.grha.model.ChicagoTncData
import edu.usf.cutr.grha.utils.GtfsUtils
import edu.usf.cutr.otp.plan.api.PlanApi
import edu.usf.cutr.otp.plan.model.Planner
import edu.usf.cutr.otp.plan.model.RequestParameters
import edu.usf.cutr.otp.plan.model.core.TraverseModes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resumeWithException

class OtpService(
    private var chicagoTncData: List<ChicagoTncData>,
    private var url: String
) {

    fun call(concurrency: Int = 10) {
        getPlanData(concurrency)
    }

    private fun getPlanData(concurrency: Int) {
        val output = mutableListOf<ChicagoTncData>()
        // val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
        runBlocking {
            chicagoTncData
                .asFlow()
                .filter {
                    isValidLocation(
                        it.pickupCentroidLatitude,
                        it.pickupCentroidLongitude,
                        it.dropoffCentroidLatitude,
                        it.dropoffCentroidLongitude
                    )
                }
                .flatMapMerge(concurrency = concurrency) {
                    flow {
                        val origin = GtfsUtils.latLong(
                            it.pickupCentroidLatitude,
                            it.pickupCentroidLongitude
                        )
                        val destination = GtfsUtils.latLong(
                            it.dropoffCentroidLatitude,
                            it.dropoffCentroidLongitude
                        )

                        // time provided here is not associated to a particular timezone. The timezone is set
                        // by the gtfs agency time zone set in the server.
                        val startDateTime = GtfsUtils.getDateFromTimeStamp(it.tripStartTimeStamp, "M/dd/yyyy H:mm")
                        val date = startDateTime.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                        val time = startDateTime.format(DateTimeFormatter.ofPattern("h:mma"))

//                        val zonedDateTime = ZonedDateTime.of(startDateTime, ZoneId.of("America/Chicago"))
//                        zonedDateTime.format(dateTimeFormatter)
//
//                        println(zonedDateTime)

                        val requestParameters = RequestParameters(
                            fromPlace = origin, toPlace = destination,
                            date = date.toString(), time = time.toString()
                        )
                        val planApi = PlanApi(url, requestParameters)
                        emit(makePlanRequest(planApi, chicagoTncData.indexOf(it)))
                    }
                }
                .collect {
                    var chicagoTnc = chicagoTncData[it.additionalProperties["Index"] as Int]

                    // get total travel time and distance from Itinerary
                    val itineraries = it.plan?.itineraries
                    if (itineraries?.size!! > 0) {
                        chicagoTnc.totalTravelTime1 = it.plan?.itineraries?.get(0)?.duration
                        chicagoTnc.totalDistance1 = calculateTotalDistance(it, 0)
                        chicagoTnc.totalWaitTime1 = it.plan?.itineraries?.get(0)?.waitingTime
                        chicagoTnc.altitudeChange1 = getAltitudeChange(it, 0)
                        chicagoTnc.transfers1 = it.plan?.itineraries?.get(0)?.transfers
                        chicagoTnc = fillTimeAndDistanceInformationForModes(chicagoTnc, it, 0)

                        // to remove extra commas
                        chicagoTnc.Modes1 = chicagoTnc.Modes1?.substring(0, chicagoTnc.Modes1?.length?.minus(2)!!)
                    }
                    if (itineraries.size > 1) {
                        chicagoTnc.totalTravelTime2 = it.plan?.itineraries?.get(1)?.duration
                        chicagoTnc.totalDistance2 = calculateTotalDistance(it, 1)
                        chicagoTnc.totalWaitTime2 = it.plan?.itineraries?.get(1)?.waitingTime
                        chicagoTnc.altitudeChange2 = getAltitudeChange(it, 1)
                        chicagoTnc.transfers2 = it.plan?.itineraries?.get(1)?.transfers
                        chicagoTnc = fillTimeAndDistanceInformationForModes(chicagoTnc, it, 1)

                        // to remove extra commas
                        chicagoTnc.Modes2 = chicagoTnc.Modes2?.substring(0, chicagoTnc.Modes2?.length?.minus(2)!!)

                    }
                    if (itineraries.size > 2) {
                        chicagoTnc.totalTravelTime3 = it.plan?.itineraries?.get(2)?.duration
                        chicagoTnc.totalDistance3 = calculateTotalDistance(it, 2)
                        chicagoTnc.totalWaitTime3 = it.plan?.itineraries?.get(2)?.waitingTime
                        chicagoTnc.altitudeChange3 = getAltitudeChange(it, 2)
                        chicagoTnc.transfers3 = it.plan?.itineraries?.get(2)?.transfers
                        chicagoTnc = fillTimeAndDistanceInformationForModes(chicagoTnc, it, 2)

                        // to remove extra commas
                        chicagoTnc.Modes3 = chicagoTnc.Modes3?.substring(0, chicagoTnc.Modes3?.length?.minus(2)!!)
                    }
                    output.add(chicagoTnc)

                }
            val headers = ChicagoTncData::class.java.declaredFields.map { field -> field.name }
            ChicagoTncWriter(output, headers).writeFile()
        }
    }

    private suspend fun makePlanRequest(planApi: PlanApi, i: Int): Planner = suspendCancellableCoroutine { cont ->
        planApi.getPlan({
            it.additionalProperties["Index"] = i
            cont.resume(it, null)
        }, {
            if (it != null)
                cont.resumeWithException(it)
        })
    }

    private fun isValidLocation(originLat: Double, originLong: Double, destLat: Double, destLong: Double): Boolean {
        return originLat != 0.0 && originLong != 0.0 &&
                destLat != 0.0 && destLong != 0.0
    }

    private fun calculateTotalDistance(planner: Planner, index: Int): Double {
        val legs = planner.plan?.itineraries?.get(index)?.legs
        var distance = 0.0
        legs?.forEach {
            distance = distance.plus(it.distance!!)
        }
        return distance
    }

    private fun getAltitudeChange(planner: Planner, index: Int): Double {
        val elevationLost = planner.plan?.itineraries?.get(index)?.elevationLost
        val elevationGained = planner.plan?.itineraries?.get(index)?.elevationGained

        return if (elevationLost!! > 0) {
            elevationLost * -1
        } else {
            elevationGained!!
        }
    }

    private fun fillTimeAndDistanceInformationForModes(
        chicagoTncData: ChicagoTncData,
        planner: Planner,
        index: Int
    ): ChicagoTncData {

        val legs = planner.plan?.itineraries?.get(index)?.legs
        legs?.forEach {
            // append travel modes
            when (index + 1) {
                1 -> chicagoTncData.Modes1 = chicagoTncData.Modes1.plus(it.mode.toString() + ", ")
                2 -> chicagoTncData.Modes2 = chicagoTncData.Modes2.plus(it.mode.toString() + ", ")
                3 -> chicagoTncData.Modes3 = chicagoTncData.Modes3.plus(it.mode.toString() + ", ")
            }

            // calculate time and distance per mode
            when (it.mode) {
                TraverseModes.WALK.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.walkTime1 = chicagoTncData.walkTime1?.plus(it.duration!!)
                            chicagoTncData.walkDistance1 = chicagoTncData.walkDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.walkTime2 = chicagoTncData.walkTime2?.plus(it.duration!!)
                            chicagoTncData.walkDistance2 = chicagoTncData.walkDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.walkTime3 = chicagoTncData.walkTime3?.plus(it.duration!!)
                            chicagoTncData.walkDistance3 = chicagoTncData.walkDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.TRANSIT.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.transitTime1 = chicagoTncData.transitTime1?.plus(it.duration!!)
                            chicagoTncData.transitDistance1 = chicagoTncData.transitDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.transitTime2 = chicagoTncData.transitTime2?.plus(it.duration!!)
                            chicagoTncData.transitDistance2 = chicagoTncData.transitDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.transitTime3 = chicagoTncData.transitTime3?.plus(it.duration!!)
                            chicagoTncData.transitDistance3 = chicagoTncData.transitDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.BUS.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.busTime1 = chicagoTncData.busTime1?.plus(it.duration!!)
                            chicagoTncData.busDistance1 = chicagoTncData.busDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.busTime2 = chicagoTncData.busTime2?.plus(it.duration!!)
                            chicagoTncData.busDistance2 = chicagoTncData.busDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.busTime3 = chicagoTncData.busTime3?.plus(it.duration!!)
                            chicagoTncData.busDistance3 = chicagoTncData.busDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.BICYCLE.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.bicycleTime1 = chicagoTncData.bicycleTime1?.plus(it.duration!!)
                            chicagoTncData.bicycleDistance1 = chicagoTncData.bicycleDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.bicycleTime2 = chicagoTncData.bicycleTime2?.plus(it.duration!!)
                            chicagoTncData.bicycleDistance2 = chicagoTncData.bicycleDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.bicycleTime3 = chicagoTncData.bicycleTime3?.plus(it.duration!!)
                            chicagoTncData.bicycleDistance3 = chicagoTncData.bicycleDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.BICYCLE_RENT.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.bicycleRentTime1 = chicagoTncData.bicycleRentTime1?.plus(it.duration!!)
                            chicagoTncData.bicycleRentDistance1 =
                                chicagoTncData.bicycleRentDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.bicycleRentTime2 = chicagoTncData.bicycleRentTime2?.plus(it.duration!!)
                            chicagoTncData.bicycleRentDistance2 =
                                chicagoTncData.bicycleRentDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.bicycleRentTime3 = chicagoTncData.bicycleRentTime3?.plus(it.duration!!)
                            chicagoTncData.bicycleRentDistance3 =
                                chicagoTncData.bicycleRentDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.BICYCLE_PARK.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.bicycleParkTime1 = chicagoTncData.bicycleParkTime1?.plus(it.duration!!)
                            chicagoTncData.bicycleParkDistance1 =
                                chicagoTncData.bicycleParkDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.bicycleParkTime2 = chicagoTncData.bicycleParkTime2?.plus(it.duration!!)
                            chicagoTncData.bicycleParkDistance2 =
                                chicagoTncData.bicycleParkDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.bicycleParkTime3 = chicagoTncData.bicycleParkTime3?.plus(it.duration!!)
                            chicagoTncData.bicycleParkDistance3 =
                                chicagoTncData.bicycleParkDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.CAR.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.carTime1 = chicagoTncData.carTime1?.plus(it.duration!!)
                            chicagoTncData.carDistance1 =
                                chicagoTncData.carDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.carTime2 = chicagoTncData.carTime2?.plus(it.duration!!)
                            chicagoTncData.carDistance2 =
                                chicagoTncData.carDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.carTime3 = chicagoTncData.carTime3?.plus(it.duration!!)
                            chicagoTncData.carDistance3 = chicagoTncData.carDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.CAR_PARK.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.carParkTime1 = chicagoTncData.carParkTime1?.plus(it.duration!!)
                            chicagoTncData.carParkDistance1 =
                                chicagoTncData.carParkDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.carParkTime2 = chicagoTncData.carParkTime2?.plus(it.duration!!)
                            chicagoTncData.carParkDistance2 =
                                chicagoTncData.carParkDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.carParkTime3 = chicagoTncData.carParkTime3?.plus(it.duration!!)
                            chicagoTncData.carParkDistance3 = chicagoTncData.carParkDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.TRAM.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.tramTime1 = chicagoTncData.tramTime1?.plus(it.duration!!)
                            chicagoTncData.tramDistance1 =
                                chicagoTncData.tramDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.tramTime2 = chicagoTncData.tramTime2?.plus(it.duration!!)
                            chicagoTncData.tramDistance2 =
                                chicagoTncData.tramDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.tramTime3 = chicagoTncData.tramTime3?.plus(it.duration!!)
                            chicagoTncData.tramDistance3 = chicagoTncData.tramDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.SUBWAY.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.subwayTime1 = chicagoTncData.subwayTime1?.plus(it.duration!!)
                            chicagoTncData.subwayDistance1 =
                                chicagoTncData.subwayDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.subwayTime2 = chicagoTncData.subwayTime2?.plus(it.duration!!)
                            chicagoTncData.subwayDistance2 =
                                chicagoTncData.subwayDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.subwayTime3 = chicagoTncData.subwayTime3?.plus(it.duration!!)
                            chicagoTncData.subwayDistance3 = chicagoTncData.subwayDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.RAIL.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.railTime1 = chicagoTncData.railTime1?.plus(it.duration!!)
                            chicagoTncData.railDistance1 =
                                chicagoTncData.railDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.railTime2 = chicagoTncData.railTime2?.plus(it.duration!!)
                            chicagoTncData.railDistance2 =
                                chicagoTncData.railDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.railTime3 = chicagoTncData.railTime3?.plus(it.duration!!)
                            chicagoTncData.railDistance3 = chicagoTncData.railDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.CABLE_CAR.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.cableCarTime1 = chicagoTncData.cableCarTime1?.plus(it.duration!!)
                            chicagoTncData.cableCarDistance1 =
                                chicagoTncData.cableCarDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.cableCarTime2 = chicagoTncData.cableCarTime2?.plus(it.duration!!)
                            chicagoTncData.cableCarDistance2 =
                                chicagoTncData.cableCarDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.cableCarTime3 = chicagoTncData.cableCarTime3?.plus(it.duration!!)
                            chicagoTncData.cableCarDistance3 = chicagoTncData.cableCarDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.FERRY.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.ferryTime1 = chicagoTncData.ferryTime1?.plus(it.duration!!)
                            chicagoTncData.ferryDistance1 =
                                chicagoTncData.ferryDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.ferryTime2 = chicagoTncData.ferryTime2?.plus(it.duration!!)
                            chicagoTncData.ferryDistance2 =
                                chicagoTncData.ferryDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.ferryTime3 = chicagoTncData.ferryTime3?.plus(it.duration!!)
                            chicagoTncData.ferryDistance3 = chicagoTncData.ferryDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.GONDOLA.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.gondolaTime1 = chicagoTncData.gondolaTime1?.plus(it.duration!!)
                            chicagoTncData.gondolaDistance1 =
                                chicagoTncData.gondolaDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.gondolaTime2 = chicagoTncData.gondolaTime2?.plus(it.duration!!)
                            chicagoTncData.gondolaDistance2 =
                                chicagoTncData.gondolaDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.gondolaTime3 = chicagoTncData.gondolaTime3?.plus(it.duration!!)
                            chicagoTncData.gondolaDistance3 = chicagoTncData.gondolaDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.FUNICULAR.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.funicularTime1 = chicagoTncData.funicularTime1?.plus(it.duration!!)
                            chicagoTncData.funicularDistance1 =
                                chicagoTncData.funicularDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.funicularTime2 = chicagoTncData.funicularTime2?.plus(it.duration!!)
                            chicagoTncData.funicularDistance2 =
                                chicagoTncData.funicularDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.funicularTime3 = chicagoTncData.funicularTime3?.plus(it.duration!!)
                            chicagoTncData.funicularDistance3 = chicagoTncData.funicularDistance3?.plus(it.distance!!)
                        }
                    }
                }
                TraverseModes.AIRPLANE.toString() -> {
                    when (index + 1) {
                        1 -> {
                            chicagoTncData.airplaneTime1 = chicagoTncData.airplaneTime1?.plus(it.duration!!)
                            chicagoTncData.airplaneDistance1 =
                                chicagoTncData.airplaneDistance1?.plus(it.distance!!)
                        }
                        2 -> {
                            chicagoTncData.airplaneTime2 = chicagoTncData.airplaneTime2?.plus(it.duration!!)
                            chicagoTncData.airplaneDistance2 =
                                chicagoTncData.airplaneDistance2?.plus(it.distance!!)
                        }
                        3 -> {
                            chicagoTncData.airplaneTime3 = chicagoTncData.airplaneTime3?.plus(it.duration!!)
                            chicagoTncData.airplaneDistance3 = chicagoTncData.airplaneDistance3?.plus(it.distance!!)
                        }
                    }
                }


            }
        }
        return chicagoTncData
    }
}