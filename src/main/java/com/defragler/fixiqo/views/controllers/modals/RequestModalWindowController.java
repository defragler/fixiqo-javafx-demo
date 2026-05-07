package com.defragler.fixiqo.views.controllers.modals;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.contexts.request.*;
import com.defragler.fixiqo.entities.enums.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.views.controllers.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;

public class RequestModalWindowController extends ControllerBase implements IModal {
    @FXML
    private DatePicker detailsDateField;
    @FXML
    private TextField detailsIdField;

    @FXML
    private ComboBox<Client> clientNameField;
    @FXML
    private TextField clientNumberField;

    @FXML
    private ComboBox<DeviceTypeEnum> deviceTypeField;
    @FXML
    private ComboBox<String> deviceBrandField;
    @FXML
    private TextField deviceModelField;
    @FXML
    private TextField deviceSerialField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TableView<RequestStatusHistory> statusTable;
    @FXML
    private TableColumn<RequestStatusHistory, Long> idColumn;
    @FXML
    private TableColumn<RequestStatusHistory, Long> statusColumn;

    @FXML
    private TableView<Option> optionsTable;
    @FXML
    private TableColumn<Option, String> optionsNameColumn;
    @FXML
    private TableColumn<Option, Double> optionsPriceColumn;

    @FXML
    private TableView<Part> materialsTable;
    @FXML
    private TableColumn<Part, String> materialsNameColumn;
    @FXML
    private TableColumn<Part, Double> materialsPriceColumn;

    private IRequestsService requestsService;
    private IClientsService clientsService;
    private IDeviceService deviceService;

    private RequestModalContext contextData;

    private List<Client> clients;
    private List<Device> devices;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    protected void onInit() {
        requestsService = context.get(IRequestsService.class);
        clientsService = context.get(IClientsService.class);
        deviceService = context.get(IDeviceService.class);

        clients = clientsService.getAll();
        devices = deviceService.getAll();

        loadDeviceTypes();
        clientAutocomplete();
        deviceAutocomplete();
        initTables();
    }

    @Override
    public void onOpen(Object parameter) {
        contextData = (RequestModalContext) parameter;

        if (contextData.isEditMode()) {
            loadData(contextData.getRequest());
        } else {
            detailsDateField.setValue(LocalDate.now());
        }
    }

    private void initTables() {
        idColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()).asObject());
        statusColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getStatusId()).asObject());

