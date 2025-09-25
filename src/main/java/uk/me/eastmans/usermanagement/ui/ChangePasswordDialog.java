package uk.me.eastmans.usermanagement.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class ChangePasswordDialog extends Dialog {

    final PasswordField passwordField = new PasswordField("New password");
    final PasswordField confirmPasswordField = new PasswordField("Confirm password");
    final Button saveButton;

    private UserEditView userEditView;

    public ChangePasswordDialog() {

        // Create the main layout
        VerticalLayout layout = new VerticalLayout();

        // Build the dialog
        layout.add(passwordField, confirmPasswordField);

        // Create the buttons
        saveButton = new Button("Save", e -> {
            // We need to persist the changes back to the user that was passed in to us
            savePassword();
        } );
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

        add(layout);
    }

    public void openDialog( UserEditView editView)
    {
        this.userEditView = editView;
        setHeaderTitle("Change Password");
        passwordField.setValue( "" );
        passwordField.setValueChangeMode(ValueChangeMode.EAGER);
        confirmPasswordField.setValue( "" );
        confirmPasswordField.setValueChangeMode(ValueChangeMode.EAGER);
        saveButton.setEnabled(false);

        // Enable or disable save button based on validation errors
        passwordField.addInputListener( event -> saveButton.setEnabled( isPasswordValid() ));
        confirmPasswordField.addInputListener( event -> saveButton.setEnabled( isPasswordValid() ));
        passwordField.focus();

        // Show the dialog
        open();
    }

    private void savePassword() {
        // Check the validation aspects
        if (isPasswordValid()) {
            userEditView.setPassword(passwordField.getValue());
            // Check password and confirm are the same
            Notification.show("Password updated", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        }
    }

    private boolean isPasswordValid() {
        return passwordField.getValue().equals(confirmPasswordField.getValue());
    }
}