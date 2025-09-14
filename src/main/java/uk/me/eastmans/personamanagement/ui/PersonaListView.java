package uk.me.eastmans.personamanagement.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.personamanagement.PersonaService;
import uk.me.eastmans.security.Persona;

import java.util.Collections;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("persona-list")
@RolesAllowed("PERSONAS")
@PageTitle("Personas List")
@Menu(order = 1, icon = "vaadin:user-check", title = "Persona List")
class PersonaListView extends Main {

    private PersonaService personaService;

    final TextField name;
    final Button createBtn;
    final Grid<Persona> personaGrid;

    PersonaListView(PersonaService personaService) {
        this.personaService = personaService;

        name = new TextField();
        name.setPlaceholder("Persona Name");
        name.setAriaLabel("Persona name");
        name.setMaxLength(Persona.NAME_MAX_LENGTH);
        name.setMinWidth("20em");

        createBtn = new Button("Create", event -> createPersona());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        personaGrid = new Grid<>();
        personaGrid.setItems(query -> personaService.list(toSpringPageRequest(query)).stream());
        personaGrid.addColumn(Persona::getName).setHeader("Name");
        personaGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Persona List", ViewToolbar.group(name, createBtn)));
        add(personaGrid);

    }

    private void createPersona() {
        personaService.createPersona(name.getValue(), Collections.EMPTY_SET);
        personaGrid.getDataProvider().refreshAll();
        name.clear();
        Notification.show("Persona added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}