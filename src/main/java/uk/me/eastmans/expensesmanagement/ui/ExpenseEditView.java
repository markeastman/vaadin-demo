package uk.me.eastmans.expensesmanagement.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.expensesmanagement.ExpenseHeader;
import uk.me.eastmans.expensesmanagement.ExpenseService;
import uk.me.eastmans.security.MyUserPrincipal;
import uk.me.eastmans.security.User;

import java.util.Optional;

@Route("expense-edit")
@RolesAllowed("EXPENSES")
@PageTitle("Expense Edit")
public class ExpenseEditView extends Main implements HasUrlParameter<String> {

    private User user;
    private ExpenseHeader expenseHeader;
    final ExpenseService expenseService;
    final ViewToolbar viewToolbar;
    final TextField name;
    final TextField description;
    final BigDecimalField totalAmount;
    final Button saveButton;
    final Binder<ExpenseHeader> editBinder = new Binder<>(ExpenseHeader.class);

    public static void editExpense(Long expenseId) {
        UI.getCurrent().navigate(ExpenseEditView.class, String.valueOf(expenseId));
    }

    ExpenseEditView(AuthenticationContext authenticationContext,
                    ExpenseService expenseService) {
        this.expenseService = expenseService;
        if (authenticationContext.getAuthenticatedUser(MyUserPrincipal.class).isPresent())
            user = authenticationContext.getAuthenticatedUser(MyUserPrincipal.class).get().getUser();

        name = new TextField("Name");
        editBinder.forField(name)
                .asRequired("Every Expense must have a name")
                .withValidator(name -> name.length() <= ExpenseHeader.NAME_MAX_LENGTH,
                        "Name length must be less than " + (ExpenseHeader.NAME_MAX_LENGTH+1) + ".")
                .bind( ExpenseHeader::getName, ExpenseHeader::setName );
        description = new TextField("Description");
        editBinder.forField(description)
                .asRequired("Every Expense must have a description")
                .withValidator(description -> description.length() <= ExpenseHeader.DESCRIPTION_MAX_LENGTH,
                        "Description length must be less than " + (ExpenseHeader.DESCRIPTION_MAX_LENGTH+1) + ".")
                .bind( ExpenseHeader::getDescription, ExpenseHeader::setDescription );
        totalAmount = new BigDecimalField("Total");
        totalAmount.setReadOnly(true);
        editBinder.forField(totalAmount)
                .bind(ExpenseHeader::getTotalAmount, null);

        FormLayout formLayout = new FormLayout();
        formLayout.setExpandColumns(true);
        formLayout.setExpandFields(true);
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        formLayout.setAutoResponsive(true);

        FormLayout.FormRow row = new FormLayout.FormRow();
        row.add(name);
        row.add(description, 2); // colspan 2
        row.add(totalAmount);
        formLayout.add(row);

        viewToolbar = new ViewToolbar("Expense Edit", ViewToolbar.group());
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

    public ExpenseHeader getExpenseHeader() {
        return expenseHeader;
    }

    public void setExpenseHeader(ExpenseHeader header) {
        if (header.getId() == null) {
            viewToolbar.setTitle("Create Expense");
            saveButton.setText("Save");
            saveButton.setEnabled(false);
        }
        this.expenseHeader = header;
        editBinder.readBean(expenseHeader);

        name.focus();
    }

    private void saveOrCreate() {
        try {
            editBinder.writeBean(expenseHeader);
            if (editBinder.validate().isOk()) {
                expenseService.saveOrCreate(expenseHeader);
                Notification.show("Expense saved", 3000, Notification.Position.BOTTOM_END)
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
        // Get expense with the passed id, or create a new expense
        ExpenseHeader passedExpense;
        if (aLong == null) {
            passedExpense = new ExpenseHeader(user, "", "");
        } else {
            Optional<ExpenseHeader> u = expenseService.getExpense(Long.parseLong(aLong));
            passedExpense = u.orElseGet(() -> new ExpenseHeader(user,"", ""));
        }
        setExpenseHeader(passedExpense);
    }
}