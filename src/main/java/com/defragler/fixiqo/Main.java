package com.defragler.fixiqo;

import com.defragler.fixiqo.repositories.sqlite.accessories.*;
import com.defragler.fixiqo.repositories.sqlite.client.*;
import com.defragler.fixiqo.repositories.sqlite.device.*;
import com.defragler.fixiqo.repositories.sqlite.option.*;
import com.defragler.fixiqo.repositories.sqlite.part.*;
import com.defragler.fixiqo.repositories.sqlite.request.*;
import com.defragler.fixiqo.repositories.sqlite.user.*;
import com.defragler.fixiqo.services.*;
import com.defragler.fixiqo.services.interfaces.*;
import com.defragler.fixiqo.utilities.*;
import com.defragler.fixiqo.views.controllers.*;
import com.defragler.fixiqo.views.windows.*;

import atlantafx.base.theme.*;
import com.jthemedetecor.*;

import java.io.*;
import java.sql.*;
import javafx.application.*;
import javafx.stage.*;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private ApplicationContext context;
    
    // Dependency Injection (DI)
    @Override
    public void init () {
        context = new ApplicationContext();

        // Repositories
        context.registerSingleton(IUserRepository.class, () -> new UserRepository(context.get(IDatabaseService.class)));
        context.registerSingleton(IUserRoleRepository.class, () -> new UserRoleRepository(context.get(IDatabaseService.class)));
        
        context.registerSingleton(IClientRepository.class, () -> new ClientRepository(context.get(IDatabaseService.class)));
        
        context.registerSingleton(IDeviceRepository.class, () -> new DeviceRepository(context.get(IDatabaseService.class)));
        context.registerSingleton(IDeviceTypeRepository.class, () -> new DeviceTypeRepository(context.get(IDatabaseService.class)));
        
        context.registerSingleton(IRequestRepository.class, () -> new RequestRepository(context.get(IDatabaseService.class)));
        context.registerSingleton(IRequestStatusRepository.class, () -> new RequestStatusRepository(context.get(IDatabaseService.class)));
        context.registerSingleton(IRequestStatusHistoryRepository.class, () -> new RequestStatusHistoryRepository(context.get(IDatabaseService.class)));
        
        context.registerSingleton(IAccessoriesRepository.class, () -> new AccessoriesRepository(context.get(IDatabaseService.class)));
        context.registerSingleton(IAccessoriesTypeRepository.class, () -> new AccessoriesTypeRepository(context.get(IDatabaseService.class)));
        
        context.registerSingleton(IPartRepository.class, () -> new PartRepository(context.get(IDatabaseService.class)));
        context.registerSingleton(IOptionRepository.class, () -> new OptionRepository(context.get(IDatabaseService.class)));

        // Basic Services
        context.registerSingleton(IDatabaseService.class, () -> new DatabaseService(AppPaths.dbPath().toString()));
        context.registerSingleton(Initializer.class, () -> new InitializerService(context.get(IDatabaseService.class)));
        context.registerSingleton(ISessionService.class, () -> new SessionService(context, context.get(IUserService.class)));
        context.registerSingleton(ICommunicationService.class, () -> context.get(CommunicationService.class));
        context.registerSingleton(ILocalizationService.class, () -> context.get(LocalizationService.class));
        context.registerSingleton(IEncryptionService.class, () -> context.get(EncryptionService.class));
        context.registerSingleton(IValidationService.class, () -> context.get(ValidationService.class));
        context.registerSingleton(INavigationService.class, () -> context.get(NavigationService.class));
        context.registerSingleton(IReportService.class, () -> context.get(ReportService.class));
        context.registerSingleton(IModalService.class, () -> context.get(ModalService.class));
        context.registerSingleton(IThemeService.class, () -> context.get(ThemeService.class));
        context.registerSingleton(IImageService.class, () -> context.get(ImageService.class));

        // Security Services 
        context.registerSingleton(IAuthenticationService.class, () -> context.get(AuthenticationService.class));
        context.registerSingleton(IRegistrationService.class, () -> context.get(RegistrationService.class));
        context.registerSingleton(IVerificationService.class, () -> context.get(VerificationService.class));

        // Business Services
        context.registerSingleton(IUserService.class, () -> context.get(UserService.class));
        context.registerSingleton(IClientsService.class, () -> context.get(ClientsService.class));
        context.registerSingleton(IDeviceService.class, () -> context.get(DeviceService.class));
        context.registerSingleton(IRequestsService.class, () -> context.get(RequestsService.class));
        context.registerSingleton(IStatusService.class, () -> context.get(StatusService.class));
        context.registerSingleton(IAccessoriesService.class, () -> context.get(AccessoriesService.class));
        context.registerSingleton(IPartsService.class, () -> context.get(PartsService.class));
        context.registerSingleton(IOptionsService.class, () -> context.get(OptionsService.class));
        
        // Utilities
        context.registerSingleton(LocalizationBinder.class, () -> new LocalizationBinder(context.get(ILocalizationService.class)));
        
        // Other
        context.registerSingleton(HostServices.class, () -> getHostServices());
    }

    @Override
    public void start(Stage stage) throws IOException {
        Platform.runLater(() -> {
            context.get(Initializer.class).initialize();
            var themeService = context.get(IThemeService.class);

            themeService.loadTheme();
            themeService.applyCurrentTheme();
            
            try {
                new LoginWindow(context).start(stage);
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit();
            }
        });
    }

    @Override
    public void stop() throws SQLException {
        if (context != null) {
            context.get(IDatabaseService.class).close();
        }
        Platform.exit();
        System.exit(0);
    }
}
