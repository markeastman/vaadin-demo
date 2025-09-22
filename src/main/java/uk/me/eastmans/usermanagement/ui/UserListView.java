package uk.me.eastmans.usermanagement.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.security.User;
import uk.me.eastmans.usermanagement.UserService;

import java.util.List;
import java.util.function.Consumer;

@Route("user-list")
@RolesAllowed("USERS")
@PageTitle("User List")
@Menu(order = 5, icon = "vaadin:user", title = "User List")
class UserListView extends Main {

    final UserService userService;
    final Grid<User> userGrid;
    final GridListDataView<User> dataView;

    UserListView(UserService userService) {
        this.userService = userService;

        userGrid = new Grid<>();
        Grid.Column<User> usernameColumn = userGrid.addColumn(User::getUsername);
        Grid.Column<User> personasColumn = userGrid.addColumn(User::getPersonas);
        Grid.Column<User> actionsColumn = userGrid.addColumn(new ComponentRenderer<>(user -> {
            if (user.isEnabled()) {
                return VaadinIcon.CHECK.create();
            } else {
                return new Span();
            }
        }));

        List<User> users = userService.getUsers();
        dataView = userGrid.setItems( users );
        UserFilter userFilter = new UserFilter(dataView);

        userGrid.getHeaderRows().clear();
        HeaderRow headerRow = userGrid.appendHeaderRow();
        headerRow.getCell(usernameColumn).setComponent(
                createFilterHeader("Username", userFilter::setUsername));

        HorizontalLayout actionsHeaderLayout = new HorizontalLayout();
        actionsHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        actionsHeaderLayout.add(new Text("Actions") );
        Button newButton = new Button(new Icon(VaadinIcon.PLUS));
        newButton.setTooltipText("Create a new User");
        newButton.addClickListener(event -> {
                // Create a new User
                createUser();
            });
        actionsHeaderLayout.add(newButton);
        headerRow.getCell(actionsColumn).setComponent(actionsHeaderLayout);

        userGrid.addComponentColumn(user -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setTooltipText("Edit this User");
            editButton.addClickListener(e ->
                    editButton.getUI().flatMap( ui ->
                            ui.navigate(UserEditView.class)).ifPresent(editor -> editor.editUser(user)));
            actionsLayout.add(editButton);
            actionsLayout.add(createRemoveButton(user));
            return actionsLayout;
        }).setHeader(actionsHeaderLayout).setWidth("150px").setFlexGrow(0);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("User List", ViewToolbar.group()));
        add(userGrid);
    }

    private Button createRemoveButton(User user) {
        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.setTooltipText("Remove this User");
        removeButton.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Are you sure?");
            dialog.setText("This will delete the User '" + user.getUsername() + "'");
            dialog.setCancelable(true);
            dialog.setCancelText("No");
            dialog.setConfirmText("Yes");
            dialog.addConfirmListener(event -> removeUser(user) );
            dialog.open();
        });
        return removeButton;
    }

    private void removeUser(User user) {
        // Remove the persona and update grid.
        userService.deleteUser(user);
        List<User> users = userService.getUsers();
        userGrid.setItems( users );
        dataView.refreshAll();
        Notification.show("User '"+ user.getUsername() + "' deleted", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void createUser() {
        // We need to create a user page
        User newUser = new User(null);
        getUI().flatMap( ui ->
                ui.navigate(UserEditView.class)).ifPresent(editor ->
                    editor.editUser(newUser));
    }

    private static Component createFilterHeader(String labelText,
                                                Consumer<String> filterChangeConsumer) {
        NativeLabel label = new NativeLabel(labelText);
        label.getStyle().set("padding-top", "var(--lumo-space-m)");
                //.set("font-size", "var(--lumo-font-size-xs)");
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

    private static class UserFilter {
        private final GridListDataView<User> dataView;

        private String username;

        public UserFilter(GridListDataView<User> dataView) {
            this.dataView = dataView;
            this.dataView.addFilter(this::test);
        }

        public void setUsername(String username) {
            this.username = username;
            this.dataView.refreshAll();
        }

       public boolean test(User user) {
            return matches(user.getUsername(), username);
       }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }
    }
}
