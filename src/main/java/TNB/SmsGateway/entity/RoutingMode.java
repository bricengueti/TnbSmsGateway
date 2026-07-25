package TNB.SmsGateway.entity;

public enum RoutingMode {
    OWN_DEVICES,   // comportement BYOD actuel — inchangé
    MANAGED_POOL   // route vers les devices de type POOL, quel que soit leur propriétaire
}