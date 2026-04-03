package com.group4.backend.dto.admin;

import java.util.List;

public class AdminUserPageResponse {

    private List<AdminUserSummaryDto> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public List<AdminUserSummaryDto> getContent() {
        return content;
    }

    public void setContent(List<AdminUserSummaryDto> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
