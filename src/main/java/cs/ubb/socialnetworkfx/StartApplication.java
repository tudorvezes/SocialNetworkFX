package cs.ubb.socialnetworkfx;

import cs.ubb.socialnetworkfx.config.DatabaseConnectionConfig;
import cs.ubb.socialnetworkfx.controller.LoginController;
import cs.ubb.socialnetworkfx.domain.*;
import cs.ubb.socialnetworkfx.domain.validator.*;
import cs.ubb.socialnetworkfx.dto.*;
import cs.ubb.socialnetworkfx.repository.*;
import cs.ubb.socialnetworkfx.repository.paging.PagingRepository;
import cs.ubb.socialnetworkfx.repository.passwordRepository.PasswordDBRepository;
import cs.ubb.socialnetworkfx.repository.passwordRepository.PasswordRepository;
import cs.ubb.socialnetworkfx.service.AdminService;
import cs.ubb.socialnetworkfx.service.GeneralService;
import cs.ubb.socialnetworkfx.utils.idGetter.IdGetter;
import cs.ubb.socialnetworkfx.utils.idGetter.IdGetterDB;
import cs.ubb.socialnetworkfx.utils.passwordEncryptor.PasswordEncryptor;
import cs.ubb.socialnetworkfx.utils.passwordEncryptor.PasswordHashEncryptor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class StartApplication extends Application {
    Validator<User> userValidator;
    Validator<Friendship> friendshipValidator;
    Validator<Message> messageValidator;
    Validator<Post> postValidator;
    PagingRepository<Long, User, UserFilterDTO> userRepository;
    Repository<Long, Friendship, FriendshipFilterDTO> friendshipRepository;
    Repository<Long, FriendshipRequest, FilterDTO> friendshipRequestRepository;
    Repository<Long, ChatRoom, ChatRoomFilterDTO> chatRoomDatabase;
    Repository<Long, Message, MessageFilterDTO> messageRepository;
    PagingRepository<Long, Post, PostFilterDTO> postDatabase;
    PasswordRepository<Long> passwordRepository;
    PasswordEncryptor<String> passwordEncryptor = new PasswordHashEncryptor();
    IdGetter<Long> idGetter;
    GeneralService generalService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        String url = DatabaseConnectionConfig.URL;
        String username = DatabaseConnectionConfig.USERNAME;
        String password = DatabaseConnectionConfig.PASSWORD;

        userValidator = new UserValidator();
        friendshipValidator = new FriendshipValidator();
        messageValidator = new MessageValidator();
        postValidator = new PostValidator();
        userRepository = new UserDBRepository(url, username, password, userValidator);
        friendshipRepository = new FriendshipDBRepository(url, username, password, friendshipValidator);
        friendshipRequestRepository = new FriendshipRequestDBRepository(url, username, password);
        chatRoomDatabase = new ChatRoomDBRepository(url, username, password);
        messageRepository = new MessageDBRepository(url, username, password, messageValidator);
        postDatabase = new PostDBRepository(url, username, password, postValidator);
        passwordRepository = new PasswordDBRepository(url, username, password, passwordEncryptor);
        idGetter = new IdGetterDB(url, username, password);
        generalService = new GeneralService(userRepository, friendshipRepository, friendshipRequestRepository, chatRoomDatabase, messageRepository, postDatabase, passwordRepository, idGetter);

        //ConsoleUI console = new ConsoleUI(adminService);
        //console.run();

        initView(primaryStage);
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException {
        FXMLLoader userLoader = new FXMLLoader();
        userLoader.setLocation(getClass().getResource("views/login-view.fxml"));
        AnchorPane userLayout = userLoader.load();
        primaryStage.setScene(new javafx.scene.Scene(userLayout));
        LoginController loginController = userLoader.getController();
        loginController.setService(generalService, primaryStage);
    }
}
