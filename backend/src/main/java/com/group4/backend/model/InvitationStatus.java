package com.group4.backend.model;

public enum InvitationStatus {
    PENDING,   // sent, awaiting response
    ACCEPTED,
    REJECTED,
    NEGOTIATING,
    CONFIRMED,
    EXPIRED,
    WITHDRAWN
}
