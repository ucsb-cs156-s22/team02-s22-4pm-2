package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
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

@Api(description = "Help Request")
@RequestMapping("/api/helprequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

    @Autowired
    HelpRequestRepository helpRequestRepository;

    @ApiOperation(value = "List all help requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<HelpRequest> allHelpRequests() {
        Iterable<HelpRequest> helpRequests = helpRequestRepository.findAll();
        return helpRequests;
    }

    @ApiOperation(value = "Get a single help request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public HelpRequest getById(
            @ApiParam("id") @RequestParam Long id) {
        HelpRequest helpRequest = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

        return helpRequest;
    }

    @ApiOperation(value = "Create a new help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public HelpRequest postHelpRequest(
            @ApiParam("requesterEmail") @RequestParam String requesterEmail,
            @ApiParam("teamId") @RequestParam String teamId,
            @ApiParam("tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
            @ApiParam("date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("requestTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime requestTime,
            @ApiParam("explanation") @RequestParam String explanation,
            @ApiParam("solved") @RequestParam boolean solved)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("requestTime={}", requestTime);

        HelpRequest helpRequest = new HelpRequest();
        HelpRequest.setRequesterEmail(requesterEmail);
        ucsbDate.setName(name);
        ucsbDate.setLocalDateTime(localDateTime);

        UCSBDate savedUcsbDate = ucsbDateRepository.save(ucsbDate);

        return savedUcsbDate;
    }

    @ApiOperation(value = "Delete a UCSBDate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBDate(
            @ApiParam("id") @RequestParam Long id) {
        UCSBDate ucsbDate = ucsbDateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDate.class, id));

        ucsbDateRepository.delete(ucsbDate);
        return genericMessage("UCSBDate with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single date")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDate updateUCSBDate(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid UCSBDate incoming) {

        UCSBDate ucsbDate = ucsbDateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDate.class, id));

        ucsbDate.setQuarterYYYYQ(incoming.getQuarterYYYYQ());
        ucsbDate.setName(incoming.getName());
        ucsbDate.setLocalDateTime(incoming.getLocalDateTime());

        ucsbDateRepository.save(ucsbDate);

        return ucsbDate;
    }
}
