package com.zoharsf.snykinterview;

import com.zoharsf.snykinterview.model.PackageDependencyTree;
import lombok.extern.slf4j.Slf4j;
import com.zoharsf.snykinterview.model.NpmPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@RestController
public class Controller {

    @Autowired
    private ApplicationManager applicationManager;

    // Incoming message from client.
    @RequestMapping(value = "/dependencies", method = RequestMethod.POST)
    public Set<NpmPackage> handleIncomingDependencyRequestFromClient(@RequestBody NpmPackage npmPackage) {
        log.info("Received dependency request from client: {}.", npmPackage);
        return applicationManager.getDependencies(npmPackage);
    }

    // Incoming message from client.
    @RequestMapping(value = "/dependencyTree", method = RequestMethod.POST)
    public PackageDependencyTree handleIncomingDependencyTreeRequestFromClient(@RequestBody NpmPackage npmPackage) {
        log.info("Received dependencyTree request from client: {}.", npmPackage);
        applicationManager.getDependencies(npmPackage);
        return applicationManager.generatePackageDependencyTree(npmPackage);
    }
}
