package uk.me.eastmans.personamanagement.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import uk.me.eastmans.personamanagement.PersonaService;
import uk.me.eastmans.security.Authority;
import uk.me.eastmans.security.Persona;

import java.util.List;
import java.util.Set;

public class PersonaEditDialog extends Dialog {

    final PersonaService personaService;
    final List<Authority> allAuthorities;
    private Persona persona;

    final private TextField nameField;
    final private CheckboxGroup<Authority> selectedAuthoprities;

    public PersonaEditDialog(PersonaService personaService) {
        this.personaService = personaService;
        allAuthorities = personaService.getAllAuthorities();

        // Create the main layout
        VerticalLayout layout = new VerticalLayout();

        // Build the dialog
        nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setMinLength(1);
        nameField.setMaxLength(Persona.NAME_MAX_LENGTH);
        layout.add(nameField);

        selectedAuthoprities = new CheckboxGroup<>();
        selectedAuthoprities.setLabel("Authorities");
        selectedAuthoprities.setItems(allAuthorities);
        selectedAuthoprities.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        layout.add(selectedAuthoprities);

        // Create the buttons
        Button saveButton = new Button("Save", e -> {
            // We need to persis the changes back to the database
            saveOrCreate();
        } );
        Button cancelButton = new Button("Cancel", e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

        add(layout);

        nameField.focus();
    }

    public void open( Persona persona, boolean createMode)
    {
        this.persona = persona;
        setHeaderTitle(createMode ? "New Persona" : "Edit Persona");
        // load the data into the fields from the Persona entity
        nameField.setValue(persona.getName());
        nameField.setErrorMessage(""); // Clear it
        // Select the relevant authorities, loop through and select appropriately
        selectedAuthoprities.clear();
        selectedAuthoprities.select(persona.getAuthorities());

        // Show the dialog
        open();
    }

    private void saveOrCreate() {
        // Get the fields and set the persona
        try {
            persona.setName(nameField.getValue());
            Set<Authority> selections = selectedAuthoprities.getSelectedItems();
            // Get the selected authorities
            persona.setAuthorities(selections);
            personaService.saveOrCreate(persona);
            Notification.show("Persona saved", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
            // Reload the list view that we came from
            UI.getCurrent().getPage().reload();
        } catch (Exception e) {
            // Something went wrong so inform the user
            nameField.setErrorMessage(e.getMessage()); // Need a way to display this
        }
    }
}
