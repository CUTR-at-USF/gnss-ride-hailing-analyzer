package edu.usf.cutr.grha.otp

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

    fun call() {
        getPlanData()
    }

    private fun getPlanData() {
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
                .flatMapMerge(concurrency = 10) {
                    flow {
                        val origin = GtfsUtils.latLong(
                            it.pickupCentroidLatitude,
                            it.pickupCentroidLongitude
                        )
                        val destination = GtfsUtils.latLong(
                            it.dropoffCentroidLatitude,
                            it.dropoffCentroidLongitude
                        )
                        val startDateTime = GtfsUtils.getDateFromTimeStamp(it.tripStartTimeStamp, "M/dd/yyyy H:mm")
                        val date = startDateTime.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                        val time = startDateTime.format(DateTimeFormatter.ofPattern("h:mma"))

                        val requestParameters = RequestParameters(
                            fromPlace = origin, toPlace = destination,
                            date = date.toString(), time = time.toString()
                        )
                        val planApi = PlanApi(url, requestParameters)
                        emit(makePlanRequest(planApi, chicagoTncData.indexOf(it)))
                    }
                }
                .collect {
                    val chicagoTnc = chicagoTncData[it.additionalProperties["Index"] as Int]
                    println(it)

                    // get total travel time from Itinerary

                    // get total travel time by walk or bus from Itinerary
                    // TODO get total distance by looping through List<Legs>

                    // TODO get Altitude change from Itinerary

                    // get number of transfers from Itinerary


                    // TODO get Traverse Modes from List<legs>
                }
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

    private fun fillTimeAndDistanceInformationForModes(
        chicagoTncData: ChicagoTncData,
        planner: Planner,
        index: Int
    ): ChicagoTncData {

        val legs = planner.plan?.itineraries?.get(index)?.legs
        legs?.forEach {
            when (it.mode) {
                TraverseModes.WALK.toString() -> {
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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
                    when (index) {
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