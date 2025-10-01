# vaadin-demo
Experiment with vaadin to test out certain features and the
aspect of components, dialogs and the ability to create decent
business applications.

To run the program just load the project into IntelliJ and click the execute button,
or from the command line use:
```./mvnw```

## Security
The security for the systems is based upon simple username and password 
authentication, but I want to test the ability for users to have personas
(similar to groups) and for them to select the persona they want to see
a menu for and work as. So a user could act as an administrator sometimes but generally as say a credit controller the rest of the time.
He can move between these two personas as he wants so as not to get flooded with menu
options for personas he uses rarely.

So a User can have many Personas and each persona has a set of roles that
someone with this persona can do. The roles govern the low level access to menu options
etc. The Persona is an application wide name and has a set of roles which anyone with that persona will 
be able to run as. The administrator can create and change the associations of Personas to roles
allowing them to create Personas that are meaningful to their business, but the underlying set
of roles is hard coded within the application.

For the demo application we have two deployed test users:
* username: admin, password: admin
* username: cc, password: cc

The admin user can act as an 'Administrator' persona and the cc user can act as either a
credit controller or a supplier manager. If the user switches between the personas that he has
available the application will rebuild the menu and provide the user with the appropriate
operations.

## CRUD Processing

For the Persona entity I have implemented a list based page which shows all the Personas
without any filters as there is not expected to be such a great number. Against
each row I have an edit button and a delete button. The delete button asks for confirmation 
and if accepted it will delete the Persona entity and update all the relevant
users. Within the header there is also a plus button to create a new Persona. Both 
the edit and the new should go to the same editing dialog.

I have used a binder for the name field but at the moment I have not implemented a binder for the 
Authority list, but I think we should do one. I have managed to get
validation working for the name field, but when I save to the database
I can also get errors such as unique key violation but at the moment I do not know
how to handle this error properly so I just throw up a general notification error.
In essence the CRUD processing is working and I can create new Persona entities, edit them and delete
them.

I have written the CRUD aspect for users using a full page view rather than a dialog.
This is working better I think than the dialog approach used by the persona edit. Within the view 
it can call a static method on the edit view to route to itself passing a parameter on the url

To help with more complex processing I have added an Expense entry.
This has a header and multiple lines. Each line can be a different
type of expense so we will need to have different panels for
each expense type.

# Things to do or investigate

1. Need to think about when to use dialogs for editing and 
when to use views. I think the view processing is better as it seems to have better 
layout control, button controls and navigation.
1. Need to look at how to handle errors, specifically the 
way we handle business errors and not just simply field errors.
1. We could do with looking at validation errors via annotations on the business entities.
2. Check to make sure we have the write stateless aspects
1. Need to work out how to display currency values with the 
thousands separator. Used to display expense header
2. Need to work out how to place a grid inside a flow layout so that the grid will have a reasonable size.