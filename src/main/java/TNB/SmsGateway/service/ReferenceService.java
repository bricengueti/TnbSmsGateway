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

    // 🔥 Utiliser findByCode au lieu de findById
    public Optional<Country> findCountryByCode(String code) {
        return countryRepository.findByCode(code);  // ✅ Méthode personnalisée
    }

    public List<Country> findAllCountries() {
        return countryRepository.findAll();
    }

    public List<Country> findAllCountriesWithOperators() {
        return countryRepository.findAllWithOperators();
    }

    public boolean countryExists(String code) {
        return countryRepository.existsByCode(code);  // ✅ Méthode personnalisée
    }

    public Optional<Operator> findOperatorByCode(String code) {
        return operatorRepository.findByCode(code);
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
}