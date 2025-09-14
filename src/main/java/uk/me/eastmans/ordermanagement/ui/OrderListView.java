package uk.me.eastmans.ordermanagement.ui;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;

@Route("order-list")
@RolesAllowed("ORDERS")
@PageTitle("Order List")
@Menu(order = 0, icon = "vaadin:cash", title = "Order List")
class OrderListView extends Main {

    OrderListView() {
        add(new ViewToolbar("Order List"));

    }

}

