package com.zoharsf.snykinterview.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class PackageDependencyTree {

    private NpmPackage npmPackage;
    private Set<PackageDependencyTree> packageDependencyTree = new HashSet<>();
}
