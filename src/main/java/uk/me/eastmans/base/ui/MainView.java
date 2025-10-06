package uk.me.eastmans.base.ui;

import com.vaadin.flow.component.html.H2;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * This view shows up when a user navigates to the root ('/') of the application.
 */
@Route
@PermitAll
@Menu(order = -100, icon = "vaadin:home", title = "Welcome!")
public final class MainView extends Main {

    // TODO Replace with your own main view.

    MainView() {
        addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.BoxSizing.BORDER);
        setSizeFull();

        var contentDiv = new Div();
        contentDiv.addClassNames(LumoUtility.Flex.GROW, LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.AlignItems.CENTER);
                //LumoUtility.JustifyContent.CENTER);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication auth = securityContext.getAuthentication();

        var centerDiv = new Div( new H2("You have the following authorities"));
        centerDiv.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.AlignItems.CENTER);

        auth.getAuthorities().forEach(authority ->
                centerDiv.add(new Paragraph(authority.toString())));
        contentDiv.add(centerDiv);

        add(new ViewToolbar("Welcome to Vaadin! " ));
        add(contentDiv);
    }
}