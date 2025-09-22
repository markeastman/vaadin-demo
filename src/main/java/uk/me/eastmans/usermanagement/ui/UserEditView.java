package uk.me.eastmans.usermanagement.ui;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.security.User;
import uk.me.eastmans.usermanagement.UserService;

@Route("user-edit")
@RolesAllowed("USERS")
@PageTitle("User Edit")
class UserEditView extends Main {

    final UserService userService;
    final TextField username;
    final ViewToolbar viewToolbar;

    UserEditView(UserService userService) {
        this.userService = userService;

        username = new TextField();
        viewToolbar = new ViewToolbar("User Edit", ViewToolbar.group());
        add(viewToolbar);
        add(username);
    }

    public void editUser(User user) {
        if (user.getUsername() == null)
            viewToolbar.setTitle( "Create User");
        username.setValue(user.getUsername());
    }
}
