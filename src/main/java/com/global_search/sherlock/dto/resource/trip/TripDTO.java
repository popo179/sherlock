package com.global_search.sherlock.dto.resource.trip;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripDTO {

    private String transportOrderCode;
    private String externalTransportOrderId;
    private String assignmentType;
    private String assignmentCode;
    private String assigneeIdentifier;

    private List<TripDetailDTO> trips;
    private Object details;

    // ============================
    //       NESTED CLASSES
    // ============================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TripDetailDTO {
        private String code;
        private String externalTripId;
        private String externalOriginLocationCode;
        private String externalDestinationLocationCode;
        private DateTimeDTO expectedStartAt;
        private DateTimeDTO expectedEndAt;

        private List<ShipmentDTO> shipments;
        private List<TaskDTO> tasks;
        private List<StopDTO> stops;

        private ResourceDTO vehicleResource;
        private List<ResourceDTO> trailerResources;
        private List<ResourceDTO> vehicleOperatorResources;

        private Object details;
    }

    // ---------------------------
    //        DateTimeDTO
    // ---------------------------
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DateTimeDTO {
        private Long epoch;
        private String timezone;
    }

    // ---------------------------
    //        ShipmentDTO
    // ---------------------------
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShipmentDTO {
        private String code;
        private String externalShipmentId;
        private String externalConsignmentId;
        private String externalCustomerOrderId;

        private StopShortDTO originStop;
        private StopShortDTO destinationStop;

        private DateTimeDTO expectedPickupAt;
        private DateTimeDTO expectedDeliveryAt;

        private Double weight;
        private String weightUom;
        private Double volume;
        private String volumeUom;

        private Object loadingDetails;
        private Object unloadingDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StopShortDTO {
        private String externalLocationCode;
        private Integer sequence;
        private Object details;
    }

    // ---------------------------
    //          TaskDTO
    // ---------------------------
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskDTO {
        private String code;
        private String taskRegistrationCode;
        private String taskTransactionCode;
        private Integer sequence;

        private DateTimeDTO expectedStartAt;
        private DateTimeDTO expectedEndAt;

        private String externalTaskMasterCode;
        private Boolean mandatory;

        private List<TaskParamDTO> taskParams;

        private Object details;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskParamDTO {
        private String paramName;
        private Object paramValue;   // can be Shipment reference or Stop reference
        private String taskCode;
    }

    // ---------------------------
    //          StopDTO
    // ---------------------------
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StopDTO {
        private String externalLocationCode;
        private Integer sequence;
        private Object segmentDetails;
        private Object details;
    }

    // ---------------------------
    //        ResourceDTO
    // ---------------------------
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResourceDTO {
        private String externalResourceId;
        private String registrationNumber;

        private String externalVehicleTypeId;
        private Double capacity;
        private String vehicleType;

        private String operatorName;
        private String licenseNumber;
        private String licenseType;
        private String crpId;

        private Object details;
    }
}

