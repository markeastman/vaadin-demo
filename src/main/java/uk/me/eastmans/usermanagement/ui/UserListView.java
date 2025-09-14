package uk.me.eastmans.usermanagement.ui;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;

@Route("user-list")
@RolesAllowed("USERS")
@PageTitle("User List")
@Menu(order = 5, icon = "vaadin:user", title = "User List")
class UserListView extends Main {

    UserListView() {
        add(new ViewToolbar("User List"));
    }

}
