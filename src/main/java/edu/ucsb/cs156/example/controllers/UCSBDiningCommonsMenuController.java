package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenu;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "UCSBDiningCommonsMenu")
@RequestMapping("/api/ucsbdiningcommonsmenu")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuController extends ApiController {
    
    @Autowired
    UCSBDiningCommonsMenuRepository ucsbDiningCommonsMenuRepository;

    @ApiOperation(value = "List all ucsb dining commons menus")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBDiningCommonsMenu> allUCSBDiningCommonsMenus() {
        Iterable<UCSBDiningCommonsMenu> diningcommonsmenus = ucsbDiningCommonsMenuRepository.findAll();
        return diningcommonsmenus;
    }

    @ApiOperation(value = "Get a single dining commons menu item")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenu getById(
            @ApiParam("id") @RequestParam Long id) {
        UCSBDiningCommonsMenu ucsbDiningCommonsMenu = ucsbDiningCommonsMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenu.class, id));

        return ucsbDiningCommonsMenu;
    }

    @ApiOperation(value = "Create a new dining commons menu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenu postUCSBDiningCommonsMenu(
            @ApiParam("diningCommonsCode") @RequestParam String diningCommonsCode,
            @ApiParam("name") @RequestParam String name,
            @ApiParam("station") @RequestParam String station)
            throws JsonProcessingException {


        //log.info("localDateTime={}", localDateTime);

        UCSBDiningCommonsMenu ucsbDiningCommonsMenu = new UCSBDiningCommonsMenu();
        ucsbDiningCommonsMenu.setDiningCommonsCode(diningCommonsCode);
        ucsbDiningCommonsMenu.setName(name);
        ucsbDiningCommonsMenu.setStation(station);

        UCSBDiningCommonsMenu savedUcsbDiningCommonsMenu = ucsbDiningCommonsMenuRepository.save(ucsbDiningCommonsMenu);

        return savedUcsbDiningCommonsMenu;
    }

    @ApiOperation(value = "Delete a UCSBDiningCommonsMenu")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBDiningCommonsMenu(
            @ApiParam("id") @RequestParam Long id) {
        UCSBDiningCommonsMenu ucsbDiningCommonsMenu = ucsbDiningCommonsMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenu.class, id));

        ucsbDiningCommonsMenuRepository.delete(ucsbDiningCommonsMenu);
        return genericMessage("UCSBDiningCommonsMenu with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single dining commons menu")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenu updateUCSBDiningCommonsMenu(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenu incoming) {

        UCSBDiningCommonsMenu ucsbDiningCommonsMenu = ucsbDiningCommonsMenuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenu.class, id));

        ucsbDiningCommonsMenu.setDiningCommonsCode(incoming.getDiningCommonsCode());
        ucsbDiningCommonsMenu.setName(incoming.getName());
        ucsbDiningCommonsMenu.setStation(incoming.getStation());

        ucsbDiningCommonsMenuRepository.save(ucsbDiningCommonsMenu);

        return ucsbDiningCommonsMenu;
    }
}
