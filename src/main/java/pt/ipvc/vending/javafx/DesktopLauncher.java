package pt.ipvc.vending.javafx;

import javafx.application.Application;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import pt.ipvc.vending.VendingRentalApplication;

public class DesktopLauncher {

    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        springContext = new SpringApplicationBuilder(VendingRentalApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run(args);
        Application.launch(DesktopApplication.class, args);
    }

    static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }
}