//        optionsNameColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
//        optionsPriceColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()).asObject());
//
//        materialsNameColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
//        materialsPriceColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()).asObject());
    }

    private void loadData(Request r) {
        detailsDateField.setValue(
              Instant.ofEpochSecond(r.getDateReceived())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
        );
        detailsIdField.setText(String.valueOf(r.getId()));
        
        descriptionField.setText(r.getDescription());

        clientsService.getById(r.getClientId()).ifPresent(c -> {
            clientNameField.getEditor().setText(c.getFullName());
            clientNumberField.setText(c.getPhoneNumber());
        });

        deviceService.getById(r.getDeviceId()).ifPresent(d -> {
            deviceTypeField.getItems().setAll(DeviceTypeEnum.values());
            deviceTypeField.setValue(DeviceTypeEnum.fromId((int) d.getDeviceTypeId()));
            deviceBrandField.getEditor().setText(d.getBrand());
            deviceModelField.setText(d.getModel());
            deviceSerialField.setText(d.getImeiOrSdn());
        });
    }
    
    private void clientAutocomplete() {
        clientNameField.setEditable(true);

        ObservableList<Client> data = FXCollections.observableArrayList(clients);
        clientNameField.setItems(data);

        clientNameField.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Client client) {
                return client == null ? "" : client.getFullName();
            }

            @Override
            public Client fromString(String string) {
                return data.stream()
                      .filter(c -> c.getFullName().equalsIgnoreCase(string))
                      .findFirst()
                      .orElse(null);
            }
        });

        clientNameField.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Client item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });

        clientNameField.getEditor().textProperty().addListener((obs, old, val) -> {
            if (contextData.isEditMode()) return;

            if (val == null || val.isBlank()) {
                clientNameField.setItems(data);
                return;
            }

            List<Client> filtered = data.stream()
                  .filter(c -> c.getFullName().toLowerCase().contains(val.toLowerCase()))
                  .toList();

            clientNameField.setItems(FXCollections.observableArrayList(filtered));
            clientNameField.show();
        });

        clientNameField.setOnAction(e -> {
            Client selected = clientNameField.getValue();
            if (selected != null) {
                clientNumberField.setText(selected.getPhoneNumber());
            }
        });
    }

    private void deviceAutocomplete() {
        deviceBrandField.setEditable(true);

        List<String> brands = devices.stream()
              .map(Device::getBrand)
              .filter(Objects::nonNull)
              .distinct()
              .toList();

        ObservableList<String> data = FXCollections.observableArrayList(brands);
        deviceBrandField.setItems(data);

        deviceBrandField.getEditor().textProperty().addListener((obs, old, val) -> {
            if (contextData.isEditMode()) return;

            if (val == null || val.isBlank()) {
                deviceBrandField.setItems(data);
                return;
            }

            List<String> filtered = data.stream()
                  .filter(b -> b.toLowerCase().contains(val.toLowerCase()))
                  .toList();

            deviceBrandField.setItems(FXCollections.observableArrayList(filtered));
            deviceBrandField.show();
        });

        deviceBrandField.setOnAction(e -> {
            String selected = deviceBrandField.getValue();
            if (selected != null) {
                deviceBrandField.getEditor().setText(selected);
            }
        });
    }

    private void loadDeviceTypes() {
        deviceTypeField.getItems().setAll(DeviceTypeEnum.values());

        deviceTypeField.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(DeviceTypeEnum item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getType());
            }
        });

        deviceTypeField.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(DeviceTypeEnum item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getType());
            }
        });
    }
    
    @FXML
    private void save() {
        try {
            validate();

            String fullName = clientNameField.getEditor().getText();
            String phone = clientNumberField.getText();

            DeviceTypeEnum type = deviceTypeField.getValue();
            String brand = deviceBrandField.getEditor().getText();
            String model = deviceModelField.getText();
            String serial = deviceSerialField.getText();

            String description = descriptionField.getText();
            long dateReceived = detailsDateField.getValue()
                  .atStartOfDay(ZoneId.systemDefault())
                  .toEpochSecond();

            if (contextData.isEditMode()) {

                Request result = contextData.getRequest();
                result.setDescription(description);
                result.setDateReceived(
                      detailsDateField.getValue()
                            .atStartOfDay(ZoneId.systemDefault())
                            .toEpochSecond()
                );

                requestsService.update(result);

                if (contextData.getOnSave() != null) {
                    contextData.getOnSave().accept(result);
                }

            } else {
                requestsService.createRequestWithClient(
                      fullName,
                      phone,
                      type.getId(),
                      brand,
                      model,
                      serial,
                      description,
                      dateReceived
                );

                if (contextData.getOnSave() != null) {
                    contextData.getOnSave().accept(null);
                }
            }

            cancel();

        } catch (ServiceException ex) {
            showError(ex.getUiMessage());
        } catch (Exception ex) {
            showError("Unexpected error: " + ex.getMessage());
        }
    }

    @FXML
    private void cancel() {
        if (contextData != null && contextData.getOnClose() != null) {
            contextData.getOnClose().run();
        }
    }
    
    // --- HELPERS ---
    private void validate() {
        if (clientNameField.getEditor().getText().isBlank()) {
            throw new ServiceException(ExceptionLevel.WARNING,"Client name is required");
        }

        if (clientNumberField.getText().isBlank()) {
            throw new ServiceException(ExceptionLevel.WARNING,"Phone number is required");
        }

        if (deviceTypeField.getValue() == null) {
            throw new ServiceException(ExceptionLevel.WARNING,"Device type is required");
        }

        if (deviceBrandField.getEditor().getText().isBlank()) {
            throw new ServiceException(ExceptionLevel.WARNING,"Brand is required");
        }

        if (deviceModelField.getText().isBlank()) {
            throw new ServiceException(ExceptionLevel.WARNING,"Model is required");
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Validation error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}