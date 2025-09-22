package uk.me.eastmans.usermanagement.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.security.User;
import uk.me.eastmans.usermanagement.UserService;

@Route("user-edit")
@RolesAllowed("USERS")
@PageTitle("User Edit")
class UserEditView extends Main {

    private User user;
    final UserService userService;
    final ViewToolbar viewToolbar;
    final TextField username;
    final Button saveButton;
    final Binder<User> editBinder = new Binder<>(User.class);

    UserEditView(UserService userService) {
        this.userService = userService;

        username = new TextField("Username");
        editBinder.forField(username)
                .asRequired("Every User must have a unique name")
                .withValidator(name -> name.length() <= User.USERNAME_MAX_LENGTH,
                        "Name length must be less than " + (User.USERNAME_MAX_LENGTH+1) + ".")
                .bind( User::getUsername, User::setUsername );
        PasswordField password = new PasswordField("Password");
        editBinder.forField(password)
                .asRequired("The password must be set")
                .withValidator(name -> name.length() <= User.PASSWORD_MAX_LENGTH,
                        "The password length must be less than " + (User.PASSWORD_MAX_LENGTH+1) + ".")
                .bind( User::getPassword, User::setPassword );
        PasswordField confirmPassword = new PasswordField("Confirm password");
        confirmPassword.setRequired(true);
        Checkbox enabled = new Checkbox("Enabled");
        editBinder.forField(enabled)
                .bind( User::isEnabled, User::setEnabled );

        FormLayout formLayout = new FormLayout();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(username);
        formLayout.addFormRow(password, confirmPassword);
        formLayout.addFormRow(enabled);

        viewToolbar = new ViewToolbar("User Edit", ViewToolbar.group());
        add(viewToolbar);

        Scroller scroller = new Scroller(formLayout);
        add(scroller);

        saveButton = new Button("Save", e -> {
            // We need to persis the changes back to the database
            saveOrCreate();
        } );
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel");

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancel);
        add(buttonLayout);
    }

    public void editUser(User user) {
        if (user.getUsername() == null) {
            viewToolbar.setTitle("Create User");
            saveButton.setText("Save");
        }
        this.user = user;
        editBinder.readBean(user);
    }

    private void saveOrCreate() {
        try {
            editBinder.writeBean(user);
            /*
            Set<Authority> selections = selectedAuthoprities.getSelectedItems();
            // Get the selected authorities
            persona.setAuthorities(selections);
            */
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
}
