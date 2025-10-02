package uk.me.eastmans.expensesmanagement.ui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ConfirmDeleteDialog;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.expensesmanagement.ExpenseHeader;
import uk.me.eastmans.expensesmanagement.ExpenseLine;
import uk.me.eastmans.expensesmanagement.ExpenseService;
import uk.me.eastmans.security.MyUserPrincipal;
import uk.me.eastmans.security.User;

import java.util.List;
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
    final TextField totalAmount;
    final Button saveButton;
    final Binder<ExpenseHeader> editBinder = new Binder<>(ExpenseHeader.class);
    final Grid<ExpenseLine> linesGrid;
    final ExpenseLineEditDialog expenseLineEditDialog;

    public static void editExpense(Long expenseId) {
        UI.getCurrent().navigate(ExpenseEditView.class, String.valueOf(expenseId));
    }

    ExpenseEditView(AuthenticationContext authenticationContext,
                    ExpenseService expenseService) {
        this.expenseService = expenseService;
        if (authenticationContext.getAuthenticatedUser(MyUserPrincipal.class).isPresent())
            user = authenticationContext.getAuthenticatedUser(MyUserPrincipal.class).get().getUser();

        expenseLineEditDialog = new ExpenseLineEditDialog(expenseService);

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
        totalAmount = new TextField("Total");
        totalAmount.setReadOnly(true);
        editBinder.forField(totalAmount)
                .bind(ExpenseHeader::getFormattedTotalAmount, null);

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

        //Scroller scroller = new Scroller(formLayout);
        add(formLayout);

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

        // Add the lines
        linesGrid = new Grid<>(ExpenseLine.class, false);
        linesGrid.setSizeFull();
        linesGrid.addColumn(ExpenseLine::getCategory).setHeader("Category")
                .setResizable(true).setAutoWidth(true).setFlexGrow(0);
        linesGrid.addColumn(ExpenseLine::getDescription).setHeader("Description")
                .setResizable(true).setAutoWidth(true).setFlexGrow(0);
        linesGrid.addColumn(
                new NumberRenderer<>(ExpenseLine::getCurrencyAmount, "%(,.2f"))
                .setResizable(true).setAutoWidth(true).setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END)
                .setHeader("Currency Amount");
        linesGrid.addColumn(ExpenseLine::getCurrencyCode)
                .setResizable(true).setAutoWidth(true).setFlexGrow(0)
                .setHeader("Currency");
        linesGrid.addColumn(
                new NumberRenderer<>(ExpenseLine::getBaseAmount, "%(,.2f"))
                .setResizable(true).setAutoWidth(true).setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END)
                .setHeader("Base Amount");
        Grid.Column<ExpenseLine> actionsColumn = linesGrid.addColumn(new ComponentRenderer<>(header -> new Span()));
        HorizontalLayout actionsHeaderLayout = new HorizontalLayout();
        actionsHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        actionsHeaderLayout.add(new Text("Actions") );
        Button newButton = new Button(new Icon(VaadinIcon.PLUS));
        newButton.setTooltipText("Add a new line");
        newButton.addClickListener(event -> {
            // Create a new expense line
            ExpenseLine newLine = new ExpenseLine("" );
            expenseLineEditDialog.editExpenseLine(newLine, expenseHeader, true);
        });

        actionsHeaderLayout.add(newButton);
        List<HeaderRow> headers = linesGrid.getHeaderRows();
        if (!headers.isEmpty())
            headers.getFirst().getCell(actionsColumn).setComponent(actionsHeaderLayout);

        linesGrid.addComponentColumn(line -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setTooltipText("Edit this expense line");
            editButton.addClickListener(e -> expenseLineEditDialog.editExpenseLine(line, expenseHeader,false));
            actionsLayout.add(editButton);
            actionsLayout.add(createRemoveButton(line));
            return actionsLayout;
        }).setHeader(actionsHeaderLayout).setWidth("150px").setFlexGrow(0);

        linesGrid.setSizeFull();
        add(linesGrid);
        setHeightFull();

    }

    private Button createRemoveButton(ExpenseLine line) {
        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.setTooltipText("Remove this expense line");
        removeButton.addClickListener(e -> ConfirmDeleteDialog.show(
                "Delete expense line", "This will permanently delete '" + line.getDescription() + "'",
                event -> removeExpenseLine(line) ));
        return removeButton;
    }

    private void removeExpenseLine(ExpenseLine line) {
        // Remove the expense line from the expense
        expenseHeader.deleteExpenseLine(line);
        // Update the grid
        linesGrid.setItems(expenseHeader.getExpenseLines());
        // Update the header section
        editBinder.readBean(expenseHeader);

        Notification.show("Expense '" + line.getDescription() + "' deleted", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
        linesGrid.setItems(expenseHeader.getExpenseLines());
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