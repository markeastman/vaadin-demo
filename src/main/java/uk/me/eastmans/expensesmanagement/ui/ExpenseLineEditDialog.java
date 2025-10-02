package uk.me.eastmans.expensesmanagement.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.LumoUtility;
import uk.me.eastmans.expensesmanagement.ExpenseHeader;
import uk.me.eastmans.expensesmanagement.ExpenseLine;
import uk.me.eastmans.expensesmanagement.ExpenseService;

import java.math.BigDecimal;

public class ExpenseLineEditDialog extends Dialog {

    private ExpenseService expenseService;
    private ExpenseHeader expenseHeader;
    private ExpenseLine expenseLine;

    Binder<ExpenseLine> editBinder = new Binder<>(ExpenseLine.class);
    TextField descriptionField;
    BigDecimalField transactionAmountField;
    BigDecimalField amountField;
    Button saveButton;

    public ExpenseLineEditDialog(ExpenseService expenseService) {
        this.expenseService = expenseService;

        // Create the dialog
        // Edit the line and then save to the expense header if we need to
        FormLayout formLayout = new FormLayout();
        formLayout.setExpandColumns(true);
        formLayout.setExpandFields(true);
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        formLayout.setAutoResponsive(true);

        // Build the dialog
        descriptionField = new TextField("Description");
        editBinder.forField(descriptionField)
                .asRequired("Every expense line must have a description")
                .withValidator(name -> name.length() <= ExpenseLine.DESCRIPTION_MAX_LENGTH,
                        "Description length must be less than " + (ExpenseLine.DESCRIPTION_MAX_LENGTH+1) + ".")
                .bind( ExpenseLine::getDescription, ExpenseLine::setDescription );
        transactionAmountField = new BigDecimalField("Transaction Amount");
        editBinder.forField(transactionAmountField)
                .asRequired("Every expense line must have a transaction amount")
                .withValidator(value -> value.compareTo(BigDecimal.ZERO) > 0,
                        "Transaction value must be greater than zero")
                .bind( ExpenseLine::getCurrencyAmount, ExpenseLine::setCurrencyAmount );
        amountField = new BigDecimalField("Corporate Converted Amount");
        editBinder.forField(amountField)
                .asRequired("Every expense line must have a transaction amount")
                .withValidator(value -> value.compareTo(BigDecimal.ZERO) > 0,
                        "Transaction value must be greater than zero")
                .bind( ExpenseLine::getBaseAmount, ExpenseLine::setBaseAmount );

        formLayout.add(descriptionField);
        formLayout.add(transactionAmountField);
        formLayout.add(amountField);

        // Create the buttons
        saveButton = new Button("Save", e -> {
            // We need to persis the changes back to the database
            saveOrCreate();
        } );
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

        add(formLayout);
    }

    public void editExpenseLine( ExpenseLine expenseLine, ExpenseHeader expenseHeader, boolean createMode) {
        this.expenseLine = expenseLine;
        this.expenseHeader = expenseHeader;

        setHeaderTitle(createMode ? "New Expense Line" : "Edit Expense Line");
        // load the data into the fields from the Persona entity
        editBinder.readBean(expenseLine);

        // Enable or disable save button based on validation errors
        editBinder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            saveButton.setEnabled(hasChanges && isValid);
        });
        descriptionField.focus();

        // Show the dialog
        open();
    }

    private void saveOrCreate() {
        // Get the fields and set the persona
        try {
            editBinder.writeBean(expenseLine);
            if (editBinder.validate().isOk()) {
                //expenseService.saveOrCreate(expenseLine);
                Notification.show("Expense line saved", 3000, Notification.Position.BOTTOM_END)
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
