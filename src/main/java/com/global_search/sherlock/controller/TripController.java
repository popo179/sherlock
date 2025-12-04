package com.global_search.sherlock.controller;

import com.global_search.sherlock.document.OrderDocument;
import com.global_search.sherlock.document.TripSearchDocument;
import com.global_search.sherlock.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // Multi-criteria search endpoint
    @GetMapping
    public List<TripSearchDocument> searchTrips(
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String shipmentCode,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String tripCode,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String consignmentCode,
            @RequestParam(required = false) String tripStatus
    ) throws IOException {
        return tripService.searchTrips(orderStatus, shipmentCode, origin, destination,
                tripCode, orderCode, consignmentCode, tripStatus);
    }

    // Index a new trip
    @PostMapping
    public String addTrip(@RequestBody TripSearchDocument trip) throws IOException {
        tripService.indexTrip(trip);
        return "Trip indexed: " + trip.getTripCode();
    }

    @PostMapping("/order/{orderCode}")
    public void updateTrip(@RequestBody OrderDocument order,
                           @PathVariable("orderCode") String orderCode) throws IOException {
        tripService.updateTripOrder(orderCode, order);
    }
}

