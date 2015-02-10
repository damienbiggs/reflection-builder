# Reflection builder
Java library intended for testing. Creates an instance of any class with a valid value for each field.
The builder implements the apache commons builder interface.

e.g.
Example of usage
User testUser = ReflectionBuilder.aGenerated(User.class).build();
This will create a User that has a value set for every field.
E.g. username, address1, address2 etc are set
  
If user has a company object field, that will have values set as well.

Specific values can be set for fields that have a single field type using the helper methods.
belongingTo()
in()
ofType()
