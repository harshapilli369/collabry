package com.group4.backend.dto;

public class DeliverableUpdateRequest {
    private String deliverableStatus;
    private String contentLink;
    private String deliverableNotes;

    public DeliverableUpdateRequest() {}

    public String getDeliverableStatus() { return deliverableStatus; }
    public void setDeliverableStatus(String deliverableStatus) { this.deliverableStatus = deliverableStatus; }
    public String getContentLink() { return contentLink; }
    public void setContentLink(String contentLink) { this.contentLink = contentLink; }
    public String getDeliverableNotes() { return deliverableNotes; }
    public void setDeliverableNotes(String deliverableNotes) { this.deliverableNotes = deliverableNotes; }
}
