package com.example.tsgpaymentsystem.dto;


import com.example.tsgpaymentsystem.domain.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@ToString
@Getter
@Setter
public class UserDTO {

    private Long id;
    private String email;
    private String name;
    private String lastname;
    private String address;
    private String contract;
    private String phone;
    private boolean active;
    private boolean api;
    private boolean aggregated = true;
    private String role;
    private String password;


    public static UserDTO info(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setLastname(user.getLastname());
        dto.setAddress(user.getAddress());
        dto.setPhone(user.getPhone());
        dto.setRole(String.valueOf(user.getRole()));
        dto.setContract(user.getContract());
        dto.setAggregated(user.getIsAggregated());
        return dto;
    }

    public static UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setLastname(user.getLastname());
        dto.setAddress(user.getAddress());
        dto.setPhone(user.getPhone());
        dto.setActive(user.getIsAktive());
        dto.setApi(user.getApi());
        dto.setAggregated(user.getIsAggregated());
        dto.setRole(String.valueOf(user.getRole()));
        dto.setPassword(user.getPassword());
        dto.setContract(user.getContract());
        return dto;
    }



}
