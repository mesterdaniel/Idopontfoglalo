package com.BC.Idopontfoglalo.entity;

/**
 * Az időpont állapotokat reprezentáló enum
 */
public enum AppointmentStatus {
    PENDING("Függőben"),      // Várja a jóváhagyást
    CONFIRMED("Jóváhagyva"),  // Megerősített
    CANCELLED("Lemondva"),    // Lemondott
    COMPLETED("Befejezett");  // Megtörtént

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Megmondja, hogy ez egy aktív állapot-e (nem lemondott/befejezett)
     */
    public boolean isActive() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * CSS osztály név az állapot színezéséhez
     */
    public String getCssClass() {
        switch (this) {
            case PENDING: return "status-pending";
            case CONFIRMED: return "status-confirmed";
            case CANCELLED: return "status-cancelled";
            case COMPLETED: return "status-completed";
            default: return "";
        }
    }
}