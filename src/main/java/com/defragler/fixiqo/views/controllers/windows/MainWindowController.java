package com.defragler.fixiqo.views.controllers.windows;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.contexts.*;
import com.defragler.fixiqo.entities.contexts.client.*;
import com.defragler.fixiqo.entities.contexts.employee.*;
import com.defragler.fixiqo.entities.contexts.request.*;
import com.defragler.fixiqo.entities.enums.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.utilities.*;
import com.defragler.fixiqo.views.controllers.*;

import java.util.function.*;
import javafx.animation.*;
import javafx.beans.binding.*;
import javafx.fxml.*;
import javafx.application.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.shape.*;
import javafx.util.*;

public class MainWindowController extends ControllerBase {

    @FXML
    private StackPane root;
    @FXML
    private StackPane contentPane;

    @FXML
    private ImageView avatarImage;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label roleLabel;

    @FXML
    private VBox navContainer;
    @FXML
    private Pane pointer;

    @FXML
    private ToggleButton homeButton;
    @FXML
    private ToggleButton clientsButton;
    @FXML
    private ToggleButton requestsButton;
    @FXML
    private ToggleButton employeesButton;
    @FXML
    private ToggleButton browserButton;
    @FXML
    private ToggleButton storageButton;
    @FXML
    private ToggleButton settingsButton;
    @FXML
    private ToggleButton informationButton;

    private IModalService modalService;
    private INavigationService navigationService;

    private ToggleGroup navGroup;
    
