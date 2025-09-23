package uk.me.eastmans.personamanagement.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import uk.me.eastmans.personamanagement.PersonaService;
import uk.me.eastmans.security.Authority;
import uk.me.eastmans.security.Persona;

import java.util.List;
import java.util.Set;

public class PersonaEditDialog extends Dialog {

    final PersonaService personaService;
    final List<Authority> allAuthorities;
    final Binder<Persona> editBinder = new Binder<>(Persona.class);

    final TextField nameField;
    final Button saveButton;
    final CheckboxGroup<Authority> selectedAuthorities;

    private Persona persona;

    public PersonaEditDialog(PersonaService personaService) {
        this.personaService = personaService;
        allAuthorities = personaService.getAllAuthorities();

        // Create the main layout
        VerticalLayout layout = new VerticalLayout();

        // Build the dialog
        nameField = new TextField("Name");
        editBinder.forField(nameField)
                .asRequired("Every Persona must have a unique name")
                .withValidator(name -> name.length() <= Persona.NAME_MAX_LENGTH,
                        "Name length must be less than " + (Persona.NAME_MAX_LENGTH+1) + ".")
                .bind( Persona::getName, Persona::setName );
        layout.add(nameField);

        selectedAuthorities = new CheckboxGroup<>();
        selectedAuthorities.setLabel("Authorities");
        selectedAuthorities.setItems(allAuthorities);
        selectedAuthorities.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        layout.add(selectedAuthorities);

        // Create the buttons
        saveButton = new Button("Save", e -> {
            // We need to persis the changes back to the database
            saveOrCreate();
        } );
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

        add(layout);
   }

    public void open( Persona persona, boolean createMode)
    {
        this.persona = persona;
        setHeaderTitle(createMode ? "New Persona" : "Edit Persona");
        // load the data into the fields from the Persona entity
        editBinder.readBean(persona);
        // Select the relevant authorities, loop through and select appropriately
        selectedAuthorities.clear();
        selectedAuthorities.select(persona.getAuthorities());

        // Enable or disable save button based on validation errors
        editBinder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            saveButton.setEnabled(hasChanges && isValid);
        });
        nameField.focus();

        // Show the dialog
        open();
    }

    private void saveOrCreate() {
        // Get the fields and set the persona
        try {
            editBinder.writeBean(persona);
            Set<Authority> selections = selectedAuthorities.getSelectedItems();
            // Get the selected authorities
            persona.setAuthorities(selections);
            if (editBinder.validate().isOk()) {
                personaService.saveOrCreate(persona);
                Notification.show("Persona saved", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                close();
                // Reload the list view that we came from
                UI.getCurrent().getPage().reload();
            }
        } catch (Exception e) {
            // Something went wrong so inform the user
            notifyValidationErrors(e);
        }
    }

    private void notifyValidationErrors(Exception e) {
        // Need to show the error somehow
        Notification.show(e.getMessage(), 5000, Notification.Position.TOP_STRETCH)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
