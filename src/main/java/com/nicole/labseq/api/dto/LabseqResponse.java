package com.nicole.labseq.api.dto;

public class LabseqResponse {
    public long index;
    public String value;

    public LabseqResponse() {}

    public LabseqResponse(long index, String value) {
        this.index = index;
        this.value = value;
    }
}
