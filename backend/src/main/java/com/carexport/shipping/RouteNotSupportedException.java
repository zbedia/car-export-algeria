package com.carexport.shipping;

public class RouteNotSupportedException extends RuntimeException {
    public RouteNotSupportedException(OriginPort origin, DestinationPort destination) {
        super("No RoRo rate available for route " + origin + " -> " + destination);
    }
}
