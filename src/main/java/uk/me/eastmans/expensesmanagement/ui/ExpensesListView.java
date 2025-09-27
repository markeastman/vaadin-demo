package uk.me.eastmans.expensesmanagement.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ConfirmDeleteDialog;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.expensesmanagement.ExpenseHeader;
import uk.me.eastmans.expensesmanagement.ExpenseService;
import uk.me.eastmans.security.MyUserPrincipal;
import uk.me.eastmans.security.User;

import java.util.List;
import java.util.function.Consumer;

@Route("expense-list")
@RolesAllowed("EXPENSES")
@PageTitle("Expenses List")
@Menu(order = 1, icon = "vaadin:user-check", title = "Expenses List")
class ExpensesListView extends Main {

    final AuthenticationContext authenticationContext;
    final ExpenseService expenseService;
    final User user;
    final Grid<ExpenseHeader> expensesGrid;
    final GridListDataView<ExpenseHeader> dataView;

    ExpensesListView(AuthenticationContext authenticationContext,
                     ExpenseService expenseService) {

        this.authenticationContext = authenticationContext;
        if (authenticationContext.getAuthenticatedUser(MyUserPrincipal.class).isPresent())
            user = authenticationContext.getAuthenticatedUser(MyUserPrincipal.class).get().getUser();
        this.expenseService = expenseService;

        expensesGrid = new Grid<>();
        Grid.Column<ExpenseHeader> nameColumn = expensesGrid.addColumn(ExpenseHeader::getName);
        Grid.Column<ExpenseHeader> descriptionColumn = expensesGrid.addColumn(ExpenseHeader::getDescription);
        Grid.Column<ExpenseHeader> actionsColumn = expensesGrid.addColumn(new ComponentRenderer<>(header -> new Span()));
        List<ExpenseHeader> expenses = expenseService.listAll(user);
        dataView = expensesGrid.setItems( expenses );
        ExpenseHeaderFilter expenseFilter = new ExpenseHeaderFilter(dataView);

        expensesGrid.getHeaderRows().clear();
        HeaderRow headerRow = expensesGrid.appendHeaderRow();
        headerRow.getCell(nameColumn).setComponent(
                createFilterHeader("Name", expenseFilter::setName));
        headerRow.getCell(descriptionColumn).setComponent(
                createFilterHeader("Description", expenseFilter::setDescription));

        HorizontalLayout actionsHeaderLayout = new HorizontalLayout();
        actionsHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        actionsHeaderLayout.add(new Text("Actions") );
        Button newButton = new Button(new Icon(VaadinIcon.PLUS));
        newButton.setTooltipText("Create a new Expense");
        newButton.addClickListener(event -> {
            // Create a new User
            createExpense();
        });
        actionsHeaderLayout.add(newButton);
        headerRow.getCell(actionsColumn).setComponent(actionsHeaderLayout);

        expensesGrid.addComponentColumn(user -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setTooltipText("Edit this Expense");
            //editButton.addClickListener(event -> UserEditView.editUser(user.getId()));
            actionsLayout.add(editButton);
            actionsLayout.add(createRemoveButton(user));
            return actionsLayout;
        }).setHeader(actionsHeaderLayout).setWidth("150px").setFlexGrow(0);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Expenses List", ViewToolbar.group()));
        add(expensesGrid);
    }

    private Button createRemoveButton(ExpenseHeader expenseHeader) {
        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.setTooltipText("Remove this Expense");
        removeButton.addClickListener(e -> ConfirmDeleteDialog.show(
                "Delete Expense", "This will permanently delete '" + expenseHeader.getName() + "'",
                event -> removeExpense(expenseHeader)));
        return removeButton;
    }

    private void removeExpense(ExpenseHeader expenseHeader) {
        // Remove the persona and update grid.
        expenseService.deleteExpense(expenseHeader);
        expensesGrid.setItems(expenseService.listAll(user));

        Notification.show("Expense '" + expenseHeader.getName() + "' deleted", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void createExpense() {
        // We need to create a user page
        //UserEditView.editUser(0L);
    }

    private static Component createFilterHeader(String labelText,
                                                Consumer<String> filterChangeConsumer) {
        NativeLabel label = new NativeLabel(labelText);
        label.getStyle().set("padding-top", "var(--lumo-space-m)");
        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");
        textField.addValueChangeListener(
                e -> filterChangeConsumer.accept(e.getValue()));
        VerticalLayout layout = new VerticalLayout(label, textField);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");

        return layout;
    }

    private static class ExpenseHeaderFilter {
        private final GridListDataView<ExpenseHeader> dataView;

        private String name;
        private String description;

        public ExpenseHeaderFilter(GridListDataView<ExpenseHeader> dataView) {
            this.dataView = dataView;
            this.dataView.addFilter(this::test);
        }

        public void setName(String name) {
            this.name = name;
            this.dataView.refreshAll();
        }

        public void setDescription(String description) {
            this.description = description;
            this.dataView.refreshAll();
        }

        public boolean test(ExpenseHeader header) {
            return matches(header.getName(), name) && matches(header.getDescription(), description);
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }
    }

}