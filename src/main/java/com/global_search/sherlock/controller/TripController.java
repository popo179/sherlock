package com.global_search.sherlock.controller;

import com.global_search.sherlock.document.TripSearchDocument;
import com.global_search.sherlock.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // Multi-criteria search endpoint
    @GetMapping
    public List<TripSearchDocument> searchTrips(
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String shipmentOrderId,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination
    ) throws IOException {
        return tripService.searchTrips(orderStatus, shipmentOrderId, origin, destination);
    }

    // Index a new trip
    @PostMapping
    public String addTrip(@RequestBody TripSearchDocument trip) throws IOException {
        tripService.indexTrip(trip);
        return "Trip indexed: " + trip.getTripId();
    }
}

