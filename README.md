# video-rental-store
## Description
A system for managing rental administration of a video rental store. It offers the following functions:

* a film inventory
* rental price (and possible return surcharge) calculation based on the film type (new, regular, old)
* bonus points for customers for renting, also based on the film type

The system also keeps a history of all the rentals and returns.

## Requirements
* Java 8 or higher
* Internet connection to download dependencies
* a MySQL database which can be created using either MySQL or Docker

### MySQL
Assuming a MySQL server is running, type the following commands into the MySQL client:
```
create database event_store;
create user 'user'@'localhost' identified by 'password';
grant all on event_store.* to 'user'@'localhost';
```

### Docker
```
docker run --name video-rental-db -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=event_store -e MYSQL_USER=user -e MYSQL_PASSWORD=password -p 3306:3306 -d mysql:latest
```
If you are running Docker using Toolbox on Windows, you will have to change the localhost database addresses in the *application.properties* files of the microservices.

## Starting
After creating the database you start the application by starting each of the five microservices in the following directories:

* api-microservice
* customer-microservice
* film-microservice
* pricing-microservice
* rental-microservice

To start a single microservice, enter its directory and type:
```
./gradlew bootRun
```
The order of starting the microservices doesn't matter, but most of them will not work properly until the rental microservice has started because it creates the schema in the database.

## Assumptions
* Film type (new, regular or old) is a dynamic condition which is determined by the number of days that passed from a film's premiere. A new film becomes regular after 30 days from the premiere. It gets classified as old when it is at least 3650 days old.
* The system is for a physical video rental store, so when a rental of a film is made it is assumed that the customer brought it to the cashier or obtained it from them. Therefore, no film availability validation is done. However, the cashier has the ability to check the current availability of a film.
* The store allows to rent more than one copy of a film.
* The store allows to rent e.g., 1 copy of a film for 1 day and 1 copy of the same film for 2 days.
* When a new film is rented and returned late and on the return day the film type is already regular or old, the surcharge is applied as for a new film (the film type on the rental day is taken into consideration).

