package com.global_search.sherlock.service;

import lombok.extern.slf4j.Slf4j;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class NLPService {

    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;
    private final NameFinderME locationFinder;

    // Valid statuses
    private static final Set<String> TRIP_STATUS =
            Set.of("PLANNED", "IN_PROGRESS", "COMPLETED");

    private static final Set<String> ORDER_STATUS =
            Set.of("CREATED", "IN_TRANSIT", "DELIVERED");

    public NLPService() throws Exception {

        InputStream tokenModel = getClass().getResourceAsStream("/models/en-token.bin");
        InputStream posModel = getClass().getResourceAsStream("/models/en-pos-maxent.bin");
        InputStream locModel = getClass().getResourceAsStream("/models/en-ner-location.bin");

        tokenizer = new TokenizerME(new TokenizerModel(tokenModel));
        posTagger = new POSTaggerME(new POSModel(posModel));
        locationFinder = new NameFinderME(new TokenNameFinderModel(locModel));
    }

    // ========================================================================
    //                         MAIN DSL BUILDER
    // ========================================================================
    public Query convertToDsl(String text) {

        String[] tokens = tokenizer.tokenize(text);

        // Extracted fields
        List<String> locations = extractLocations(tokens);
        String status = extractStatus(tokens);                           // trip OR order
        String customerName = extractCustomerName(tokens);
        String tripCode = extractTripCode(tokens);
        String shipmentOrderId = extractShipmentOrderId(tokens);

        String origin = locations.size() > 0 ? locations.get(0) : null;
        String destination = locations.size() > 1 ? locations.get(1) : null;

        BoolQuery.Builder bool = new BoolQuery.Builder();

        // --------------------------------------------------------------------
        // TRIP CODE
        // --------------------------------------------------------------------
        if (tripCode != null) {
            bool.must(term("tripCode", tripCode));
        }

        // --------------------------------------------------------------------
        // STATUS LOGIC
        // --------------------------------------------------------------------
        if (status != null) {

            if (TRIP_STATUS.contains(status)) {
                // Trip status → direct field
                bool.must(term("status", status));
            }
            else if (ORDER_STATUS.contains(status)) {
                // Order status → nested query
                bool.must(nested("orders",
                        term("orders.status", status)
                ));
            }
        }

        // --------------------------------------------------------------------
        // ORIGIN
        // --------------------------------------------------------------------
        if (origin != null) {
            bool.must(match("origin", origin));
        }

        // --------------------------------------------------------------------
        // DESTINATION
        // --------------------------------------------------------------------
        if (destination != null) {
            bool.must(match("destination", destination));
        }

        // --------------------------------------------------------------------
        // SHIPMENT externalCustomerOrderId (nested)
        // --------------------------------------------------------------------
        if (shipmentOrderId != null) {
            bool.must(nested("shipments",
                    term("shipments.externalCustomerOrderId", shipmentOrderId)
            ));
        }

        // --------------------------------------------------------------------
        // CUSTOMER NAME (nested)
        // --------------------------------------------------------------------
        if (customerName != null) {
            bool.must(nested("orders",
                    match("orders.customerName", customerName)
            ));
        }

        return Query.of(q -> q.bool(bool.build()));
    }

    // ========================================================================
    //                          QUERY HELPERS
    // ========================================================================
    private Query term(String field, String value) {
        return Query.of(q -> q.term(t -> t.field(field).value(v -> v.stringValue(value))));
    }

    private Query match(String field, String value) {
        return Query.of(q -> q.match(m -> m.field(field).query(v -> v.stringValue(value))));
    }

    private Query nested(String path, Query inner) {
        return Query.of(q -> q.nested(n -> n.path(path).query(inner)));
    }

    // ========================================================================
    //                       NLP EXTRACTORS
    // ========================================================================

    private List<String> extractLocations(String[] tokens) {
        Span[] spans = locationFinder.find(tokens);
        List<String> locations = new ArrayList<>();
        for (Span span : spans) {
            StringBuilder sb = new StringBuilder();
            for (int i = span.getStart(); i < span.getEnd(); i++) {
                sb.append(tokens[i]).append(" ");
            }
            locations.add(sb.toString().trim());
        }
        return locations;
    }

    /**
     * Extract status ONLY if it belongs to either TRIP or ORDER status sets.
     */
    private String extractStatus(String[] tokens) {
        for (String t : tokens) {
            String up = t.toUpperCase();
            if (TRIP_STATUS.contains(up) || ORDER_STATUS.contains(up)) {
                return up;
            }
        }
        return null; // INVALID → IGNORED
    }

    private String extractCustomerName(String[] tokens) {
        // dataset: simple extraction (optional: extend to ML/NLP)
        for (int i = 0; i < tokens.length - 2; i++) {
            if (tokens[i].equalsIgnoreCase("customer")
                    && tokens[i + 1].equalsIgnoreCase("name")) {
                return tokens[i + 2]; // naive but works for your structure
            }
        }
        return null;
    }

    private String extractTripCode(String[] tokens) {
        for (String t : tokens) {
            if (t.matches("TRP-\\d+")) {
                return t;
            }
        }
        return null;
    }

    private String extractShipmentOrderId(String[] tokens) {
        for (String t : tokens) {
            if (t.startsWith("ECO")) {
                return t;
            }
        }
        return null;
    }
}

