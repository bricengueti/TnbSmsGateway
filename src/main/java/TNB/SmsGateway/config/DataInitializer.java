package TNB.SmsGateway.config;

import TNB.SmsGateway.entity.Country;
import TNB.SmsGateway.entity.Operator;
import TNB.SmsGateway.repository.CountryRepository;
import TNB.SmsGateway.repository.OperatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final CountryRepository countryRepository;
    private final OperatorRepository operatorRepository;

    public DataInitializer(CountryRepository countryRepository,
                           OperatorRepository operatorRepository) {
        this.countryRepository = countryRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("🔧 Initialisation des données de référence...");

        if (countryRepository.count() > 0) {
            log.info("✅ Données de référence déjà présentes ({} pays)", countryRepository.count());
            return;
        }

        // ============================================
        // PAYS D'AFRIQUE
        // ============================================

        // Afrique de l'Ouest
        Country benin = new Country("BJ", "Bénin", "+229");
        Country burkina = new Country("BF", "Burkina Faso", "+226");
        Country capeVerde = new Country("CV", "Cap-Vert", "+238");
        Country coteIvoire = new Country("CI", "Côte d'Ivoire", "+225");
        Country gambia = new Country("GM", "Gambie", "+220");
        Country ghana = new Country("GH", "Ghana", "+233");
        Country guinea = new Country("GN", "Guinée", "+224");
        Country guineaBissau = new Country("GW", "Guinée-Bissau", "+245");
        Country liberia = new Country("LR", "Liberia", "+231");
        Country mali = new Country("ML", "Mali", "+223");
        Country mauritania = new Country("MR", "Mauritanie", "+222");
        Country niger = new Country("NE", "Niger", "+227");
        Country nigeria = new Country("NG", "Nigeria", "+234");
        Country senegal = new Country("SN", "Sénégal", "+221");
        Country sierraLeone = new Country("SL", "Sierra Leone", "+232");
        Country togo = new Country("TG", "Togo", "+228");

        // Afrique Centrale
        Country cameroon = new Country("CM", "Cameroun", "+237");
        Country car = new Country("CF", "République Centrafricaine", "+236");
        Country chad = new Country("TD", "Tchad", "+235");
        Country congo = new Country("CG", "Congo-Brazzaville", "+242");
        Country drc = new Country("CD", "République Démocratique du Congo", "+243");
        Country equatorialGuinea = new Country("GQ", "Guinée Équatoriale", "+240");
        Country gabon = new Country("GA", "Gabon", "+241");
        Country saoTome = new Country("ST", "Sao Tomé-et-Principe", "+239");

        // Afrique de l'Est
        Country burundi = new Country("BI", "Burundi", "+257");
        Country djibouti = new Country("DJ", "Djibouti", "+253");
        Country eritrea = new Country("ER", "Érythrée", "+291");
        Country ethiopia = new Country("ET", "Éthiopie", "+251");
        Country kenya = new Country("KE", "Kenya", "+254");
        Country madagascar = new Country("MG", "Madagascar", "+261");
        Country malawi = new Country("MW", "Malawi", "+265");
        Country mauritius = new Country("MU", "Maurice", "+230");
        Country mozambique = new Country("MZ", "Mozambique", "+258");
        Country rwanda = new Country("RW", "Rwanda", "+250");
        Country seychelles = new Country("SC", "Seychelles", "+248");
        Country somalia = new Country("SO", "Somalie", "+252");
        Country southSudan = new Country("SS", "Soudan du Sud", "+211");
        Country tanzania = new Country("TZ", "Tanzanie", "+255");
        Country uganda = new Country("UG", "Ouganda", "+256");
        Country zambia = new Country("ZM", "Zambie", "+260");
        Country zimbabwe = new Country("ZW", "Zimbabwe", "+263");

        // Afrique Australe
        Country angola = new Country("AO", "Angola", "+244");
        Country botswana = new Country("BW", "Botswana", "+267");
        Country comoros = new Country("KM", "Comores", "+269");
        Country eswatini = new Country("SZ", "Eswatini", "+268");
        Country lesotho = new Country("LS", "Lesotho", "+266");
        Country namibia = new Country("NA", "Namibie", "+264");
        Country southAfrica = new Country("ZA", "Afrique du Sud", "+27");

        // Afrique du Nord
        Country algeria = new Country("DZ", "Algérie", "+213");
        Country egypt = new Country("EG", "Égypte", "+20");
        Country libya = new Country("LY", "Libye", "+218");
        Country morocco = new Country("MA", "Maroc", "+212");
        Country sudan = new Country("SD", "Soudan", "+249");
        Country tunisia = new Country("TN", "Tunisie", "+216");
        Country westernSahara = new Country("EH", "Sahara Occidental", "+212");

        // ============================================
        // PAYS D'EUROPE
        // ============================================

        // Europe de l'Ouest
        Country france = new Country("FR", "France", "+33");
        Country germany = new Country("DE", "Allemagne", "+49");
        Country uk = new Country("GB", "Royaume-Uni", "+44");
        Country italy = new Country("IT", "Italie", "+39");
        Country spain = new Country("ES", "Espagne", "+34");
        Country portugal = new Country("PT", "Portugal", "+351");
        Country belgium = new Country("BE", "Belgique", "+32");
        Country netherlands = new Country("NL", "Pays-Bas", "+31");
        Country switzerland = new Country("CH", "Suisse", "+41");
        Country austria = new Country("AT", "Autriche", "+43");
        Country ireland = new Country("IE", "Irlande", "+353");
        Country luxembourg = new Country("LU", "Luxembourg", "+352");
        Country monaco = new Country("MC", "Monaco", "+377");
        Country andorra = new Country("AD", "Andorre", "+376");
        Country liechtenstein = new Country("LI", "Liechtenstein", "+423");
        Country malta = new Country("MT", "Malte", "+356");
        Country sanMarino = new Country("SM", "Saint-Marin", "+378");
        Country vatican = new Country("VA", "Vatican", "+379");

        // Europe du Nord
        Country denmark = new Country("DK", "Danemark", "+45");
        Country finland = new Country("FI", "Finlande", "+358");
        Country iceland = new Country("IS", "Islande", "+354");
        Country norway = new Country("NO", "Norvège", "+47");
        Country sweden = new Country("SE", "Suède", "+46");

        // Europe de l'Est
        Country poland = new Country("PL", "Pologne", "+48");
        Country czechia = new Country("CZ", "République Tchèque", "+420");
        Country slovakia = new Country("SK", "Slovaquie", "+421");
        Country hungary = new Country("HU", "Hongrie", "+36");
        Country romania = new Country("RO", "Roumanie", "+40");
        Country bulgaria = new Country("BG", "Bulgarie", "+359");
        Country greece = new Country("GR", "Grèce", "+30");
        Country cyprus = new Country("CY", "Chypre", "+357");
        Country croatia = new Country("HR", "Croatie", "+385");
        Country slovenia = new Country("SI", "Slovénie", "+386");
        Country lithuania = new Country("LT", "Lituanie", "+370");
        Country latvia = new Country("LV", "Lettonie", "+371");
        Country estonia = new Country("EE", "Estonie", "+372");
        Country bosnia = new Country("BA", "Bosnie-Herzégovine", "+387");
        Country serbia = new Country("RS", "Serbie", "+381");
        Country montenegro = new Country("ME", "Monténégro", "+382");
        Country northMacedonia = new Country("MK", "Macédoine du Nord", "+389");
        Country albania = new Country("AL", "Albanie", "+355");
        Country moldova = new Country("MD", "Moldavie", "+373");

        // Europe du Sud
        Country slovenia2 = new Country("SI", "Slovénie", "+386");
        Country kosovo = new Country("XK", "Kosovo", "+383");
        Country turkiye = new Country("TR", "Turquie", "+90");

        // ============================================
        // REGROUPER TOUS LES PAYS
        // ============================================

        List<Country> countries = countryRepository.saveAll(Arrays.asList(
                // Afrique de l'Ouest
                benin, burkina, capeVerde, coteIvoire, gambia, ghana, guinea, guineaBissau,
                liberia, mali, mauritania, niger, nigeria, senegal, sierraLeone, togo,
                // Afrique Centrale
                cameroon, car, chad, congo, drc, equatorialGuinea, gabon, saoTome,
                // Afrique de l'Est
                burundi, djibouti, eritrea, ethiopia, kenya, madagascar, malawi,
                mauritius, mozambique, rwanda, seychelles, somalia, southSudan,
                tanzania, uganda, zambia, zimbabwe,
                // Afrique Australe
                angola, botswana, comoros, eswatini, lesotho, namibia, southAfrica,
                // Afrique du Nord
                algeria, egypt, libya, morocco, sudan, tunisia, westernSahara,
                // Europe de l'Ouest
                france, germany, uk, italy, spain, portugal, belgium, netherlands,
                switzerland, austria, ireland, luxembourg, monaco, andorra,
                liechtenstein, malta, sanMarino, vatican,
                // Europe du Nord
                denmark, finland, iceland, norway, sweden,
                // Europe de l'Est
                poland, czechia, slovakia, hungary, romania, bulgaria, greece,
                cyprus, croatia, slovenia, lithuania, latvia, estonia, bosnia,
                serbia, montenegro, northMacedonia, albania, moldova,
                // Europe du Sud
                kosovo, turkiye
        ));

        log.info("✅ {} pays créés", countries.size());

        // ============================================
        // OPÉRATEURS PAR PAYS
        // ============================================

        List<Operator> operators = new ArrayList<>();

        // --- AFRIQUE DE L'OUEST ---

        // Bénin
        operators.addAll(Arrays.asList(
                new Operator("MTN_BJ", "MTN", benin),
                new Operator("MOOV_BJ", "Moov", benin),
                new Operator("GLO_BJ", "Glo", benin)
        ));

        // Burkina Faso
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_BF", "Orange", burkina),
                new Operator("TELMOB_BF", "Telmob", burkina)
        ));

        // Cap-Vert
        operators.addAll(Arrays.asList(
                new Operator("CVTELECOM_CV", "CV Telecom", capeVerde),
                new Operator("TMAIS_CV", "T-Mais", capeVerde)
        ));

        // Côte d'Ivoire
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_CI", "Orange", coteIvoire),
                new Operator("MTN_CI", "MTN", coteIvoire),
                new Operator("MOOV_CI", "Moov", coteIvoire)
        ));

        // Gambie
        operators.addAll(Arrays.asList(
                new Operator("GAMTEL_GM", "Gamtel", gambia),
                new Operator("AFRICELL_GM", "Africell", gambia)
        ));

        // Ghana
        operators.addAll(Arrays.asList(
                new Operator("MTN_GH", "MTN", ghana),
                new Operator("VODAFONE_GH", "Vodafone", ghana),
                new Operator("AIRTELTIGO_GH", "AirtelTigo", ghana)
        ));

        // Guinée
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_GN", "Orange", guinea),
                new Operator("MTN_GN", "MTN", guinea)
        ));

        // Guinée-Bissau
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_GW", "Orange", guineaBissau),
                new Operator("MTN_GW", "MTN", guineaBissau)
        ));

        // Liberia
        operators.addAll(Arrays.asList(
                new Operator("LONESTAR_LR", "Lonestar", liberia),
                new Operator("ORANGE_LR", "Orange", liberia)
        ));

        // Mali
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_ML", "Orange", mali),
                new Operator("MALITEL_ML", "Malitel", mali)
        ));

        // Mauritanie
        operators.addAll(Arrays.asList(
                new Operator("MAURITEL_MR", "Mauritel", mauritania),
                new Operator("MATTEL_MR", "Mattel", mauritania)
        ));

        // Niger
        operators.addAll(Arrays.asList(
                new Operator("SONITEL_NE", "Sonitel", niger),
                new Operator("NIGELEC_NE", "Nigelec", niger)
        ));

        // Nigeria
        operators.addAll(Arrays.asList(
                new Operator("MTN_NG", "MTN", nigeria),
                new Operator("GLO_NG", "Glo", nigeria),
                new Operator("AIRTEL_NG", "Airtel", nigeria),
                new Operator("9MOBILE_NG", "9mobile", nigeria)
        ));

        // Sénégal
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_SN", "Orange", senegal),
                new Operator("EXPRESSO_SN", "Expresso", senegal),
                new Operator("FREE_SN", "Free", senegal)
        ));

        // Sierra Leone
        operators.addAll(Arrays.asList(
                new Operator("AFRICELL_SL", "Africell", sierraLeone),
                new Operator("ORANGE_SL", "Orange", sierraLeone)
        ));

        // Togo
        operators.addAll(Arrays.asList(
                new Operator("TOGOCEL_TG", "Togocel", togo),
                new Operator("MOOV_TG", "Moov", togo)
        ));

        // --- AFRIQUE CENTRALE ---

        // Cameroun
        operators.addAll(Arrays.asList(
                new Operator("MTN_CM", "MTN", cameroon),
                new Operator("ORANGE_CM", "Orange", cameroon),
                new Operator("CAMTEL_CM", "Camtel", cameroon)
        ));

        // RCA
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_CF", "Orange", car),
                new Operator("TELE_CAR", "Telecel", car)
        ));

        // Tchad
        operators.addAll(Arrays.asList(
                new Operator("AIRTEL_TD", "Airtel", chad),
                new Operator("TIGO_TD", "Tigo", chad)
        ));

        // Congo-Brazzaville
        operators.addAll(Arrays.asList(
                new Operator("AIRTEL_CG", "Airtel", congo),
                new Operator("MTN_CG", "MTN", congo)
        ));

        // RDC
        operators.addAll(Arrays.asList(
                new Operator("VODACOM_CD", "Vodacom", drc),
                new Operator("ORANGE_CD", "Orange", drc),
                new Operator("AIRTEL_CD", "Airtel", drc)
        ));

        // Gabon
        operators.addAll(Arrays.asList(
                new Operator("GABON_TELECOM_GA", "Gabon Telecom", gabon),
                new Operator("LIBERTIS_GA", "Libertis", gabon)
        ));

        // --- AFRIQUE DE L'EST ---

        // Kenya
        operators.addAll(Arrays.asList(
                new Operator("SAFARICOM_KE", "Safaricom", kenya),
                new Operator("AIRTEL_KE", "Airtel", kenya),
                new Operator("TELKOM_KE", "Telkom", kenya)
        ));

        // Tanzanie
        operators.addAll(Arrays.asList(
                new Operator("VODACOM_TZ", "Vodacom", tanzania),
                new Operator("AIRTEL_TZ", "Airtel", tanzania),
                new Operator("TIGO_TZ", "Tigo", tanzania)
        ));

        // Ouganda
        operators.addAll(Arrays.asList(
                new Operator("MTN_UG", "MTN", uganda),
                new Operator("AIRTEL_UG", "Airtel", uganda)
        ));

        // Rwanda
        operators.addAll(Arrays.asList(
                new Operator("MTN_RW", "MTN", rwanda),
                new Operator("AIRTEL_RW", "Airtel", rwanda)
        ));

        // Mozambique
        operators.addAll(Arrays.asList(
                new Operator("VODACOM_MZ", "Vodacom", mozambique),
                new Operator("M_CELL_MZ", "M-Cell", mozambique)
        ));

        // Madagascar
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_MG", "Orange", madagascar),
                new Operator("TELMA_MG", "Telma", madagascar)
        ));

        // Zambie
        operators.addAll(Arrays.asList(
                new Operator("MTN_ZM", "MTN", zambia),
                new Operator("AIRTEL_ZM", "Airtel", zambia)
        ));

        // Zimbabwe
        operators.addAll(Arrays.asList(
                new Operator("ECONET_ZW", "Econet", zimbabwe),
                new Operator("NETONE_ZW", "NetOne", zimbabwe)
        ));

        // Malawi
        operators.addAll(Arrays.asList(
                new Operator("TNM_MW", "TNM", malawi),
                new Operator("AIRTEL_MW", "Airtel", malawi)
        ));

        // Maurice
        operators.addAll(Arrays.asList(
                new Operator("MYT_MU", "MYT", mauritius),
                new Operator("EMTEL_MU", "Emtel", mauritius)
        ));

        // --- AFRIQUE AUSTRALE ---

        // Afrique du Sud
        operators.addAll(Arrays.asList(
                new Operator("VODACOM_ZA", "Vodacom", southAfrica),
                new Operator("MTN_ZA", "MTN", southAfrica),
                new Operator("TELKOM_ZA", "Telkom", southAfrica),
                new Operator("CELL_C_ZA", "Cell C", southAfrica)
        ));

        // Angola
        operators.addAll(Arrays.asList(
                new Operator("UNITEL_AO", "Unitel", angola),
                new Operator("MOVICEL_AO", "Movicel", angola)
        ));

        // Namibie
        operators.addAll(Arrays.asList(
                new Operator("MTC_NA", "MTC", namibia),
                new Operator("TELECOM_NA", "Telecom", namibia)
        ));

        // Botswana
        operators.addAll(Arrays.asList(
                new Operator("MASCOM_BW", "Mascom", botswana),
                new Operator("ORANGE_BW", "Orange", botswana)
        ));

        // --- AFRIQUE DU NORD ---

        // Maroc
        operators.addAll(Arrays.asList(
                new Operator("IAM_MA", "IAM", morocco),
                new Operator("ORANGE_MA", "Orange", morocco),
                new Operator("INWI_MA", "Inwi", morocco)
        ));

        // Algérie
        operators.addAll(Arrays.asList(
                new Operator("MOBILIS_DZ", "Mobilis", algeria),
                new Operator("DJAZZY_DZ", "Djezzy", algeria),
                new Operator("OOREDOO_DZ", "Ooredoo", algeria)
        ));

        // Tunisie
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_TN", "Orange", tunisia),
                new Operator("OOREDOO_TN", "Ooredoo", tunisia),
                new Operator("TUNISIANA_TN", "Tunisiana", tunisia)
        ));

        // Égypte
        operators.addAll(Arrays.asList(
                new Operator("VODAFONE_EG", "Vodafone", egypt),
                new Operator("ORANGE_EG", "Orange", egypt),
                new Operator("ETISALAT_EG", "Etisalat", egypt)
        ));

        // Libye
        operators.addAll(Arrays.asList(
                new Operator("LIBYANA_LY", "Libyana", libya),
                new Operator("ALMADAR_LY", "Al-Madar", libya)
        ));

        // Soudan
        operators.addAll(Arrays.asList(
                new Operator("SUDANI_SD", "Sudani", sudan),
                new Operator("MTN_SD", "MTN", sudan)
        ));

        // --- EUROPE DE L'OUEST ---

        // France
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_FR", "Orange", france),
                new Operator("SFR_FR", "SFR", france),
                new Operator("FREE_FR", "Free Mobile", france),
                new Operator("BOUYGUES_FR", "Bouygues Telecom", france)
        ));

        // Allemagne
        operators.addAll(Arrays.asList(
                new Operator("T_MOBILE_DE", "T-Mobile", germany),
                new Operator("VODAFONE_DE", "Vodafone", germany),
                new Operator("O2_DE", "O2", germany)
        ));

        // Royaume-Uni
        operators.addAll(Arrays.asList(
                new Operator("EE_GB", "EE", uk),
                new Operator("VODAFONE_GB", "Vodafone", uk),
                new Operator("O2_GB", "O2", uk),
                new Operator("THREE_GB", "Three", uk)
        ));

        // Italie
        operators.addAll(Arrays.asList(
                new Operator("TIM_IT", "TIM", italy),
                new Operator("VODAFONE_IT", "Vodafone", italy),
                new Operator("WIND_IT", "Wind", italy)
        ));

        // Espagne
        operators.addAll(Arrays.asList(
                new Operator("MOVISTAR_ES", "Movistar", spain),
                new Operator("ORANGE_ES", "Orange", spain),
                new Operator("VODAFONE_ES", "Vodafone", spain)
        ));

        // Portugal
        operators.addAll(Arrays.asList(
                new Operator("MEO_PT", "MEO", portugal),
                new Operator("NOS_PT", "NOS", portugal),
                new Operator("VODAFONE_PT", "Vodafone", portugal)
        ));

        // Belgique
        operators.addAll(Arrays.asList(
                new Operator("PROXIMUS_BE", "Proximus", belgium),
                new Operator("ORANGE_BE", "Orange", belgium),
                new Operator("TELENET_BE", "Telenet", belgium)
        ));

        // Pays-Bas
        operators.addAll(Arrays.asList(
                new Operator("KPN_NL", "KPN", netherlands),
                new Operator("VODAFONE_NL", "Vodafone", netherlands),
                new Operator("T_MOBILE_NL", "T-Mobile", netherlands)
        ));

        // Suisse
        operators.addAll(Arrays.asList(
                new Operator("SWISSCOM_CH", "Swisscom", switzerland),
                new Operator("SUNRISE_CH", "Sunrise", switzerland),
                new Operator("SALT_CH", "Salt", switzerland)
        ));

        // Autriche
        operators.addAll(Arrays.asList(
                new Operator("A1_AT", "A1", austria),
                new Operator("T_MOBILE_AT", "T-Mobile", austria),
                new Operator("DREI_AT", "Drei", austria)
        ));

        // Irlande
        operators.addAll(Arrays.asList(
                new Operator("VODAFONE_IE", "Vodafone", ireland),
                new Operator("EIR_IE", "Eir", ireland),
                new Operator("THREE_IE", "Three", ireland)
        ));

        // --- EUROPE DU NORD ---

        // Danemark
        operators.addAll(Arrays.asList(
                new Operator("TDC_DK", "TDC", denmark),
                new Operator("TELENOR_DK", "Telenor", denmark),
                new Operator("THREE_DK", "Three", denmark)
        ));

        // Suède
        operators.addAll(Arrays.asList(
                new Operator("TELIA_SE", "Telia", sweden),
                new Operator("TELENOR_SE", "Telenor", sweden),
                new Operator("THREE_SE", "Three", sweden)
        ));

        // Norvège
        operators.addAll(Arrays.asList(
                new Operator("TELENOR_NO", "Telenor", norway),
                new Operator("TELIA_NO", "Telia", norway),
                new Operator("ICE_NO", "Ice", norway)
        ));

        // Finlande
        operators.addAll(Arrays.asList(
                new Operator("TELIA_FI", "Telia", finland),
                new Operator("DNA_FI", "DNA", finland),
                new Operator("ELISA_FI", "Elisa", finland)
        ));

        // Islande
        operators.addAll(Arrays.asList(
                new Operator("SÍMINN_IS", "Síminn", iceland),
                new Operator("VODAFONE_IS", "Vodafone", iceland)
        ));

        // --- EUROPE DE L'EST ---

        // Pologne
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_PL", "Orange", poland),
                new Operator("T_MOBILE_PL", "T-Mobile", poland),
                new Operator("PLUS_PL", "Plus", poland)
        ));

        // République Tchèque
        operators.addAll(Arrays.asList(
                new Operator("O2_CZ", "O2", czechia),
                new Operator("T_MOBILE_CZ", "T-Mobile", czechia),
                new Operator("VODAFONE_CZ", "Vodafone", czechia)
        ));

        // Roumanie
        operators.addAll(Arrays.asList(
                new Operator("ORANGE_RO", "Orange", romania),
                new Operator("VODAFONE_RO", "Vodafone", romania),
                new Operator("DIGI_RO", "Digi", romania)
        ));

        // Grèce
        operators.addAll(Arrays.asList(
                new Operator("COSMOTE_GR", "Cosmote", greece),
                new Operator("VODAFONE_GR", "Vodafone", greece),
                new Operator("WIND_GR", "Wind", greece)
        ));

        // Hongrie
        operators.addAll(Arrays.asList(
                new Operator("MAGYAR_HU", "Magyar", hungary),
                new Operator("VODAFONE_HU", "Vodafone", hungary),
                new Operator("T_MOBILE_HU", "T-Mobile", hungary)
        ));

        // Bulgarie
        operators.addAll(Arrays.asList(
                new Operator("A1_BG", "A1", bulgaria),
                new Operator("VIVACOM_BG", "Vivacom", bulgaria),
                new Operator("TELENOR_BG", "Telenor", bulgaria)
        ));

        // Croatie
        operators.addAll(Arrays.asList(
                new Operator("HT_HR", "HT", croatia),
                new Operator("TELE2_HR", "Tele2", croatia)
        ));

        // Serbie
        operators.addAll(Arrays.asList(
                new Operator("TELENOR_RS", "Telenor", serbia),
                new Operator("VIP_RS", "Vip", serbia)
        ));

        // --- TURQUIE ---
        operators.addAll(Arrays.asList(
                new Operator("TURKCELL_TR", "Turkcell", turkiye),
                new Operator("VODAFONE_TR", "Vodafone", turkiye),
                new Operator("TURK_TELEKOM_TR", "Turk Telekom", turkiye)
        ));

        // ============================================
        // SAUVEGARDE DES OPÉRATEURS
        // ============================================

        List<Operator> savedOperators = operatorRepository.saveAll(operators);
        log.info("✅ {} opérateurs créés", savedOperators.size());

        // ============================================
        // RÉSUMÉ
        // ============================================

        log.info("========================================");
        log.info("📊 DONNÉES DE RÉFÉRENCE CHARGÉES");
        log.info("   🌍 Pays: {}", countries.size());
        log.info("   📱 Opérateurs: {}", savedOperators.size());
        log.info("   🌐 Pays couverts:");
        log.info("      Afrique de l'Ouest: 16 pays");
        log.info("      Afrique Centrale: 8 pays");
        log.info("      Afrique de l'Est: 16 pays");
        log.info("      Afrique Australe: 7 pays");
        log.info("      Afrique du Nord: 7 pays");
        log.info("      Europe de l'Ouest: 17 pays");
        log.info("      Europe du Nord: 5 pays");
        log.info("      Europe de l'Est: 16 pays");
        log.info("========================================");
    }
}