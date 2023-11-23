package com.example.tsgpaymentsystem.dto;

import com.example.tsgpaymentsystem.domain.PayAgent;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PayAgentDto {

    private Long id;
    private String username;
    private String phone;
    private boolean active;
    private String password;

    public static PayAgentDto toDto(PayAgent agent) {
        PayAgentDto dto = new PayAgentDto();
        dto.setId(agent.getId());
        dto.setActive(agent.isActive());
        dto.setPhone(agent.getPhone());
        dto.setUsername(agent.getUsername());
        dto.setPassword(agent.getPassword());
        return dto;
    }
}
