package uk.me.eastmans.usermanagement.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.personamanagement.PersonaService;
import uk.me.eastmans.security.Persona;
import uk.me.eastmans.security.User;
import uk.me.eastmans.usermanagement.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Route("user-edit")
@RolesAllowed("USERS")
@PageTitle("User Edit")
public class UserEditView extends Main implements HasUrlParameter<String> {

    private User user;
    final UserService userService;
    final PersonaService personaService;
    final List<Persona> allPersonas;
    final ViewToolbar viewToolbar;
    final TextField username;
    final Button saveButton;
    final Binder<User> editBinder = new Binder<>(User.class);
    final CheckboxGroup<Persona> selectedPersonas;
    final Select<Persona> defaultPersona;
    final PasswordField passwordField;

    public static void editUser(Long userId) {
        UI.getCurrent().navigate(UserEditView.class, String.valueOf(userId));
    }

    UserEditView(UserService userService,
                 PersonaService personaService) {
        this.userService = userService;
        this.personaService = personaService;
        allPersonas = personaService.listAll();

        username = new TextField("Username");
        editBinder.forField(username)
                .asRequired("Every User must have a unique name")
                .withValidator(name -> name.length() <= User.USERNAME_MAX_LENGTH,
                        "Name length must be less than " + (User.USERNAME_MAX_LENGTH+1) + ".")
                .bind( User::getUsername, User::setUsername );
        passwordField = new PasswordField("Password");
        editBinder.forField(passwordField)
                .asRequired("The password must be set")
                .withValidator(name -> name.length() <= User.PASSWORD_MAX_LENGTH,
                        "The password length must be less than " + (User.PASSWORD_MAX_LENGTH+1) + ".")
                .bind( User::getPassword, User::setPassword );
        Button changePassword = new Button("Change Password", event -> {
            // We have been clicked so display dialog to edit the password
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
            changePasswordDialog.openDialog(this);
        });
        changePassword.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        // We need to align the button with the text field of password and
        // not the label when displayed in vertical alignment.
        VerticalLayout buttonArea = new VerticalLayout();
        buttonArea.setPadding(false);
        buttonArea.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonArea.add(changePassword);
        Checkbox enabled = new Checkbox("Enabled");
        editBinder.forField(enabled)
                .bind( User::isEnabled, User::setEnabled );
        defaultPersona = new Select<>();
        defaultPersona.setLabel("Default persona");
        editBinder.forField(defaultPersona)
                .asRequired("You must set a default persona")
                .bind( User::getDefaultPersona, User::setDefaultPersona );

        selectedPersonas = new CheckboxGroup<>();
        selectedPersonas.setLabel("Personas");
        selectedPersonas.setItems(allPersonas);
        selectedPersonas.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        selectedPersonas.setRequiredIndicatorVisible(true);

        FormLayout formLayout = new FormLayout();
        //formLayout.setExpandColumns(true);
        formLayout.setExpandFields(true);
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(username);
        formLayout.setColspan(username, 2);
        formLayout.addFormRow(passwordField, buttonArea);
        formLayout.addFormRow(enabled);
        formLayout.addFormRow(selectedPersonas);
        formLayout.setColspan(selectedPersonas, 2);
        formLayout.addFormRow(defaultPersona);
        formLayout.setColspan(defaultPersona, 2);

        viewToolbar = new ViewToolbar("User Edit", ViewToolbar.group());
        add(viewToolbar);

        Scroller scroller = new Scroller(formLayout);
        add(scroller);

        saveButton = new Button("Save", e -> {
            // We need to persis the changes back to the database
            saveOrCreate();
        } );
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", e -> {
            History history = UI.getCurrent().getPage().getHistory();
            history.back();
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancel);
        add(buttonLayout);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user.getUsername() == null) {
            viewToolbar.setTitle("Create User");
            saveButton.setText("Save");
            saveButton.setEnabled(false);
        }
        this.user = user;
        editBinder.readBean(user);
        // Select the relevant personas, loop through and select appropriately
        selectedPersonas.clear();
        selectedPersonas.select(user.getPersonas());
        selectedPersonas.addSelectionListener(event -> {
            // Some changes have been made, so update the default persona selection box
            // Try to keep the selected item
            Persona selected = defaultPersona.getValue();
            defaultPersona.clear();
            defaultPersona.setItems(selectedPersonas.getSelectedItems());
            if (selected != null && selectedPersonas.getSelectedItems().contains(selected)) {
                defaultPersona.setValue(selected);
            } else {
                defaultPersona.setValue(null);
            }
        });

        defaultPersona.setItems(user.getPersonas());
        defaultPersona.setValue(user.getDefaultPersona());

        // Enable or disable save button based on validation errors
        /* This does not work properly, probably my implementation
        editBinder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            saveButton.setEnabled(hasChanges && isValid);
        });
        */
        username.focus();
    }

    public void setPassword(String password) {
        user.setPassword(password);
        // Need to update screen
        passwordField.setValue(user.getPassword());
    }

    private void saveOrCreate() {
        try {
            Set<Persona> selections = selectedPersonas.getSelectedItems();
            // Get the selected authorities
            user.setPersonas(selections);
            editBinder.writeBean(user);
            // Get the defaultPersona
            if (editBinder.validate().isOk()) {
                userService.saveOrCreate(user);
                Notification.show("User saved", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                History history = UI.getCurrent().getPage().getHistory();
                history.back();
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

    @Override
    public void setParameter(BeforeEvent beforeEvent, String aLong) {
        // Get user with the passed id, or create a new User
        User passedUser;
        if (aLong == null) {
            passedUser = new User("");
        } else {
            Optional<User> u = userService.getUser(Long.parseLong(aLong));
            passedUser = u.orElseGet(() -> new User(null));
        }
        setUser(passedUser);
    }
}
