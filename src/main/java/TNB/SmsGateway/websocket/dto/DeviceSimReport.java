package TNB.SmsGateway.websocket.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DeviceSimReport(
        @JsonProperty("sims")
        List<SimInfo> sims
) {
    public record SimInfo(
            @JsonProperty("slotIndex")
            Integer slotIndex,

            @JsonProperty("operatorCode")
            String operatorCode,

            @JsonProperty("phoneNumber")
            String phoneNumber,

            @JsonProperty("isActive")
            Boolean isActive
    ) {}
}