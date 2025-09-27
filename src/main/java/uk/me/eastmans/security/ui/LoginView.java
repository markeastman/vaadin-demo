package uk.me.eastmans.security.ui;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import uk.me.eastmans.base.ui.component.MessageDialog;

@Route(value = "login", autoLayout = false)
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends Main implements BeforeEnterObserver {

    public LoginView() {
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.JustifyContent.CENTER,
                LumoUtility.AlignItems.CENTER);
        setSizeFull();
        LoginOverlay loginOverlay = new LoginOverlay();
        loginOverlay.setTitle("Vaadin Demo");
        loginOverlay.setDescription("Built by M. Eastman");

        Paragraph text = new Paragraph("Never tell your password to anyone");
        text.addClassName(LumoUtility.TextAlignment.CENTER);
        loginOverlay.getFooter().add(text);
        loginOverlay.getElement().getThemeList().add("dark");

        add(loginOverlay);

        loginOverlay.setAction("login");
        loginOverlay.addForgotPasswordListener(event ->
            MessageDialog.show("Forgotten password:","<p>Not sure how to get it back</p>") );
        loginOverlay.setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {}
}