## API
The application exposes the following REST API at [http://localhost:8084/](http://localhost:8084/):

### Film inventory (queries, commands)
* **/films**
    - GET returns all films from the film inventory
    - POST adds a new film to the inventory
```
{
    "name": "Matrix",
    "premiere": "1999-08-13",
    "overallCount": 10
}
```
* **/films/{filmId}**
    - GET
    - PUT
    - PATCH
    - DELETE
* GET **/films/{filmId}/type** - returns the type of the film (new/regular/old)
* GET **/films/{filmId}/count** - returns the current count of available copies of the film
* GET **/films/search/findByNameStartsWith?name={someName}** - returns a list of films with names starting with someName

### Price calculations (queries)
* POST **/price-calculations/rentals** - calculates and returns the price the cashier can tell to a customer during an up front rental payment
```
{
    "rentals": [
        {"filmId": 1, "filmCount": 1, "days": 1},
        {"filmId": 2, "filmCount": 1, "days": 1}
    ],
    "filmIdsToTypes": {
        "1": "NEW"
    }
}
```
In case the *filmIdsToTypes* map is null, empty, or missing a filmId, the application will try to resolve the film type on its own.

Response:
```
{
    "price": 70,
    "messages": [
        "Film id: 1, days: 1, price for one: 40.0, count: 1, price for all: 40.0",
        "Film id: 2, days: 1, price for one: 30.0, count: 1, price for all: 30.0"
    ],
    "filmIdsToTypes": {
        "1": "NEW",
        "2": "OLD"
    }
}
```
*filmIdsToTypes* are returned because a rental command, which would usually follow this query, requires it. This is to make sure the customer gets the correct number of bonus points in case the price check is made at 23:59 and the rental command at 0:01 when in the meantime the type of film has changed from new to regular.

* POST **/price-calculations/returns** - calculates and returns the possible extra charge on return
```
{
    "customerId": 1,
    "filmIdsToCounts": {
        "1": 1,
        "2": 1
    }
}
```
*filmIdsToCounts* is how many copies of a film the customer brought to return.

Response:
```
{
    "price": 110,
    "messages": [
        "Film id: 1, extra days: 2, price for one: 80.0, count: 1, price for all: 80.0",
        "Film id: 2, extra days: 1, price for one: 30.0, count: 1, price for all: 30.0"
    ]
}
```
### Customer bonus points (queries)
* GET **/customers/{customerId}/points** - returns total amount of bonus points of the customer
* GET **/customers/{customerId}/points/history** - returns the history of customer's bonus points additions

### Rentals and returns (commands)
* POST **/rentals** - adds a rental record
```
{
    "customerId": 1,
    "rentals": [
        {"filmId": 1, "filmCount": 1, "days": 1},
        {"filmId": 2, "filmCount": 1, "days": 1}
    ],
    "price": 70,
    "filmIdsToTypes": {
        "1": "NEW",
        "2": "OLD"
    }
}
```
* POST **/returns** - adds a return record
```
{
    "customerId": 1,
    "filmIdsToCounts": {
        "1": "1",
        "2": "1"
    },
    "price": 110
}
```

## Architecture
### Event sourcing
The application (precisely, the rental microservice) saves all relevant events that take place in the rental store to the MySQL database which serves as an event store.

These events are:

1. Rental
2. Added bonus points for customer for rental
3. Return

The events are used by Pricing, Customer and Film Inventory microservices to apply their logic.

### Command-query separation
Rental price calculations are separated from the actual rentals and so are the returns and possible surcharge calculations. The calculations are queries to the system and both rentals and returns are commands to the system to save these events.

## Microservices
The application has three primary functions, all of which are centered around different things and each function has a dedidcated microservice.

There is also a rental microservice that saves rental and return events and an API gateway microservice.
### API Gateway
It is a *Spring Cloud Gateway* application that exposes the abovementioned endpoints and routes the requests to the corresponding microservices.

### Customer Points
A simple application, which, when asked for the bonus points of a particular customer, loads the historical records of the added bonus points for this customer and either returns the records or their sum (depending on the request).

### Film Inventory
It stores films in its own database which is a *H2* in-memory database that saves its state to a file.

Using *Spring Data* with *Spring Data REST* to easily expose all the necessary endpoints for a films collection and a film resource.

When the cashier queries the current number of the available copies of a particular film, the microservice gets the total number of its historically rented and returned copies from the event store and returns the result of the *overallCount - overallRented + overallReturned* calculation.

This microservice is also called by the Pricing microservice when Pricing needs to find out the type of a film. This endpoint can also be used by the cashier.

There is also an abovementioned search function, useful when a customer asks if the store has a particular film in the inventory. This call would usually be followed by the get current available copies count call.

Requests are validated and explanatory responses are returned in case of any validation problems.

### Pricing
#### Rental payment calculation
The microservice first performs a validation of the request and in case something's wrong, a thorough explanation of the encountered problems is returned.

Assuming the request passed the validation check, the microservice then checks if it was provided with the film type information for all the requested films to be priced. If not, for each film with an unknown film type it sends a request (asynchronously) to the film microservice.

In case something goes wrong there, it is deeply analyzed and an explanation of the problem is returned.

When everything is fine, the price is calculated and returned with the details of the calculation.

#### Return surcharge calculation
The request is first validated, just like for the rental calculation. If something is wrong, a response with explanation is returned.

If the request is fine, the microservice calculates if the customer should be surcharged and, if so, how much. If not, the returned price will be zero.

The calculation is based on the historical records of the customer renting the films they have brought to return. Of course, the return records have to be taken into consideration too to ensure proper calculations.

The response contains the calculation logic description as well.

### Rental
This microservice is used when the cashier wants to save a record of rental or return, along with the price paid. It creates and saves events mentioned above using *Spring Data JPA*.

An event is modelled as an object that gets transformed to a JSON string which is saved to the database. The record also has an id, a creation date and the event type specified, which simplifies queries.

The application ensures that the rental event and the added customer bonus points event are always both saved (so there is a rollback of one in case another fails to be saved).

## Technologies
*Spring Boot* is used for all the microservices and *Lombok* and *Guava* for most of them. The microservices that read from the event store use a *JdbcTemplate* instead of *Spring Data* to improve performance.
