package com.example.tsgpaymentsystem.dto;

import com.example.tsgpaymentsystem.domain.Address;
import com.example.tsgpaymentsystem.domain.Calculation;
import com.example.tsgpaymentsystem.domain.Payment;
import com.example.tsgpaymentsystem.domain.Service;
import com.example.tsgpaymentsystem.utils.AddressRecord;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InformationResponseDto {

    private List<String> address;
    private List<SpanItem> span;
    private int status = 200;
    private String error;

    public static InformationResponseDto err(Throwable any) {
        InformationResponseDto informationResponseDto = new InformationResponseDto();
        informationResponseDto.error = any.getLocalizedMessage();
        informationResponseDto.status = 500;
        return informationResponseDto;
    }

    public static InformationResponseDto multiAddress(List<Address> addresses) {
        InformationResponseDto informationResponseDto = new InformationResponseDto();
        informationResponseDto.address = addresses.stream()
                .map(AddressRecord::from)
                .collect(Collectors.toList());
        return informationResponseDto;
    }

    public static InformationResponseDto found(Address address, List<Calculation> calculations, List<Payment> payments) {
        InformationResponseDto dto = new InformationResponseDto();
        Set<Long> servicesFromCalculations = calculations.stream().map(e -> e.getService().getId()).collect(Collectors.toSet());
        dto.address = Collections.singletonList(AddressRecord.from(address));
        dto.span = Stream.concat(
                        calculations.stream().map(SpanItem::from),
                        payments.stream().filter(p -> !servicesFromCalculations.contains(p.getService().getId()))
                                .collect(Collectors.groupingBy(p -> p.getService().getService(), Collectors.summingInt(p -> (int)(p.getPayment() * 100d))))
                                .entrySet().stream()
                                .map(e -> new SpanItem(e.getKey(), e.getKey(), e.getValue()))
                )
                .collect(Collectors.groupingBy(SpanItem::getKey,
                        Collectors.reducing((a, b) -> new SpanItem(a.getName(), a.getGroup(), a.getBalance() + b.getBalance()))))
                .values().stream().filter(Optional::isPresent).map(Optional::get).sorted(Comparator.comparing(SpanItem::getKey))
                .collect(Collectors.toList());
        return dto;
    }

    @Data
    public static class SpanItem {
        private final String name;
        private final String group;
        private final int balance;

        public static SpanItem from(Calculation c) {
            return new SpanItem(
                    Optional.ofNullable(c.getGroup()).filter(g -> c.getService().isCommon()).map(Service::getService).orElseGet(() -> c.getService().getService()),
                    Optional.ofNullable(c.getGroup()).map(Service::getService).orElse(null),
                    (int) ((c.getOutstandingDebt() == null ? 0d : c.getOutstandingDebt()) * -100d)
            );
        }

        @JsonIgnore()
        public Key getKey() {
            return new Key(name, group);
        }

        @Data
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Key implements Comparable<Key> {
            private final String group;
            private final String name;
            @Override
            public int compareTo(Key o) {
                int result;
                if ((result = Optional.ofNullable(getGroup()).orElse("")
                        .compareTo(Optional.ofNullable(o.getGroup()).orElse(""))) != 0)
                    return result;
                if ((result = Optional.ofNullable(getName()).orElse("")
                        .compareTo(Optional.ofNullable(o.getName()).orElse(""))) != 0)
                    return result;
                return result;
            }
        }
    }
}
