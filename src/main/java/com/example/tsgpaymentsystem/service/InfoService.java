package com.example.tsgpaymentsystem.service;

import com.example.tsgpaymentsystem.domain.*;
import com.example.tsgpaymentsystem.dto.InfoDto;
import com.example.tsgpaymentsystem.dto.InformationResponseDto;
import com.example.tsgpaymentsystem.dto.OptionDto;
import com.example.tsgpaymentsystem.dto.RequestInfoDto;
import com.example.tsgpaymentsystem.exception.AddressNotFoundException;
import com.example.tsgpaymentsystem.exception.UserNotFoundException;
import com.example.tsgpaymentsystem.repository.*;
import com.example.tsgpaymentsystem.utils.AddressRecord;
import com.example.tsgpaymentsystem.utils.StandartServiceName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InfoService {

    private final AccountRepository accountRepository;
    private final ServiceRepository serviceRepository;
    private final AddressRepository addressRepository;
    private final BuildingRepository buildingRepository;
    private final CalculationRepo calculationsRepository;
    private final PaymentRepository paymentsRepository;
    private final UserRepository userRepository;

    public InfoService(AccountRepository accountRepository, ServiceRepository serviceRepository,
                       AddressRepository addressRepository, BuildingRepository buildingRepository,
                       CalculationRepo calculationsRepository, PaymentRepository paymentsRepository,
                       UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.serviceRepository = serviceRepository;
        this.addressRepository = addressRepository;
        this.buildingRepository = buildingRepository;
        this.calculationsRepository = calculationsRepository;
        this.paymentsRepository = paymentsRepository;
        this.userRepository = userRepository;
    }

    public InformationResponseDto findInfoByUser(User user, RequestInfoDto req) throws AccountNotFoundException, AddressNotFoundException {

        /*
        Request
        {
            service: "municipal",
            provider: "tsg-zolotaya-niva",
            account: "002"
        }
         Response
         {
            address: ["Новосибирск, ул. Богаткова 228, кв. 28"],
            span: [
                { "name": "Содержание дома", "balance": -150000 },
                { "name": "Капитальный ремонт", "balance": 300 }
            ],
            enabled: true
         }
         */


         user = userRepository.findByEmail(req.getTsgId()).orElseThrow();
        return ObjectUtils.isEmpty(req.getAddress())
                ? getInformationByAccount(user, req)
                : getInformationByAccountAndAddress(user, req);


    }
    private InformationResponseDto getInformationByAccountAndAddress(User user, RequestInfoDto req)
            throws AccountNotFoundException, AddressNotFoundException {

        List<Account> accounts = accountRepository
                .findByUserAndAccountAndLastUpload(user, req.getAccount(), user.getLastUpload());

        if (accounts.size() > 1)
            throw new IllegalStateException("We assumed that accounts are unique but it isn't true so discuss it");

        if (accounts.isEmpty())
            throw new AccountNotFoundException(req.getAccount());

        AddressRecord addressRecord = AddressRecord.createAddressRecord(req.getAddress());

        Account foundAccount = null;
        Address foundAddress = null;

        Building building = buildingRepository.findByUserAndBuilding(user, addressRecord.getBuilding());

        for (Account account : accounts) {
            List<Address> addresses = addressRepository
                    .findByUserAndAccountAndApartmentAndBuilding(user, account, addressRecord.getApartment(), building);

            if (addresses.isEmpty())
                continue;

            for (Address a : addresses) {

                if (a.getBuilding().getBuilding().equalsIgnoreCase(addressRecord.getBuilding())
                        && a.getApartment().equalsIgnoreCase(addressRecord.getApartment())) {
                    foundAccount = account;
                    foundAddress = a;
                    break;
                }

            }
        }

        if (foundAddress == null)
            throw new AddressNotFoundException(req.getAccount(), req.getAddress());

        // if (addresses.size() > 1)
        //    throw new IllegalStateException("Found multiple record for account=" + account + " and addressRecord " + addressRecord);

        List<Calculation> calculations = calculationsRepository
                .findByUserAndAccountAndAddressAndLastUploadOrderById(user, foundAccount, foundAddress, user.getLastUpload());
        List<Payment> payments = paymentsRepository
                .findByUserAndAccountAndAddressAndLastUploadOrderById(user, foundAccount, foundAddress, user.getLastUpload());
        return InformationResponseDto.found(foundAddress, calculations, payments);
    }

    private InformationResponseDto getInformationByAccount(User user, RequestInfoDto req) throws AccountNotFoundException, AddressNotFoundException {
        log.debug(">>> Looking for {} by user {} from date {}", req.getAccount(), user.getUsername(), user.getLastUpload());
        List<Account> accounts = accountRepository
                .findByUserAndAccountAndLastUpload(user, req.getAccount(), user.getLastUpload());
        log.debug(">>> Found accounts {}", accounts);

        if (accounts.size() > 1)
            throw new IllegalStateException("Мы предполагаем что аккаунт всегда уникальный, в базе есть несколько записей с этим номером "
                    + req.getAccount() + " дата последней загрузки " + user.getLastUpload());

        if (accounts.isEmpty())
            throw new AccountNotFoundException(req.getAccount());

        log.debug(">>> addressRepository.findByUserAndAccount({}, {})", user.getId(), accounts.get(0).getId());
        List<Address> addresses = addressRepository.findByUserAndAccount(user, accounts.get(0));
        if (addresses.isEmpty())
            throw new AddressNotFoundException(req.getAccount(), req.getAddress());

        if (addresses.size() > 1)
            return InformationResponseDto.multiAddress(addresses);

        Address address = addresses.get(0);
        Account account = accounts.get(0);

        List<Calculation> calculations = calculationsRepository
                .findByUserAndAccountAndAddressAndLastUploadOrderById(user, account, address, user.getLastUpload());
        List<Payment> payments = paymentsRepository
                .findByUserAndAccountAndAddressAndLastUploadOrderById(user, account, address, user.getLastUpload());

        InformationResponseDto dto = InformationResponseDto.found(address, calculations, payments);
        log.debug(">>> result {}", dto);
        return dto;
    }
    public InfoDto findInfoByUser(User user) {

        List<Account> accounts = accountRepository.findAllByUserOrderByAccount(user);
        List<com.example.tsgpaymentsystem.domain.Service> services = serviceRepository.findAllByUserOrderByService(user);
        List<Building> buildings = buildingRepository.findAllByUser(user);


        InfoDto infoDto = new InfoDto();
        infoDto.accounts = accounts.stream()
                .map(i -> new OptionDto(i.getId(), i.getAccount())).toArray(OptionDto[]::new);

        infoDto.services = services.stream()
                .map(i -> new OptionDto(i.getId(), i.getService())).toArray(OptionDto[]::new);

        infoDto.addresses = buildings.stream()
                .map(i -> new OptionDto(i.getId(), i.getBuilding())).toArray(OptionDto[]::new);

        return infoDto;

    }

    public OptionDto[] findDefaultServices(User user) {

        List<com.example.tsgpaymentsystem.domain.Service> services = serviceRepository.findAllByUserOrderByService(user);

        Map<String, com.example.tsgpaymentsystem.domain.Service> persistedServices = services
                .stream()
                .collect(Collectors.toMap(com.example.tsgpaymentsystem.domain.Service::getService, v -> v));

        List<com.example.tsgpaymentsystem.domain.Service> defaultServices = getOrCreateDefaultServices(user, persistedServices);

        return defaultServices.stream()
                .map(i -> new OptionDto(i.getId(), i.getService())).toArray(OptionDto[]::new);
    }

    private List<com.example.tsgpaymentsystem.domain.Service> getOrCreateDefaultServices(User user, Map<String, com.example.tsgpaymentsystem.domain.Service> persistedServices) {

        List<com.example.tsgpaymentsystem.domain.Service> services = new ArrayList<>();

        if (!persistedServices.containsKey(StandartServiceName.ALL))
            services.add(serviceRepository.save(createService(user, StandartServiceName.ALL)));
        else
            services.add(persistedServices.get(StandartServiceName.ALL));

        if (!persistedServices.containsKey(StandartServiceName.RENEWAL))
            services.add(serviceRepository.save(createService(user, StandartServiceName.RENEWAL)));
        else
            services.add(persistedServices.get(StandartServiceName.RENEWAL));

        return services;
    }
    private static com.example.tsgpaymentsystem.domain.Service createService(User user, String serviceName) {
        com.example.tsgpaymentsystem.domain.Service service = new com.example.tsgpaymentsystem.domain.Service();
        service.setService(serviceName);
        service.setUser(user);
        return service;
    }

}
