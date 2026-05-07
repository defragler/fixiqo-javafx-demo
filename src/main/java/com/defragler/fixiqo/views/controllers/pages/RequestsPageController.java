package com.defragler.fixiqo.views.controllers.pages;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.contexts.request.*;
import com.defragler.fixiqo.entities.enums.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.views.controllers.*;

import java.time.*;
import java.time.format.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.fxml.*;
import javafx.scene.control.*;

public class RequestsPageController extends ControllerBase implements IRoutable<RequestsPageContext> {
    @FXML
    private TextField searchField;

    @FXML
    private TabPane statusTabs;
    @FXML
    private Tab tabAll;
    @FXML
    private Tab tabAccepted;
    @FXML
    private Tab tabWaiting;
    @FXML
    private Tab tabInProgress;
    @FXML
    private Tab tabCompleted;
    @FXML
    private Tab tabIssued;
    @FXML
    private Tab tabReturned;

    @FXML
    private TableView<Request> requestsTable;
    @FXML
    private TableColumn<Request, Long> requestsIdColumn;
    @FXML
    private TableColumn<Request, String> requestsClientColumn;
    @FXML
    private TableColumn<Request, String> requestsBrandColumn;
    @FXML
    private TableColumn<Request, String> requestsModelColumn;
    @FXML
    private TableColumn<Request, String> requestsStatusColumn;
    @FXML
    private TableColumn<Request, String> requestsReceivedColumn;
    @FXML
    private TableColumn<Request, String> requestsIssuedColumn;

    private IRequestsService requestsService;
    private IClientsService clientsService;
    private IDeviceService deviceService;

    private RequestsPageContext contextActions;

    private ObservableList<Request> masterData;
    private FilteredList<Request> filteredData;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    protected void onInit() {
        requestsService = context.get(IRequestsService.class);
        clientsService = context.get(IClientsService.class);
        deviceService = context.get(IDeviceService.class);

        initTable();
        loadData();
        setupFiltering();
    }

    @Override
    public void onNavigate(RequestsPageContext parameter) {
        this.contextActions = parameter;
    }

    @FXML
    private void add() {
        if (contextActions != null) {
            contextActions.getOnAdd().run();
        }
    }

    @FXML
    private void edit() {
        Request selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null && contextActions != null) {
            contextActions.getOnEdit().accept(selected);
        }
    }

    @FXML
    private void delete() {
        Request selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null && contextActions != null) {
            contextActions.getOnDelete().accept(selected);
            loadData();
        }
    }

    @FXML
    private void search() {
        applyFilters();
    }

    private void initTable() {
        requestsIdColumn.setCellValueFactory(data ->
              new SimpleObjectProperty<>(data.getValue().getId())
        );

        requestsClientColumn.setCellValueFactory(data -> {
            return new SimpleStringProperty(
                  clientsService.getById(data.getValue().getClientId())
                        .map(Client::getFullName)
                        .orElse("Unknown")
            );
        });

        requestsBrandColumn.setCellValueFactory(data -> {
            return new SimpleStringProperty(
                  deviceService.getById(data.getValue().getDeviceId())
                        .map(Device::getBrand)
                        .orElse("Unknown")
            );
        });

        requestsModelColumn.setCellValueFactory(data -> {
            return new SimpleStringProperty(
                  deviceService.getById(data.getValue().getDeviceId())
                        .map(Device::getModel)
                        .orElse("Unknown")
            );
        });

        requestsStatusColumn.setCellValueFactory(data -> {
            String status = RequestStatusEnum
                  .fromId((int) data.getValue().getStatusId())
                  .toString();

            return new SimpleStringProperty(status);
        });

        requestsReceivedColumn.setCellValueFactory(data -> {
            return new SimpleStringProperty(
                  formatDate(data.getValue().getDateReceived())
            );
        });

        requestsIssuedColumn.setCellValueFactory(data -> {
            Long issued = data.getValue().getDateIssued();

            return new SimpleStringProperty(
                  issued != null ? formatDate(issued) : "-"
            );
        });
    }

    private void loadData() {
        masterData = FXCollections.observableArrayList(requestsService.getAll());
        filteredData = new FilteredList<>(masterData, p -> true);
        requestsTable.setItems(filteredData);

        Platform.runLater(() -> requestsTable.requestFocus());
    }
    
    private void setupFiltering() {

        statusTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            applyFilters();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    private void applyFilters() {

        String search = searchField.getText();
        Tab selectedTab = statusTabs.getSelectionModel().getSelectedItem();

        filteredData.setPredicate(request -> {

            // --- STATUS FILTER ---
            boolean matchesStatus = switch (selectedTab.getId()) {

                case "tabAccepted" ->
                      request.getStatusId() == RequestStatusEnum.ACCEPTED.getId();

                case "tabWaiting" ->
                      request.getStatusId() == RequestStatusEnum.WAITING_PARTS.getId();

                case "tabInProgress" ->
                      request.getStatusId() == RequestStatusEnum.IN_PROGRESS.getId();

                case "tabCompleted" ->
                      request.getStatusId() == RequestStatusEnum.COMPLETED.getId();

                case "tabIssued" ->
                      request.getStatusId() == RequestStatusEnum.ISSUED.getId();

                case "tabReturned" ->
                      request.getStatusId() == RequestStatusEnum.RETURNED.getId();

                default -> true;
            };

            // --- SEARCH FILTER ---
            boolean matchesSearch = true;

            if (search != null && !search.isBlank()) {
                String lower = search.toLowerCase();

                String client = clientsService.getById(request.getClientId())
                      .map(Client::getFullName)
                      .orElse("")
                      .toLowerCase();

                String brand = deviceService.getById(request.getDeviceId())
                      .map(Device::getBrand)
                      .orElse("")
                      .toLowerCase();

                String model = deviceService.getById(request.getDeviceId())
                      .map(Device::getModel)
                      .orElse("")
                      .toLowerCase();

                matchesSearch =
                      client.contains(lower) ||
                            brand.contains(lower) ||
                            model.contains(lower);
            }

            return matchesStatus && matchesSearch;
        });
    }

    // ---------------- HELPERS ----------------

    private String formatDate(long epoch) {
        return Instant.ofEpochSecond(epoch)
              .atZone(ZoneId.systemDefault())
              .toLocalDateTime()
              .format(formatter);
    }
}
