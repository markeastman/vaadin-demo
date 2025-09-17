package uk.me.eastmans.personamanagement.ui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.personamanagement.PersonaService;
import uk.me.eastmans.security.Persona;

import java.util.HashSet;

@Route("persona-list")
@RolesAllowed("PERSONAS")
@PageTitle("Personas List")
@Menu(order = 1, icon = "vaadin:user-check", title = "Persona List")
class PersonaListView extends Main {

    final PersonaService personaService;
    final Grid<Persona> personaGrid;
    private PersonaEditDialog editDialog;

    PersonaListView(PersonaService personaService) {
        this.personaService = personaService;

        personaGrid = new Grid<>(Persona.class, false);
        personaGrid.setItems(personaService.listAll());
        personaGrid.setSizeFull();
        personaGrid.addColumn(Persona::getName).setHeader("Name").setResizable(true)
                .setAutoWidth(true).setFlexGrow(0);
        personaGrid.addColumn(Persona::getAuthorities).setHeader("Roles");
        HorizontalLayout actionsHeaderLayout = new HorizontalLayout();
        actionsHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        actionsHeaderLayout.add(new Text("Actions") );
        Button newButton = new Button(new Icon(VaadinIcon.PLUS));
        newButton.setTooltipText("Create a new Persona");
        newButton.addClickListener(event -> {
            // Create a new Persona
            Persona newPersona = new Persona("", new HashSet<>());
            editDialog.open(newPersona, true);
        });
        actionsHeaderLayout.add(newButton);
        personaGrid.addComponentColumn(persona -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setTooltipText("Edit this Persona");
            editButton.addClickListener(e -> editDialog.open(persona, false));
            actionsLayout.add(editButton);
            actionsLayout.add(createRemoveButton(persona));
            return actionsLayout;
        }).setHeader(actionsHeaderLayout).setWidth("150px").setFlexGrow(0);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

       add(personaGrid);

       editDialog = new PersonaEditDialog(personaService);
    }

    private Button createRemoveButton(Persona persona) {
        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.setTooltipText("Remove this Persona");
        removeButton.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Are you sure?");
            dialog.setText("This will delete the Persona '" +
                    persona.getName() + "' and remove it from every user");
            dialog.setCancelable(true);
            dialog.setCancelText("No");
            dialog.setConfirmText("Yes");
            dialog.addConfirmListener(event -> removePersona(persona) );
            dialog.open();
        });
        return removeButton;
    }

    private void removePersona(Persona persona) {
        // Remove the persona and update grid.
        personaService.deletePersona(persona);
        personaGrid.setItems(personaService.listAll());

        //personaGrid.getDataProvider().refreshItem(persona);
        Notification.show("Persona deleted", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);}

}