package com.example.tsgpaymentsystem.service;

import com.example.tsgpaymentsystem.domain.*;
import com.example.tsgpaymentsystem.exception.FileContainsCorruptedLines;
import com.example.tsgpaymentsystem.repository.*;
import com.example.tsgpaymentsystem.service.processor.CalculationDocumentProcessor;
import com.example.tsgpaymentsystem.utils.AddressRecord;
import com.example.tsgpaymentsystem.utils.DateUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CalculationProcessorService {

    private final FileRepository fileRepository;
    private final ServiceRepository serviceRepository;
    private final AccountRepository accountRepository;
    private final AddressRepository addressRepository;
    private final BuildingRepository buildingRepository;
    private final CalculationRepo calculationsRepository;
    private final UserRepository userRepository;

    public CalculationProcessorService(FileRepository fileRepository, ServiceRepository serviceRepository,
                                       AccountRepository accountRepository, AddressRepository addressRepository,
                                       BuildingRepository buildingRepository, CalculationRepo calculationsRepository,
                                       UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.serviceRepository = serviceRepository;
        this.accountRepository = accountRepository;
        this.addressRepository = addressRepository;
        this.buildingRepository = buildingRepository;
        this.calculationsRepository = calculationsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void processCalculations(User owner, UploadData combinedUploadData, List<UploadData> calculationDataList, Long[] defaultServiceIds) throws ExecutionException, InterruptedException,
            TimeoutException, FileContainsCorruptedLines, FileNotFoundException {
        if (calculationDataList == null || calculationDataList.isEmpty())
            throw new FileNotFoundException("Не может быть нолем");

        if (calculationDataList.size() != defaultServiceIds.length)
            throw new FileNotFoundException("Сервис должен быть определен для кажгого файла");

        List<com.example.tsgpaymentsystem.domain.Service> persistedServices = serviceRepository.findAllByUser(owner); //получаем сервисы у юзера

        Map<Long, com.example.tsgpaymentsystem.domain.Service> servicesById = persistedServices // мапа для сбора сервисов
                .stream()
                .collect(Collectors.toMap(com.example.tsgpaymentsystem.domain.Service::getId, v -> v));

        List<Future<CalculationDocumentProcessor.CalculationDocumentResult>> itemFutures = new ArrayList<>(); //создается многототочный метод с рассчётом
        // который создает документ

        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i = 0; i < defaultServiceIds.length; i++) {
            Long defaultServiceId = defaultServiceIds[i];
            com.example.tsgpaymentsystem.domain.Service defaultService = servicesById.get(defaultServiceId);
            itemFutures.add(executor.submit(new CalculationDocumentProcessor(calculationDataList.get(i).getContent(), defaultService.getService())));
            //подписывается на возврат результата
            log.debug("процесс рассчёт начался для объединенного файла id={}, file={}, name={}, service={} owner-id={}",
                    combinedUploadData.getId(), i,
                    calculationDataList.get(i).getName(), defaultService.getService(), combinedUploadData.getOwner().getId());
        }
        executor.shutdown();

        combinedUploadData.setState(ProcessState.IN_PROGRESS);
        combinedUploadData.setStatus("No errors");
        fileRepository.save(combinedUploadData);

        executor.awaitTermination(3, TimeUnit.MINUTES); //блокируется до выполнения всех задач

        // Файл обработан, пришло время сохранить данные
        // CalculationDocumentProcessor.CalculationDocumentResult calculationDoc = calculationItemFuture.get(5, TimeUnit.MINUTES);

        List<CalculationDocumentProcessor.CalculationDocumentResult> calculationDocs = new ArrayList<>(); //список данных обработанного файла

        for (Future<CalculationDocumentProcessor.CalculationDocumentResult> future : itemFutures) { //получается список по ожидаемому результату задачи
            // обходя цикл ожидаемого документа
            CalculationDocumentProcessor.CalculationDocumentResult calculationDoc = future.get();
            calculationDocs.add(calculationDoc);

            if (!calculationDoc.getErrors().isEmpty() || calculationDoc.getLines().isEmpty()) {
                combinedUploadData.setState(ProcessState.FAIL);
                combinedUploadData.setStatus(String.join("\n", calculationDoc.getErrors()));
                if (calculationDoc.getLines().isEmpty())
                    combinedUploadData.setStatus("File is empty");

                fileRepository.save(combinedUploadData); //потом сохраняется
                throw new FileContainsCorruptedLines(calculationDoc.getErrors());
            }
        }

        // ТСЖ #41 Очистить все начисления в этот день
        ZonedDateTime today = DateUtils.todayStart();
        cleanTodayUploadDataIfAny(owner, today);

        // Все его сервисы
        Map<String, com.example.tsgpaymentsystem.domain.Service> services = persistedServices
                .stream()
                .collect(Collectors.toMap(com.example.tsgpaymentsystem.domain.Service::getService, v -> v));

        // Все здания
        Map<String, Building> buildings = buildingRepository
                .findAllByUser(owner)
                .stream()
                .collect(Collectors.toMap(Building::getBuilding, v -> v));

        // Все квартиры
        Map<String, Address> apartments = addressRepository
                .findAllByUser(owner)
                .stream()
                .collect(Collectors.toMap(k -> toKey(k.getApartment(), k.getBuilding().getBuilding()), v -> v));

        // Все аккаунты.
        Map<String, Account> accounts = apartments.values().stream()
                .collect(Collectors.toMap(k -> toKey(k.getAccount().getAccount(), k.getApartment(), k.getBuilding().getBuilding()),
                        Address::getAccount));

        Set<String> servicesFromFile = new HashSet<>(); //множество сервисов в файле
        Set<AddressRecord> addressesFromFile = new HashSet<>(); //множество адресов в файле
        Set<CalculationDocumentProcessor.AccountRecord> accountsFromFile = new HashSet<>(); //множетсво аккаунтов в файле
        List<AbstractMap.SimpleEntry<String, CalculationDocumentProcessor.CalculationRecord>> linesFromFile = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (CalculationDocumentProcessor.CalculationDocumentResult calculationDoc : calculationDocs) { //проход по списку документа
            servicesFromFile.addAll(calculationDoc.getServices());
            addressesFromFile.addAll(calculationDoc.getAddresses());
            accountsFromFile.addAll(calculationDoc.getAccounts());
            linesFromFile.addAll(calculationDoc.getLines().stream().map(value -> new AbstractMap.SimpleEntry<>
                    (calculationDoc.getDefaultService(), value)).collect(Collectors.toList()));
            errors.addAll(calculationDoc.getErrors());
        }

        // Сохраним новые сервисы если есть
        saveNewServicesIfExists(owner, services, servicesFromFile);

        // Сохраним новые здания если есть
        saveNewBuildingsIfExists(owner, buildings, addressesFromFile);

        // First file uploading may be slow due to creating of many accounts but consecutive files will be much faster
        saveNewAccountsIfExists(owner, buildings, apartments, accounts, accountsFromFile);

        // Update status
        List<Calculation> calculations = new ArrayList<>(linesFromFile.size());
        Set<Account> activeAccounts = new HashSet<>();
        for (AbstractMap.SimpleEntry<String, CalculationDocumentProcessor.CalculationRecord> line : linesFromFile) {
            Calculation calculation = createCalculation(apartments, services, line.getValue(), line.getKey());
            calculations.add(calculation);
            activeAccounts.add(calculation.getAccount());
        }

        // проход по списку последней калькуляции
        for (Calculation calculation : calculations) {
            calculation.setLastUpload(combinedUploadData.getId());
            calculation.setLastUploadDate(combinedUploadData.getTimestamp());
        }

        // проход по списку аккаунтов
        for (Account account : activeAccounts)
            account.setLastUpload(combinedUploadData.getId());

        calculationsRepository.saveAll(calculations);
        accountRepository.saveAll(activeAccounts);

        combinedUploadData.setState(ProcessState.OK);
        combinedUploadData.setStatus(String.join("\n", errors));
        fileRepository.save(combinedUploadData);

        // make the user use the latest uploadData
        owner.setLastUpload(combinedUploadData.getId());
        owner.setLastUploadDate(combinedUploadData.getTimestamp());
        userRepository.save(owner);

        log.debug("процесс калькуляции готов файл id={}, name={}, юзер-id={}",
                combinedUploadData.getId(), combinedUploadData.getName(), combinedUploadData.getOwner().getId());
    }

    private String toKey(String... args) {
        return String.join("_", args);
    }

    private void cleanTodayUploadDataIfAny(User owner, ZonedDateTime today) {
        if (today.isEqual(owner.getLastUploadDate())) {
            log.debug(">> Удалилось все сегодня {} ", today);
            calculationsRepository.deleteByLastUpload(owner.getLastUpload());
            // удалить все пустые адреса
        }
    }

    private void saveNewBuildingsIfExists(User owner, Map<String, Building> buildings, Set<AddressRecord> addresses) {
        for (AddressRecord addressRecord : addresses) { //из уникальных записей адресов получаем циклом здания по адресу
            // в ключ карты ложим адрес здания из списка адресов, в значение параметры пользователя и здания
            String buildingRecord = addressRecord.getBuilding();
            Building building = buildings.get(buildingRecord);
            if (building == null) {
                building = new Building();
                building.setBuilding(buildingRecord);
                building.setUser(owner);
                building = buildingRepository.save(building);
                buildings.put(buildingRecord, building);
            }
        }
    }

    private void saveNewAccountsIfExists(User owner,
                                         Map<String, Building> buildings,
                                         Map<String, Address> apartments,
                                         Map<String, Account> accounts,
                                         Set<CalculationDocumentProcessor.AccountRecord> accountsFromFile) {
        for (CalculationDocumentProcessor.AccountRecord ac : accountsFromFile) {

            String accountKey = toKey(ac.getAccount(), ac.getAddress().getApartment(), ac.getAddress().getBuilding());//получаем данные ключа аккаунта из записи
            // аккаунта в методе CalculationDocumentProcessor путем обхода множества записей аккаунтов
            String apartmentKey = toKey(ac.getAddress().getApartment(), ac.getAddress().getBuilding()); //получаем данные ключа апартаменов из записи аккаунта

            // в методе CalculationDocumentProcessor путем обхода множества записей аккаунтов
            // apartment
            Address apartment = apartments.get(apartmentKey); //адрес апартаментов получается как значение по ключу апартаментов
            if (apartment == null) { //если нет, то создается аккаунт

                // аккаунт проверяется по ключу мапы аккаунтов
                Account account = accounts.get(accountKey);
                if (account == null) { //если нет, туда создантся новый акк с юзером и сохраняется в репо, а мапа получает ключ аккаут кей и значение нового акка
                    account = new Account();
                    account.setUser(owner);
                    account.setAccount(ac.getAccount());
                    account = accountRepository.save(account);
                    accounts.put(accountKey, account);
                }

                apartment = new Address(); //создается новый адрес, куда вписываются остальные данные
                apartment.setBuilding(buildings.get(ac.getAddress().getBuilding()));
                apartment.setApartment(ac.getAddress().getApartment());
                apartment.setUser(owner);
                apartment.setAccount(account); // Присоединяется аккаунт к номеру квартиры и сейвится в репо
                apartment = addressRepository.save(apartment);
                apartments.put(apartmentKey, apartment); //мапа получает ключ из строки и сохраняет значение по нему нового адреса
            }
        }
    }

    private Calculation createCalculation(Map<String, Address> addresses,
                                          Map<String, com.example.tsgpaymentsystem.domain.Service> services,
                                          CalculationDocumentProcessor.CalculationRecord line, String defaultService) {

        Address address = addresses.get(toKey(line.getAccount().getAddress().getApartment(), line.getAccount().getAddress().getBuilding())); //получаем значение адреса из мапы по ключу
        Account account = address.getAccount(); //сопоставляем аккаунт с адресом

        //в обьект калькуляции ложаться полученные данные

        Calculation calculation = new Calculation();
        calculation.setAccount(account);
        calculation.setAddress(address);
        calculation.setService(services.get(line.getService()));
        calculation.setGroup(services.get(defaultService));
        calculation.setDebt(line.getDebt());
        calculation.setOutstandingDebt(calculation.getDebt());
        calculation.setUser(address.getUser());
        return calculation;
    }

    private void saveNewServicesIfExists(User owner, Map<String, com.example.tsgpaymentsystem.domain.Service> services, Set<String> servicesFromFile) {
        for (String service : servicesFromFile) {
            if (!services.containsKey(service)) { //если ключ карты не сождержит назваиние
                // сервиса из строк множества то создается новый и сохраняется в репозиторий,
                // перед этим проверяется по наличию ключа
                com.example.tsgpaymentsystem.domain.Service newService = new com.example.tsgpaymentsystem.domain.Service();
                newService.setService(service);
                newService.setUser(owner);
                newService = serviceRepository.save(newService);
                services.put(service, newService);
            }
        }
    }
}
