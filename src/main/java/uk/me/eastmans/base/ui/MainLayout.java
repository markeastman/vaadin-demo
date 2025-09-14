package uk.me.eastmans.base.ui;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.me.eastmans.security.MyUserDetailsService;
import uk.me.eastmans.security.MyUserPrincipal;
import uk.me.eastmans.security.Persona;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
@PermitAll
public final class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    private MyUserDetailsService userDetailsService;

    public MainLayout(AuthenticationContext authenticationContext,
                      MyUserDetailsService userDetailsService) {
        this.authenticationContext = authenticationContext;
        this.userDetailsService = userDetailsService;

        setPrimarySection(Section.NAVBAR);

        SideNav nav = createSideNav();

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
        addToNavbar(createTopHeader());
    }

    private Div createTopHeader() {
        DrawerToggle toggle = new DrawerToggle();

        var appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);

        var appName = new Span("Demo App");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);

        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        layout.addToStart(toggle);
        layout.addToStart(appLogo);
        layout.addToStart(appName);
        layout.addToEnd(createUserDropdown());
        Div div = new Div();
        div.setClassName(Padding.NONE);
        div.getElement().getStyle().set("width", "100%");
        div.add(layout);
        return div;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(Margin.Horizontal.MEDIUM);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }

    private Component createUserDropdown() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_DROPDOWN_INDICATORS);
        ComponentEventListener<ClickEvent<MenuItem>> personaPicklistener =
                e -> switchToPersonaWithName(e.getSource().getText()); // Change to new Persona

        if (authenticationContext.getPrincipalName().isPresent()) {
            MenuItem share = menuBar.addItem(authenticationContext.getPrincipalName().get());
            SubMenu shareSubMenu = share.getSubMenu();
            // Add the set of persona
            MyUserPrincipal myUser = authenticationContext.getAuthenticatedUser(MyUserPrincipal.class).get();
            Set<Persona> personas = myUser.getPersonas();
            Persona currentPersona = myUser.getCurrentPersona();
            // We want a sorted list of Personas
            ArrayList<Persona> arrayList = new ArrayList<Persona>(personas);
            // sorting the list
            Collections.sort(arrayList);
            arrayList.forEach(
                    persona -> {
                        MenuItem mItem = shareSubMenu.addItem(persona.getName(), personaPicklistener);
                        mItem.setCheckable(true);
                        if (persona.equals(currentPersona)) {
                            mItem.setChecked(true);
                        }
                        if (persona.equals(currentPersona))
                            mItem.setEnabled(false);
                    });

            shareSubMenu.addSeparator();
            ComponentEventListener<ClickEvent<MenuItem>> logoutListener =
                    e -> authenticationContext.logout();
            shareSubMenu.addItem("Logout", logoutListener);
        }
        return menuBar;
    }

    private void switchToPersonaWithName(String name) {
        // We must of selected a new Persona as the current persona is disabled
        LoggerFactory.getLogger(MainLayout.class)
                .warn("Switching to persona with name = " + name);
        // First switch to the new Persona within the user object and then
        MyUserPrincipal myUser = authenticationContext.getAuthenticatedUser(MyUserPrincipal.class).get();
        userDetailsService.switchPersonaForUser(myUser.getUser(),name);
        // Update granted authorities
        myUser.buildGrantedAuthorities();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                auth.getPrincipal(), auth.getCredentials(), myUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        // Get vaadin to recalculate the layout security aspects for the new persona
        UI.getCurrent().getPage().reload();
        //getUI().ifPresent(ui -> ui.getPage().reload());
    }
}
