package com.dtz.safestroute.net.gson

class RouteResponse(val response: RouteResponse.Response) {

    inner class Response(val metaInfo: MetaInfo, val route: List<RouteItem>, val language: String)

    inner class MetaInfo(val moduleVersion: String, val mapVersion: String, val interfaceVersion: String, val timestamp: String)

    inner class RouteItem(val waypoint: List<WaypointItem>, val mode: Mode, val summary: Summary, val shape: List<String>, val leg: List<LegItem>)

    inner class WaypointItem(val mappedPosition: MappedPosition, val linkId: String, val spot: Double, val shapeIndex: Int, val label: String, val type: String, val sideOfStreet: String, val mappedRoadName: String, val originalPosition: OriginalPosition)

    inner class Mode(val transportModes: List<String>, val feature: List<Any>, val trafficMode: String, val type: String)

    inner class Summary(val travelTime: Int, val distance: Int, val trafficTime: Int, val flags: List<String>, val type: String, val text: String, val baseTime: Int)

    inner class LegItem(val travelTime: Int, val start: Start, val length: Int, val end: End, val maneuver: List<ManeuverItem>)

    inner class MappedPosition(val latitude: Double, val longitude: Double)

    inner class OriginalPosition(val latitude: Double, val longitude: Double)

    inner class Start(val mappedPosition: MappedPosition, val linkId: String, val spot: Double, val shapeIndex: Int, val label: String, val type: String, val sideOfStreet: String, val mappedRoadName: String, val originalPosition: OriginalPosition)

    inner class End(val mappedPosition: MappedPosition, val linkId: String, val spot: Double, val shapeIndex: Int, val label: String, val type: String, val sideOfStreet: String, val mappedRoadName: String, val originalPosition: OriginalPosition)

    inner class ManeuverItem(val travelTime: Int, val instruction: String, val length: Int, val type: String, val position: Position, val id: String)

    inner class Position(val latitude: Double, val longitude: Double)
}
