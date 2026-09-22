---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* maintains one user's local records of clients and prospects
* stores contact details, relationship status, tags, preferences, interests, important dates, and other concise relationship context
* records high-level business needs such as financial goals, priorities, budget ranges, and areas of interest
* records dated interactions, outcomes, next actions, and reminders
* finds, filters, sorts, reviews, archives, and backs up relationship records
* supports frequent users who prefer fast typed input while using the interface for clear feedback

**Value proposition**: Keeps track of their clients' details, allowing them to quickly recall important personal context about their clients, so that they can maintain more personalised and informed client relationships.



### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

#### First Use and Core Contact Records

| Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- |
| * | As a potential user exploring the app | view realistic sample client records | understand how the product supports relationship management before entering real data. |
| * | As a user ready to start using the app | remove all sample or experimental records | begin with a clean set of client data. |
| * * * | As a financial consultant | add a client or prospect with their basic contact details | keep a record of every professional relationship I manage. |
| * * * | As a financial consultant | view all active clients and prospects | see the relationships currently under my care. |
| * * | As a financial consultant preparing to contact someone | view one person's complete profile | recall the relevant relationship context before the conversation. |
| * * | As a financial consultant | update a person's contact details | keep the record accurate when their information changes. |
| * * * | As a financial consultant | delete a record that I created by mistake | prevent incorrect entries from cluttering my records. |

#### Relationship Context

| Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- |
| * * | As a financial consultant handling different kinds of relationships | assign one or more meaningful tags to a person | organise contacts according to how I work. |
| * * * | As a financial consultant managing a relationship pipeline | record whether a person is a prospect, active client, or inactive client | tell what kind of attention each relationship needs. |
| * * | As a financial consultant who relies on personal rapport | record a person's preferences and interests | make future conversations more relevant and personal. |
| * * | As a financial consultant | record important dates shared by a client | remember occasions that matter to the relationship. |
| * * | As a financial consultant | record a person's preferred contact channel and timing | approach them in a way that is convenient for them. |
| * * * | As a financial consultant | record a client's high-level financial needs, priorities, and areas of interest | continue the business discussion from the correct context. |

#### Interaction History

| Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- |
| * | As a financial consultant after speaking with a client | add a dated interaction note | preserve important details while they are still fresh. |
| * | As a financial consultant | record the type and outcome of an interaction | distinguish a meeting, call, message, or unsuccessful contact attempt. |
| * | As a financial consultant reviewing a relationship | view interaction notes in chronological order | understand how the relationship has developed over time. |
| * | As a financial consultant who entered an inaccurate interaction note | correct or remove that note | keep the relationship history trustworthy. |

#### Follow Ups and Reminders

| Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- |
| * * | As a financial consultant after an interaction | record the next action and its due date | turn the conversation into a concrete follow-up. |
| * * | As a financial consultant starting the workday | view follow-ups that are due or overdue | act on urgent relationships before they are forgotten. |
| * * | As a financial consultant who has completed a follow-up | mark the action as completed | distinguish finished work from work that still needs attention. |
| * * | As a financial consultant whose plan has changed | reschedule a follow-up | keep my next-action list realistic without losing the task. |
| * * | As a financial consultant managing recurring relationship events | set reminders for dates such as birthdays or renewal periods | maintain relationships and prepare for time-sensitive conversations. |

#### Retrieval and Prioritisation

| Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- |
| * * * | As a financial consultant looking for a specific person | find clients or prospects using one or more name keywords | quickly locate the relevant client record. |
| * * | As a financial consultant who remembers context rather than a name | search across preferences and notes using keywords | find the relevant relationship from the detail I remember. |
| * * | As a financial consultant focusing on a segment of contacts | filter people by relationship stage or tag | work with only the relevant group. |
| * * | As a financial consultant planning the order of work | sort contacts by next follow-up or last interaction date | prioritise relationships using timely information. |
| * | As a financial consultant maintaining long-term relationships | identify people I have not contacted recently | notice relationships that may otherwise be neglected. |
| * * | As a busy financial consultant about to meet or call someone | view a concise summary of their preferences, recent interactions, and next action | refresh my memory without reading the entire record. |

#### Long Term Use and Data Continuity

| Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- |
| * | As a long-time user | move inactive records out of my active view and restore them later | reduce clutter without permanently losing relationship history. |
| * * | As a financial consultant moving or safeguarding my local data | create a backup copy of my records | protect the relationship information I have accumulated. |
| * * | As a financial consultant recovering from data loss or changing computers | restore my records from a backup | resume work without rebuilding my relationship history. |


### Use cases

(For all use cases below, the **System** is the `AddressBook` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Delete a person**

**MSS**

1.  User requests to list persons
2.  AddressBook shows a list of persons
3.  User requests to delete a specific person in the list
4.  AddressBook deletes the person

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. AddressBook shows an error message.

      Use case resumes at step 2.

**Use case: Update a client relationship**

**MSS**

1. User requests to find a client or prospect by name.
2. AddressBook displays matching records.
3. User updates the person’s relationship stage, preferences, or contact details.
4. AddressBook saves the updated relationship context and displays a confirmation message.

   Use case ends.

**Extensions**

* 2a. No matching record is found.

  * 2a1. AddressBook shows a message that no matching records exist.

    Use case ends.

* 3a. The updated details are invalid.

  * 3a1. AddressBook shows an error message.

    Use case ends.

**Use case: Record a follow-up after a client interaction**

**MSS**

1. User requests to find a client or prospect.
2. ClientBook displays the matching record.
3. User records the outcome of the interaction.
4. User records the next action and its due date.
5. ClientBook saves the interaction details and follow-up.

    Use case ends.

**Extensions**

* 2a. No matching client or prospect is found.
  * 2a1. ClientBook shows a message that no matching record exists.
  * Use case ends.

* 4a. The supplied due date is invalid.
  * 4a1. ClientBook shows an error message.
  * Use case resumes at step 4.

**Use case: Add an important date**

**MSS**

1. User requests to add an important date to an existing client or prospect.
2. ClientBook identifies the specified client or prospect.
3. User provides the important date and its description.
4. ClientBook validates the supplied information.
5. ClientBook adds the important date to the person's record and displays a confirmation message.

   Use case ends.

**Extensions**

* 2a. The specified client or prospect does not exist.
    * 2a1. ClientBook shows an error message.
    * Use case ends.

* 3a. The required information is not supplied.
    * 3a1. ClientBook shows an error message.
    * Use case ends.

* 4a. The supplied date is invalid.
    * 4a1. ClientBook shows an error message.
    * Use case resumes at step 3.

**Use case: Convert a prospect to a client**

**MSS**

1. User issues a search command with name keywords to locate the target prospect.
2. ClientBook identifies the specified prospect.
3. User issues a conversion command specifying the target prospect's index number.
4. ClientBook verifies the contact is currently classified as a prospect and prompts the user to input mandatory client-specific onboarding details (e.g., billing address and tax ID).
5. User inputs the required onboarding details.
6. ClientBook validates the new information, changes the contact's classification from "prospect" to "client", saves the data, and displays a confirmation message.

   Use case ends.

**Extensions**

* 1a. The specified client or prospect does not exist.
    * 1a1. ClientBook shows an error message.
    * Use case ends.

* 3a. The user provides an invalid index number (e.g., out of bounds or non-numeric).
    * 3a1. System displays an error message stating the index is invalid.
    * Use case resumes at step 3.

* 4a. The selected contact is already classified as a client.
    * 4a1. System displays an error message stating the contact cannot be converted because they are already a client.
    * Use case ends.

* 5a. The user provides invalid formatting for the onboarding details (e.g., an alphanumeric string for a strictly numeric tax ID).
    * 5a1. System displays an error message detailing the specific parameter constraint and re-prompts the user for the details.
    * Use case resumes at step 5.

* 5b. The user inputs an abort command.
  * 5b1. System cancels the conversion process and leaves the contact as a prospect.
  * Use case ends.

*{More to be added}*

### Non-Functional Requirements

1. Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2. Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3. A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4. ClientBook should support all core client-record management features without requiring an Internet connection.
5. ClientBook should save all changes to client records to local storage so that the data is available after the application is closed and restarted.
6. The application must fail gracefully without crashing if the local storage file is missing, locked by another process, or corrupted via manual editing. It should present a clear error message to the user rather than freezing.
7. The graphical user interface must reflect the results of any command execution (e.g., adding a client, filtering the list, or deleting a record) within 500 milliseconds to maintain the perception of instantaneous feedback.
*{More to be added}*

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Prospect**: A person who is being tracked as a potential future client but is not currently an active client.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
