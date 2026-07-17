package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.response.CoverageResponse;
import TNB.SmsGateway.dto.response.CountryCoverage;
import TNB.SmsGateway.dto.response.OperatorCoverage;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.repository.DeviceSimRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CoverageService {

    private final DeviceRepository deviceRepository;
    private final DeviceSimRepository deviceSimRepository;
    private final ReferenceService referenceService;

    public CoverageService(DeviceRepository deviceRepository,
                           DeviceSimRepository deviceSimRepository,
                           ReferenceService referenceService) {
        this.deviceRepository = deviceRepository;
        this.deviceSimRepository = deviceSimRepository;
        this.referenceService = referenceService;
    }

    /**
     * Obtenir la couverture pour un utilisateur
     * Scénario: L'utilisateur consulte ses dispositifs disponibles
     */
    public CoverageResponse getCoverage(UUID userId) {
        // Récupérer les données de couverture
        List<Object[]> coverageData = deviceRepository.getCoverageByUser(userId);

        // Grouper par pays
        Map<String, List<OperatorCoverage>> countryMap = new HashMap<>();

        for (Object[] data : coverageData) {
            String countryCode = (String) data[0];
            String operatorCode = (String) data[1];
            Long count = (Long) data[2];

            // Calculer la capacité quotidienne (100 SMS par SIM)
            int dailyCapacity = count.intValue() * 100;

            OperatorCoverage operatorCoverage = new OperatorCoverage(
                    operatorCode,
                    referenceService.findOperatorByCode(operatorCode)
                            .map(op -> op.getDisplayName())
                            .orElse(operatorCode),
                    count.intValue(),
                    count.intValue(), // activeSims = devices
                    dailyCapacity
            );

            countryMap.computeIfAbsent(countryCode, k -> new ArrayList<>())
                    .add(operatorCoverage);
        }

        // Construire la réponse
        List<CountryCoverage> countries = new ArrayList<>();
        for (Map.Entry<String, List<OperatorCoverage>> entry : countryMap.entrySet()) {
            String countryCode = entry.getKey();
            String countryName = referenceService.findCountryByCode(countryCode)
                    .map(c -> c.getName())
                    .orElse(countryCode);

            countries.add(new CountryCoverage(
                    countryCode,
                    countryName,
                    entry.getValue()
            ));
        }

        return new CoverageResponse(countries);
    }
}