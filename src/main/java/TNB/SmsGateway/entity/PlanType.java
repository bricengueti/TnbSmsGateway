package TNB.SmsGateway.entity;

public enum PlanType {
    POOL,      // crédits SMS prépayés, consommés via le pool partagé
    PERSONAL   // limite de devices (+ éventuellement SMS), pour les clients BYOD
}