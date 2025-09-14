package uk.me.eastmans.suppliermanagement.ui;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;

@Route("supplier-list")
@RolesAllowed("SUPPLIERS")
@PageTitle("Supplier List")
@Menu(order = 3, icon = "vaadin:truck", title = "Supplier List")
class SupplierListView extends Main {

    SupplierListView() {
        add(new ViewToolbar("Supplier List"));

    }

}
