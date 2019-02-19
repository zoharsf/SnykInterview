package com.zoharsf.snykinterview;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.zoharsf.snykinterview.model.NpmPackage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class ApplicationCache {

    private MutableGraph<NpmPackage> dependencyGraph = GraphBuilder.directed().build();

    //check if dependency graph contains requested package
    public boolean hasRequestedDependency(NpmPackage npmPackage) {
        boolean hasDependency = dependencyGraph.nodes().contains(npmPackage);
        if (hasDependency) {
            log.info("Cache contains requested package: {}", npmPackage);
        } else {
            log.info("Cache does not contain requested package and will be populated from registry: {}", npmPackage);
        }
        return hasDependency;
    }

    //get all of the dependencies for a given package from dependency graph
    public Set<NpmPackage> getDependencies(NpmPackage npmPackage) {
        log.info("Getting dependencies for {} from cache", npmPackage);
        Set<NpmPackage> dependencies = new HashSet<>(Graphs.reachableNodes(dependencyGraph, npmPackage));
        log.info("Discovered {} dependencies for {}: {}", dependencies.size(), npmPackage, dependencies);
        return dependencies;
    }

    //add new package to dependency graph
    public void addDependencyToCache(NpmPackage npmPackage, Set<NpmPackage> dependencies) {
        log.info("Adding discovered dependencies to cache: {}.", npmPackage);
        dependencyGraph.addNode(npmPackage);
        for (NpmPackage dependency : dependencies) {
            dependencyGraph.putEdge(npmPackage, dependency);
        }
    }

    public Set<NpmPackage> getSuccessors(NpmPackage npmPackage) {
        return dependencyGraph.successors(npmPackage);
    }
}
