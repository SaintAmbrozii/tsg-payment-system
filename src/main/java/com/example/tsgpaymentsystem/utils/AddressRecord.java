package com.example.tsgpaymentsystem.utils;

import com.example.tsgpaymentsystem.domain.Address;
import com.example.tsgpaymentsystem.exception.InvalidAddressFormatException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class AddressRecord {

    private String apartment;
    private String building;

    public static AddressRecord createAddressRecord(String addressString) {
        try {
            String[] addressItems = addressString.split(",");
            String building = String.join(",", addressItems[0], addressItems[1], addressItems[2]);
            String object = addressItems[3];
            return new AddressRecord(object, building);
        } catch (Throwable any) {
            throw new InvalidAddressFormatException(addressString);
        }
    }


    public static String from(Address address) {
        return address.getBuilding().getBuilding() + "," + address.getApartment();
    }
}
