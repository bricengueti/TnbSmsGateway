package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.Country;
import TNB.SmsGateway.entity.Operator;
import TNB.SmsGateway.repository.CountryRepository;
import TNB.SmsGateway.repository.OperatorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReferenceService {

    private final CountryRepository countryRepository;
    private final OperatorRepository operatorRepository;

    public ReferenceService(CountryRepository countryRepository, OperatorRepository operatorRepository) {
        this.countryRepository = countryRepository;
        this.operatorRepository = operatorRepository;
    }

    public Optional<Country> findCountryByCode(String code) {
        return countryRepository.findByCode(code);
    }

    public List<Country> findAllCountries() {
        return countryRepository.findAll();
    }

    public List<Country> findAllCountriesWithOperators() {
        return countryRepository.findAllWithOperators();
    }

    public boolean countryExists(String code) {
        return countryRepository.existsByCode(code);
    }

    /**
     * @deprecated Ambigu depuis les codes génériques par marque (ORANGE, MTN...).
     * Utiliser findOperatorByCodeAndCountry.
     */
    @Deprecated
    public Optional<Operator> findOperatorByCode(String code) {
        return operatorRepository.findByCode(code);
    }

    public Optional<Operator> findOperatorByCodeAndCountry(String code, String countryCode) {
        return operatorRepository.findByCodeAndCountryCode(code, countryCode);
    }

    public List<Operator> findOperatorsByCountry(String countryCode) {
        return operatorRepository.findByCountryCode(countryCode);
    }

    public boolean isOperatorInCountry(String operatorCode, String countryCode) {
        return operatorRepository.existsByCodeAndCountryCode(operatorCode, countryCode);
    }

    public Optional<Operator> findOperatorWithCountry(String code) {
        return operatorRepository.findByCodeWithCountry(code);
    }

    /**
     * 🔥 Résolution à partir d'un nom d'opérateur brut (ex: "Orange CM", venant du
     * carrierName Android), scopée au pays du device. On cherche le premier opérateur
     * du pays dont le code générique (ORANGE, MTN...) apparaît comme sous-chaîne
     * (insensible à la casse) dans le nom brut reçu.
     */
    public Optional<Operator> resolveOperatorFromRawName(String rawName, String countryCode) {
        if (rawName == null || rawName.isBlank() || countryCode == null) {
            return Optional.empty();
        }
        String normalized = rawName.trim().toUpperCase();
        List<Operator> countryOperators = operatorRepository.findByCountryCode(countryCode);

        return countryOperators.stream()
                .filter(op -> normalized.contains(op.getCode().toUpperCase()))
                .findFirst();
    }
}