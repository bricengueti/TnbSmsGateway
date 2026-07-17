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

    public ReferenceService(CountryRepository countryRepository,
                            OperatorRepository operatorRepository) {
        this.countryRepository = countryRepository;
        this.operatorRepository = operatorRepository;
    }

    // ===== COUNTRY =====

    /**
     * Trouver un pays par son code
     */
    public Optional<Country> findCountryByCode(String code) {
        return countryRepository.findById(code);
    }

    /**
     * Trouver tous les pays
     */
    public List<Country> findAllCountries() {
        return countryRepository.findAll();
    }

    /**
     * Trouver tous les pays avec leurs opérateurs
     */
    public List<Country> findAllCountriesWithOperators() {
        return countryRepository.findAllWithOperators();
    }

    /**
     * Vérifier si un pays existe
     */
    public boolean countryExists(String code) {
        return countryRepository.existsById(code);
    }

    // ===== OPERATOR =====

    /**
     * Trouver un opérateur par son code
     */
    public Optional<Operator> findOperatorByCode(String code) {
        return operatorRepository.findByCode(code);
    }

    /**
     * Trouver tous les opérateurs d'un pays
     */
    public List<Operator> findOperatorsByCountry(String countryCode) {
        return operatorRepository.findByCountryCode(countryCode);
    }

    /**
     * Vérifier si un opérateur appartient à un pays
     */
    public boolean isOperatorInCountry(String operatorCode, String countryCode) {
        return operatorRepository.existsByCodeAndCountryCode(operatorCode, countryCode);
    }

    /**
     * Trouver un opérateur avec son pays
     */
    public Optional<Operator> findOperatorWithCountry(String code) {
        return operatorRepository.findByCodeWithCountry(code);
    }
}