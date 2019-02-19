package com.zoharsf.snykinterview.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class NpmPackage {

    @JsonProperty("name")
    private String packageName;
    @JsonProperty("version")
    private String version;

    public NpmPackage(String packageName, String version) {
        this.packageName = packageName;
        this.version = version;
    }

    @JsonIgnore
    public boolean isValid() {
        return (packageName != null && !packageName.isEmpty()) &&
                (version != null && !version.isEmpty());
    }
}
