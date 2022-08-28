# LloydsTechTest

### by Tomasz Rykala

#### This Sample demonstrates a simple GitHub repository browser app.

It's written in Kotlin, Flow, Jetpack Compose, and is using GitHub's GraphQL endpoint.

____
### Characteristics

The Sample demostrates clear separation of concerns, by splitting Use Cases into dediccated classes, covered with Unit Tests.

It uses a Repository as its data source, which injected into the Use Case, streams the data as a Kotlin Flow.

The data becomes an observable State, the changes in which the UI reacts to, and recomposes as needed.

Benefitting from the Android's ViewModel characteristic, the UI survives Configuration Changes. 

When a Config Change occures while a network call is being made, that call is cancelled and restarted on start. (?)

After a call has been made, the call is not restarted, and the state is resumed.

**The Sample requires an API Token** to be added for the search function to work. Instructions provided below.
____
### Use instructions
1. Clone the repository and open in Android Studio
2. Generate a simple read-only Access Token, as described [HERE](https://docs.github.com/en/enterprise-server@3.4/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
3. Enter that token in *apikeys.properties* file.
4. Launch the app.

____

### Potential improvements

I. Repeated searches for the same term are re-launched. To combat this the following could be added:

- A Data Store layer in the Repository that stores the results for a period of time, until an agreed stale time is met.

- Proposed implementation: Room.

II. (...)
