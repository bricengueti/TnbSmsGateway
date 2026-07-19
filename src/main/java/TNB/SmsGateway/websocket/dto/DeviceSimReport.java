package TNB.SmsGateway.websocket.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

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

            @JsonProperty("dailyQuota")
            String dailyQuota,   // "100" ou "ILLIMITE", null accepté (fallback au quota par défaut)

            @JsonProperty("isActive")
            Boolean isActive
    ) {}
}