    @Override
    protected void onInit() {
        if (root == null) {
            throw new ControllerException(ExceptionLevel.WARNING,
                  "root was not injected from FXML");
        } else if (contentPane == null) {
            throw new ControllerException(ExceptionLevel.WARNING,
                  "contentPane was not injected from FXML");
        }

        var user = context.getCurrentUser();
        if (user != null) {
            var role = UserRoleEnum.fromId(user.getRole());
            var imageService = context.get(IImageService.class);

            byte[] avatar = user.getAvatar();

            if (avatar == null) {
                avatar = imageService.getDefaultAvatar();
            }

            if (usernameLabel != null) {
                usernameLabel.setText(user.getUsername());
            }
            if (roleLabel != null) {
                roleLabel.setText(role.getName());
            }

            avatarImage.setImage(ImageConverter.fromBytes(avatar));
            configureAccess(role);
        }

        navGroup = new ToggleGroup();

        homeButton.setToggleGroup(navGroup);
        clientsButton.setToggleGroup(navGroup);
        requestsButton.setToggleGroup(navGroup);
        employeesButton.setToggleGroup(navGroup);
        browserButton.setToggleGroup(navGroup);
        storageButton.setToggleGroup(navGroup);
        settingsButton.setToggleGroup(navGroup);
        informationButton.setToggleGroup(navGroup);

        homeButton.setSelected(true);

        navGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                movePointer((ToggleButton) selected);
            }
        });

        modalService = new ModalService(root, new ControllerFactory(context));
        navigationService = new NavigationService(contentPane, new ControllerFactory(context));
        
        setupAvatarClip();
        
        Platform.runLater(() -> {
            homeButton.setDisable(true);
            openHomePage();
        });
    }

    @FXML
    private void openAccountModal() {
        String modal = "/fxml/modals/account-modal-window.fxml";

        Runnable closeAll = modalService::closeAll;

        Consumer<User> onSave = updatedUser -> {
            context.setCurrentUser(updatedUser);

            if (usernameLabel != null) {
                usernameLabel.setText(updatedUser.getUsername());
            }

            byte[] avatar = updatedUser.getAvatar();
            if (avatar == null) {
                avatar = context.get(IImageService.class).getDefaultAvatar();
            }

            avatarImage.setImage(ImageConverter.fromBytes(avatar));
        };

        var currentUser = context.getCurrentUser();

        AccountContext modalContext =
              new AccountContext(
                    currentUser,
                    onSave,
                    closeAll
              );

        modalService.show(modal, modalContext);
    }

    @FXML
    public void openHomePage() {
        select(homeButton);
        navigationService.navigateTo("/fxml/pages/home-page.fxml", null);
    }

    @FXML
    private void openClientsPage() {
        select(clientsButton);
        String modal = "/fxml/modals/client-modal-window.fxml";

        Runnable reload = navigationService::reloadCurrentPage;
        Runnable closeAll = modalService::closeAll;

        Consumer<Client> onSave = client -> reload.run();

        Supplier<ClientModalContext> addContext =
              () -> new ClientModalContext(null, false, onSave, closeAll);
        Function<Client, ClientModalContext> editContext =
              client -> new ClientModalContext(client, true, onSave, closeAll);

        navigationService.navigateTo(
              "/fxml/pages/clients-page.fxml",
              new ClientsPageContext(
                    // ADD
                    () -> modalService.show(modal, addContext.get()),

                    // EDIT
                    client -> modalService.show(modal, editContext.apply(client)),

                    // DELETE
                    client -> context.get(IClientsService.class)
                          .deleteClientCascade(client.getId())
              )
        );
    }

    @FXML
    private void openRequestsPage() {
        select(requestsButton);
        String modal = "/fxml/modals/request-modal-window.fxml";

        Runnable reload = navigationService::reloadCurrentPage;
        Runnable closeAll = modalService::closeAll;

        Consumer<Request> onSave = request -> reload.run();

        Supplier<RequestModalContext> addContext =
              () -> new RequestModalContext(null, false, onSave, closeAll);

        Function<Request, RequestModalContext> editContext =
              request -> new RequestModalContext(request, true, onSave, closeAll);

        navigationService.navigateTo(
              "/fxml/pages/requests-page.fxml",
              new RequestsPageContext(
                    // ADD
                    () -> modalService.show(modal, addContext.get()),

                    // EDIT
                    request -> modalService.show(modal, editContext.apply(request)),

                    // DELETE
                    request -> context.get(IRequestsService.class)
                          .delete(request.getId())
              )
        );
    }

    @FXML
    private void openEmployeesPage() {
        select(employeesButton);
        String modal = "/fxml/modals/employee-modal-window.fxml";

        Runnable reload = navigationService::reloadCurrentPage;
        Runnable closeAll = modalService::closeAll;

        Consumer<User> onSave = user -> reload.run();

        Supplier<EmployeeModalContext> addContext =
              () -> new EmployeeModalContext(null, false, onSave, closeAll);
        Function<User, EmployeeModalContext> editContext =
              user -> new EmployeeModalContext(user, true, onSave, closeAll);

        navigationService.navigateTo(
              "/fxml/pages/employees-page.fxml",
              new EmployeesPageContext(
                    // ADD
                    () -> modalService.show(modal, addContext.get()),

                    // EDIT
                    user -> modalService.show(modal, editContext.apply(user)),

                    // DELETE
                    user -> context.get(IUserService.class).deactivate(user.getId())
              )
        );
    }

    @FXML
    private void openBrowserPage() {
        select(browserButton);
        navigationService.navigateTo("/fxml/pages/browser-page.fxml", null);
    }

    @FXML
    private void openStoragePage() {
        select(storageButton);
        navigationService.navigateTo("/fxml/pages/storage-page.fxml", null);
    }

    @FXML
    private void openSettingsPage() {
        select(settingsButton);
        navigationService.navigateTo("/fxml/pages/settings-page.fxml", null);
    }

    @FXML
    private void openInformationPage() {
        select(informationButton);
        navigationService.navigateTo("/fxml/pages/information-page.fxml", null);
    }

    @FXML
    private void goBack() {
        navigationService.goBack();
    }

    @FXML
    private void goForward() {
        navigationService.goForward();
    }

    private void configureAccess(UserRoleEnum role) {
        switch (role) {
            case ADMINISTRATOR -> {
                setAccess(homeButton, true);
                setAccess(clientsButton, true);
                setAccess(requestsButton, true);
                setAccess(employeesButton, true);
                setAccess(browserButton, true);
                setAccess(storageButton, true);
                setAccess(settingsButton, true);
                setAccess(informationButton, true);
            }

            case MANAGER -> {
                setAccess(homeButton, true);
                setAccess(clientsButton, true);
                setAccess(requestsButton, true);
                setAccess(employeesButton, false);
                setAccess(browserButton, true);
                setAccess(storageButton, false);
                setAccess(settingsButton, true);
                setAccess(informationButton, true);
            }

            case EMPLOYEE -> {
                setAccess(homeButton, true);
                setAccess(clientsButton, false);
                setAccess(requestsButton, true);
                setAccess(employeesButton, false);
                setAccess(browserButton, false);
                setAccess(storageButton, false);
                setAccess(settingsButton, true);
                setAccess(informationButton, true);
            }
        }
    }

    private void setAccess(ToggleButton button, boolean allowed) {
        button.setVisible(allowed);
        button.setManaged(allowed);
    }

    private void movePointer(ToggleButton button) {
        double targetY = button.getBoundsInParent().getMinY() + 10;

        TranslateTransition transition = new TranslateTransition(Duration.millis(200), pointer);
        transition.setToY(targetY);
        transition.setInterpolator(Interpolator.EASE_BOTH);

        transition.play();
    }

    private void select(ToggleButton button) {
        navGroup.selectedToggleProperty().addListener((obs, old, selected) -> {

            if (old != null) {
                ((ToggleButton) old).setDisable(false);
            }

            if (selected != null) {
                ToggleButton btn = (ToggleButton) selected;

                btn.setDisable(true);
                movePointer(btn);
            }
        });
        
        if (button.isSelected()) return;
        button.setSelected(true);
    }

    private void setupAvatarClip() {
        Circle clip = new Circle();

        clip.centerXProperty().bind(avatarImage.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(avatarImage.fitHeightProperty().divide(2));

        clip.radiusProperty().bind(
              Bindings.createDoubleBinding(
                    () -> Math.min(
                          avatarImage.getFitWidth(),
                          avatarImage.getFitHeight()
                    ) / 2.0,
                    avatarImage.fitWidthProperty(),
                    avatarImage.fitHeightProperty()
              )
        );

        avatarImage.setClip(clip);
    }
}
