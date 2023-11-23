package com.example.tsgpaymentsystem.controller;

import com.example.tsgpaymentsystem.dto.UserDTO;
import com.example.tsgpaymentsystem.dto.seacrhcriteria.UserSearchCriteria;
import com.example.tsgpaymentsystem.repository.UserRepository;
import com.example.tsgpaymentsystem.service.UserService;
import com.example.tsgpaymentsystem.sprecifications.UserSpecs;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@Slf4j
@SecurityRequirement(name = "JWT")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Page<UserDTO> list(@RequestParam(value = "page", defaultValue = "0", required = false) int page,
                              @RequestParam(value = "count", defaultValue = "10", required = false) int size,
                              @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                              @RequestParam(value = "sort", defaultValue = "id", required = false) String sortProperty) {
        Sort sort = Sort.by(new Sort.Order(direction, sortProperty));
        Pageable pageable = PageRequest.of(page, size, sort);
        return userRepository.findAll(pageable).map(UserDTO::toDto);
    }

    @GetMapping("/filter")
    public Page<UserDTO> filter(UserSearchCriteria query) {
        log.debug("UserSearchCriteria={}", query);

      //  query.validate();
        return userRepository
                .findAll(UserSpecs.accordingToReportProperties(query), query.getPageable())
                .map(UserDTO::toDto);
    }

    @GetMapping("/get")
    public UserDTO get(@RequestParam("id") Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping("/add")
    public UserDTO add(@RequestBody UserDTO userData) throws Exception {
        return UserDTO.toDto(userService.createUser(userData));
    }

    @PostMapping("/update")
    public UserDTO update(@RequestBody UserDTO userData) {
        return UserDTO.toDto(userService.updateUser(userData));
    }

}
