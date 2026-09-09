package com.formation.booking.client;

import com.formation.booking.dto.ClassDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "class-service")
public interface ClassClient {

    @GetMapping("/api/classes/{id}")
    ClassDto getClassById(@PathVariable("id") Long id);

    @RequestMapping(method = RequestMethod.PATCH, value = "/api/classes/{id}/increment")
    void increment(@PathVariable("id") Long id, @RequestParam("spots") int spots);

    @RequestMapping(method = RequestMethod.PATCH, value = "/api/classes/{id}/decrement")
    void decrement(@PathVariable("id") Long id);
}
