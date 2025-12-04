package com.global_search.sherlock.controller;

import com.global_search.sherlock.document.TripSearchDocument;
import com.global_search.sherlock.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;

    @GetMapping
    public List<TripSearchDocument> search(@RequestParam String q) throws IOException {
        return tripService.searchTrips(q);
    }

    @PostMapping
    public String addTrip(@RequestBody TripSearchDocument trip) throws IOException {
        tripService.indexTrip(trip);
        return "Trip indexed: " + trip.getTripCode();
    }

    @PostMapping("/bulk")
    public void indexTripsBulk(@RequestBody List<TripSearchDocument> trips) throws IOException {
        tripService.indexTrips(trips);
    }
}


