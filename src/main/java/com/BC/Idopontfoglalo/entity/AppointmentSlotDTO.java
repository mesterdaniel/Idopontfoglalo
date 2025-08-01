package com.BC.Idopontfoglalo.entity;

import java.time.LocalDateTime;

public class AppointmentSlotDTO {
    private Long id;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public Integer getCurrentAttendees() {
        return currentAttendees;
    }

    public void setCurrentAttendees(Integer currentAttendees) {
        this.currentAttendees = currentAttendees;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String typeName;
    private Integer maxAttendees;
    private Integer currentAttendees;
    private String status;

    // getters, setters, constructors
}