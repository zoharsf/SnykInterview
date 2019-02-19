package com.zoharsf.snykinterview;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.MoreExecutors;
import com.zoharsf.snykinterview.model.NpmPackage;
import com.zoharsf.snykinterview.model.PackageDependencyTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
@Slf4j
public class ApplicationManager {

    @Autowired
    private ApplicationCache applicationCache;
    private ExecutorService executor = MoreExecutors.getExitingExecutorService((ThreadPoolExecutor) Executors.newCachedThreadPool());

    public Set<NpmPackage> getDependencies(NpmPackage npmPackage) {
        Set<NpmPackage> dependencies = null;
        if (npmPackage != null) {
            if (npmPackage.isValid()) {
                log.info("Requested package is valid: {}.", npmPackage);
                if (!applicationCache.hasRequestedDependency(npmPackage)) {
                    populateCacheWithDependencies(npmPackage);
                }
                dependencies = getCachedDependencies(npmPackage);
            } else {
                log.error("The requested package is invalid: {}.", npmPackage);
            }
        } else {
            log.error("There was a problem parsing the requested package.");
        }
        return dependencies;
    }

    private void populateCacheWithDependencies(NpmPackage npmPackage) {
        Queue<NpmPackage> uncheckedDependencies = new ArrayDeque<>();
        AtomicReference<Set<NpmPackage>> dependenciesForSingleNpmPackage = new AtomicReference<>();
        uncheckedDependencies.add(npmPackage);

        log.info("Cache will now be populated.");
        while (!uncheckedDependencies.isEmpty()) {
            try {
                runAsync(() -> {
                    NpmPackage uncheckedDependency = uncheckedDependencies.remove();
                    dependenciesForSingleNpmPackage.set(getDependenciesForSinglePackage(uncheckedDependency));
                    uncheckedDependencies.addAll(dependenciesForSingleNpmPackage.get());
                    applicationCache.addDependencyToCache(uncheckedDependency, dependenciesForSingleNpmPackage.get());
                }, executor).get(60, SECONDS);
            } catch (TimeoutException e) {
                log.error("Cache population timed out: {}", e.getMessage());
            } catch (ExecutionException e) {
                log.error("Cache population failed: " + e.getMessage());
            } catch (InterruptedException e) {
                log.error("Cache population interrupted: " + e.getMessage());
            }
        }
    }

    private Set<NpmPackage> getDependenciesForSinglePackage(NpmPackage npmPackage) {
        Set<NpmPackage> npmPackageArrayList = new HashSet<>();

        String url = "https://registry.npmjs.org/" + npmPackage.getPackageName() + "/" + npmPackage.getVersion();
        StringBuilder response = new StringBuilder();

        log.info("Registry will be queried for the following package: {} ({})", npmPackage, url);
        try {
            URL urlObject = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while (true) {
                inputLine = in.readLine();
                if (inputLine == null) {
                    break;
                }
                response.append(inputLine);
            }
            in.close();

            try {
                JsonNode jsonNode = new ObjectMapper().readTree(response.toString()).get("dependencies");


                NpmPackage dependency;
                ObjectMapper objectMapper = new ObjectMapper();

                HashMap<String, String> dependencies = objectMapper.convertValue(jsonNode, new TypeReference<HashMap<String, String>>() {
                });

                if (dependencies != null && !dependencies.isEmpty()) {
                    for (Map.Entry<String, String> entry : dependencies.entrySet()) {
                        dependency = extractDependency(entry.getKey(), entry.getValue());
                        log.info("New dependency extracted from registry response: {}", dependency);
                        npmPackageArrayList.add(dependency);
                    }
                }
            } catch (FileNotFoundException e) {
                log.error("Error parsing response from registry: {}", e.getMessage());
            }
        } catch (IOException e) {
            log.error("Error contacting registry: {}", e.getMessage());
        }
        return npmPackageArrayList;
    }

    private NpmPackage extractDependency(String key, String value) {
        String fixedValue = value.replace(" ", "").trim()
                .replaceAll("x", "0").trim()
                .replaceAll("<=", "").trim()
                .replaceAll(">=", "").trim()
                .replaceAll("[~^]", "").trim();
        if (fixedValue.contains(">")) {
            fixedValue = fixedValue.substring(0, fixedValue.indexOf(">")).trim();
        }
        if (fixedValue.contains("<")) {
            fixedValue = fixedValue.substring(0, fixedValue.indexOf("<")).trim();
        }
        if (fixedValue.contains("||")) {
            fixedValue = fixedValue.substring(0, fixedValue.indexOf("||")).trim();
        }
        return new NpmPackage(key, fixedValue);
    }

    private Set<NpmPackage> getCachedDependencies(NpmPackage npmPackage) {
        return applicationCache.getDependencies(npmPackage);
    }

    public PackageDependencyTree generatePackageDependencyTree(NpmPackage npmPackage) {
        PackageDependencyTree packageDependencyTree = new PackageDependencyTree();
        packageDependencyTree.setNpmPackage(npmPackage);
        Set<NpmPackage> uncheckedPackages;

        if (applicationCache.hasRequestedDependency(npmPackage)) {
            uncheckedPackages = applicationCache.getSuccessors(npmPackage);
            for (NpmPackage dependency : uncheckedPackages)
                packageDependencyTree.getPackageDependencyTree().add(generatePackageDependencyTree(dependency));
        }
        return packageDependencyTree;
    }
}
