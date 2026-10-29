---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# ClientBook Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* Liu Junhan (`ZachL1113`) used OpenAI ChatGPT/Codex to assist with implementation, testing and documentation for
  `delete`, `next`, `due`, `done`, `reschedule`, `remind` and `unremind`, including the related bug fixes
  and the delete confirmation. Liu Junhan directed the work, reviewed the changes and tested them before submission.

* Koey Xian Han (`KoeyXianHan123`) used OpenAI ChatGPT and Codex to assist with implementation reviews, debugging,
  test generation, and documentation improvements for the `info` and `contact` features. This included assistance
  with parser validation, regression tests, User Guide and Developer Guide revisions, and the corresponding sequence
  diagrams. The resulting changes were reviewed and validated before submission.

* Phu Xien (`PhuXien`) used OpenAI Codex to assist with implementation reviews, test planning, and documentation
  improvements for the `find`, `tag`, and `tagdelete` features. This included assistance with auditing parser and
  predicate behavior, reviewing tag addition and confirmed deletion flows, revising the User Guide and Developer
  Guide, and refining the corresponding sequence diagrams. The resulting changes were reviewed and validated before
  submission.

* Nicholas Goh (**nicholas-gohh**) used Codex to assist with implementation reviews, test generation, documentation
  improvements, and debugging for the `add`, `edit`, and storage features. This included Developer Guide and User
  Guide revisions, parser validation, and regression testing. Nicholas reviewed and validated the changes before
  submission.

* Tan Wei Jie (`JokThreee`) used OpenAI [Codex](https://openai.com/codex/) to assist with reviewing the interaction-note
  implementation and revising its documentation, including the feature heading and manual testing instructions.

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

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-F14a-4/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-F14a-4/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows the deletion phase after the user enters `delete 1` and confirms with `Y`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F14a-4/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F14a-4/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F14a-4/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* displays the exact command submitted by the user above its success or error feedback in the `ResultDisplay`, wrapping
  long feedback to the available width.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F14a-4/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking an `add` command as an example.

<puml src="diagrams/AddSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the add command" />

<box type="info" seamless>

**Note:** The lifeline for `AddCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to a `ClientBookParser` object, which in turn creates a parser that matches the command (e.g., `AddCommandParser`) and uses it to parse the command.
2. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `AddCommand`) which is executed by the `LogicManager`.
3. The command can communicate with the `Model` when it is executed (e.g. to add a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
4. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

`LogicManager` executes each command against a temporary model initialized with the current records, preferences and active filter. When a command changes records, it saves the temporary client book before committing it to the main model. Read-only commands, such as `list`, `find`, and `due`, update the displayed view from memory without writing to storage. If saving fails, the main model is never committed, so the application does not display a change that would be lost after restart. Regression tests cover read-only commands avoiding persistence, a failed save retaining the records and active filter, and a subsequent successful `tag` command preserving that filter.

The sequence diagram below shows the corresponding flow for an `edit` command.

<puml src="diagrams/EditSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the edit command" />

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `ClientBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `ClientBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F14a-4/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the client book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* retains the complete list as the source of displayed indexes. Filtering and editing do not change a person's index;
  deleting a person removes it from the source list and renumbers the persons after it.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `ClientBook`, and each `Person` references tags from that list. This lets `ClientBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F14a-4/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both client book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonClientBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Find

The `find` command supports four independently optional filters: name keywords, one relationship, tag keywords, and
remembered-detail keywords. `FindCommandParser` tokenizes the optional `r/`, `t/`, and `k/` filters, rejects duplicate 
prefixes, and requires at least one filter. Name keywords form the preamble and must contain letters with optional 
internal hyphens or apostrophes, except for the accepted markers `s/o`, `d/o`, and `w/o`. Relationship and tag 
validation is delegated to `ParserUtil`; detail keywords accept any non-whitespace text.

The parser creates a `PersonMatchesFiltersPredicate`. Name keywords use case-insensitive contiguous substring
matching and are joined with `OR`. Thus, `Alex` matches `Alexander`, while either `Anne` or `Marie` matches
`Anne-Marie Tan`. Tag keywords use the same case-insensitive contiguous substring matching and are also joined with
`OR`; for example, `friend` matches both `friend` and `friends`. Detail keywords search the two contact preferences,
the three financial-information fields, each interaction note's date and text, and the follow-up action text and due
date. Dates use their ISO `yyyy-MM-dd` representation, so both full dates and contiguous fragments match. Follow-up
completion status, interaction type, and interaction outcome are excluded. Detail keywords are joined with `OR`. The
predicate joins the name, relationship, tag, and detail filter results with `AND`. For example,
`find Alex David r/client t/priority referral k/retirement WhatsApp` returns clients whose name contains `Alex` or
`David`, whose tags contain `priority` or `referral`, and whose remembered details contain `retirement` or `WhatsApp`.

The parsing rules are  summarised in the table below:
| Filter | Matching within the filter |
|:--|:--|
| Name | Case-insensitive contiguous match against names; any keyword matches. |
| Relationship | Exact `Relationship` equality. |
| Tag | Case-insensitive contiguous match against tag names; any keyword matches. |
| Remembered detail | Case-insensitive contiguous match against contact preferences, financial fields, interaction-note date/type/outcome/text, or follow-up action/due date; any keyword matches. |

The successful `CommandResult` returned by `FindCommand` includes a selection request. `MainWindow` responds by
calling `PersonListPanel#selectFirstPerson`, which selects the first filtered person and scrolls it into view, or
clears the selection if the result list is empty. `MainWindow` enables an explicit find-result navigation state only
when that selection request produces a selected person. The state is reset whenever another command is submitted,
including an invalid command, so selecting a person with the mouse or executing a non-find command does not activate
or retain find-result navigation. While the state is active, a key-event filter on `MainWindow` handles unmodified
<kbd>Up</kbd> and <kbd>Down</kbd> arrow keys and asks `PersonListPanel` to select the previous or next result and scroll
to that index. Modified arrow keys are left to the focused control. Selection remains at the corresponding boundary
when the first or last result is reached. Keeping this navigation at the window level allows it to work while the
command box retains keyboard focus.

The following sequence shows a combined search. The `FilteredList` applies the predicate to each source person;
omitted filter types evaluate as matched, while the supplied filter types must all match.

<puml src="diagrams/FindSequenceDiagram.puml" alt="Find by name, relationship, tags and details sequence" />

### Tagging a person

`TagCommandParser` reads the first whitespace-separated argument as the displayed-list index and validates each
remaining argument with `ParserUtil#parseTag(String)`. Tag names use the existing alphanumeric-only
`Tag` validation. It retains the parsed values long enough to detect repeated input before collecting them into a
set. Because `Tag` equality is case-insensitive, inputs such as `priority PRIORITY` raise a `ParseException` listing
the duplicate tags. `TagCommand` resolves the visible person against its index in the complete list using
`Command#getVisiblePersonAtIndex`. It then intersects the supplied and existing tag sets. A non-empty intersection
raises a `CommandException` listing the tags already on the contact and leaves the contact unchanged. Otherwise, the
command unions the supplied and existing tags and replaces the immutable `Person` through `Model#setPerson`,
preserving every non-tag field and the active list filter. The existing JSON adapter persists a successfully updated
set.

`AddCommandParser` reuses the same tag validation for repeatable `t/TAG` fields. `PersonCard` sorts the stored tags
before rendering them in its `FlowPane`. Both commands run on a temporary model, which is saved before its state is
committed to the live model.

`PersonCard` renders the complete tag set in its existing `FlowPane`, sorting tags alphabetically by name while
ignoring case. The list view observes the model, so tags supplied by `add` or appended by `tag` appear with the
person's contact and relationship details without a manual refresh. The `list` command restores all persons while
retaining those tags.

The following sequence shows tags being appended to a person in the currently displayed list. Parsing and index
validation complete before the immutable `Person` is replaced and the updated client book is saved.

<puml src="diagrams/TagSequenceDiagram.puml" alt="Tagging a person sequence" />

### Deleting tags from a person

`TagDeleteCommandParser` reads the displayed-list index and an optional set of tag names. An empty set means all
tags; otherwise, `TagDeleteCommand` requires every supplied tag to exist on the person. Matching uses the same
case-insensitive `Tag` equality as tag addition. Missing tags, a person with no tags, or an invalid displayed index
raises a `CommandException` before confirmation or mutation.

Tag deletion uses a two-phase Logic flow. The initial `Logic#execute(String)` call parses and validates the command,
stores the pending `TagDeleteCommand`, and returns a `CommandResult` containing `Are you sure you want to delete
this?`, the contact name, the exact stored tags that will be removed, and the accepted responses. `MainWindow`
displays the result through the existing result box below the command box.

`TagDeleteCommand` and `DeleteCommand` implement `ConfirmableCommand`, sharing one pending command in
`LogicManager`. Its `handleConfirmation` method uses the same response rules and invalid-response message
for both commands; each command supplies its validation/prompt and cancellation message. Tag deletion's
existing prompt, success messages and `Tag deletion cancelled.` feedback are unchanged.

While a deletion is pending, `LogicManager` interprets the next command-box input as a confirmation response instead
of parsing it as a new command. `y` and `yes` execute the stored command; `n` and `no` discard it. Matching is
case-insensitive and ignores surrounding whitespace. Any other response raises a `CommandException` and leaves the
command pending. A confirmed command copies the person's tag set, clears it or removes the selected tags, replaces
the immutable `Person` through `Model#setPerson`, and saves the updated client book.

The following sequence shows the confirmation gate before selected tags are deleted.

<puml src="diagrams/TagDeleteSequenceDiagram.puml" alt="Deleting tags with confirmation sequence" />

### Contact preferences (`contact`)

`ContactCommandParser` accepts `contact INDEX [ch/CHANNEL|-] [tm/TIMING|-]`. It requires a positive index
and at least one preference, and rejects duplicate prefixes. Prefixes are case-sensitive and begin after an
ordinary space. Other slash-containing text remains part of a value. The parser retains raw arguments so
only an exact `-`, padded with ordinary spaces, means clear. A tab-padded hyphen is normal text and can become
a stored `-` after trimming. `PreferredContactChannel` and `PreferredContactTiming` are immutable free-text
values with a 1–200 UTF-16-code-unit limit after surrounding whitespace is removed; internal text is preserved.
Unpaired UTF-16 surrogates are rejected to prevent replacement characters when saving UTF-8 JSON.
The top-level parser preserves raw `contact` arguments, including Unicode line separators, for preference validation.
Invalid values report `Preferred contact channel must contain 1 to 200 characters of valid Unicode text.` or
`Preferred contact timing must contain 1 to 200 characters of valid Unicode text.`, depending on the invalid field.

The parser also performs targeted, case-sensitive malformed-prefix detection after ordinary spaces:
`/ch` and `/tm` followed by whitespace, `/`, or the end of input, and the exact prefixes `cch/`, `chh/`,
`ttm/`, `tmm/` are rejected, including inside values. Index validation comes first, followed by duplicate checks
and malformed-prefix detection, so genuinely invalid indices retain their index error. Other slash-containing
text, such as `Phone/WhatsApp`, `AM/PM` or `xx/After 6 pm`, remains valid; arbitrary typo detection would
incorrectly reject free text.

`ContactDescriptor` distinguishes three states per preference: omitted (preserve), specified non-null (replace),
and specified null (clear). `ContactCommand` defensively copies the descriptor. It constructs a replacement
`Person` rather than using `Person#withContactPreferences`, whose null arguments mean preserve and therefore
cannot express clearing. All unrelated fields, including recurring reminders and interaction notes with their
recorded type and outcome, are copied unchanged.

`Command#getVisiblePersonAtIndex` first checks the index against the **complete** person list, selects that
person, and then checks membership in the filtered list. It does not interpret the index as a filtered-list
position. Thus a sole result labelled `2` requires index `2`; index `1` is rejected if that person is hidden.

Both `contact` and `info` use the existing `LogicManager` transaction flow. After parsing, it creates a temporary
`ModelManager` from the live client book and preferences, and applies the current filter and sort comparator.
The command replaces the person in this temporary model. If the data changed, `LogicManager` saves it through
`Storage` before committing the temporary client book, filter and comparator to the live model. Parse or command
failures leave the live model untouched; save failures prevent the commit. Unchanged data, including repeated clears, needs no save.
The sequence below shows a successful change; individual getters and replacement-person construction are omitted.

<puml src="diagrams/ContactSequenceDiagram.puml" alt="Contact preference update sequence" />

`JsonAdaptedPerson` persists optional `preferredContactChannel` and `preferredContactTiming` strings.
Missing or null properties load as absent, preserving compatibility with older files; invalid non-null values
are rejected. Clearing reuses this absence representation. The card hides absent preferences and wraps recorded
values. Parser, command and storage tests cover set/omit/clear combinations, filtered indices, preservation of
unrelated fields and save/reload compatibility.
`InfoContactReleaseAuditTest` also checks Unicode line separators through `LogicManager.execute`, rejection of
unpaired surrogates without changing the model or saved JSON, and Unicode preference round trips.

### Financial information (`info`)

`InfoCommandParser` accepts `info INDEX [fn/VALUE|-] [pr/VALUE|-] [ai/VALUE|-]`, requiring a positive index
and at least one field. Prefixes may appear in any order at whitespace boundaries; duplicates, slash-first
parameters (including the former `/fn`, `/pr`, `/ai` syntax), and reserved near misses are rejected.
Only lowercase `fn/`, `pr/`, `ai/` are valid. Their case variants and the explicit near misses `fnn/`, `prr/`,
`aii/` (also case-insensitive) produce an invalid-parameter error.

This targeted detection preserves financial notation such as `fn/Planning CPF/SRS` and `fn/Save $3000/month`.
A generic `NAME/` scan is confined to the preamble, where value text cannot occur; the preceding index is
validated before reporting an unknown parameter. Inside values, arbitrary forms such as `xyz/Value` remain
text because they are indistinguishable from ordinary slash-containing words. Recognised prefixes always
start fields: `fn/Review pr/High options` sets two fields. There is no quoting or escaping, so users must
rephrase literal prefix-like text. The UG documents this restriction and the limited typo detection.

`FinancialNeed`, `Priority` and `AreaOfInterest` are immutable, independently optional values on `Person`.
They accept Unicode letters, combining marks, decimal digits, ordinary spaces and `. , ' ( ) & - $ % + /`.
Validation trims only ordinary spaces, rejects blanks, controls and unsupported characters, and limits each
value to 200 UTF-16 code units. No Unicode normalization occurs; combining marks count separately.
`Priority` is free text rather than an enumeration. The parser reports all invalid supplied fields together
in financial-need, priority, area-of-interest order, before returning a command.

`InfoDescriptor` uses specified flags and nullable values to distinguish omission, replacement and clearing,
just as the contact descriptor does. Only a raw `-` surrounded by ordinary spaces clears a field.
`InfoCommand` copies the descriptor, resolves the original full-list index and checks visibility through
`Command#getVisiblePersonAtIndex`, then replaces the person with all unrelated fields preserved.
Recurring reminders and interaction-note metadata remain intact. `LogicManager` uses the same temporary-model,
save-before-commit flow described above; the active filter and sort comparator are retained.
The sequence below shows priority replacement and area-of-interest clearing in one command.

<puml src="diagrams/InfoSequenceDiagram.puml" alt="Financial information update sequence" />

`JsonAdaptedPerson` saves optional `financialNeed`, `priority` and `areaOfInterest` strings. Missing or null
properties load as absent, while non-null values are validated. Clearing therefore needs no JSON-format change.
Present values appear on the card; absent labels are hidden. Regression tests cover financial notation and Unicode,
validation and atomicity, descriptor states, original filtered indices, and persistence after setting or clearing.

<a id="dated-interaction-notes"></a>
<a id="dated-interaction-notes-us14"></a>

### Interaction notes

`note INDEX d/yyyy-MM-dd [t/TYPE] [o/OUTCOME] n/NOTE` appends an interaction note to the person at the displayed
client index. It works for clients and prospects, preserves the current filter, and does not replace earlier entries.

#### Model and command flow

1. `ClientBookParser` routes the command to `NoteCommandParser`. The parser reads the index, date and optional
   interaction metadata before the first `n/`, then treats the entire remaining body as text. URLs, slashes and further prefix-like strings remain note content.
2. `InteractionNote` stores an immutable `LocalDate`, nonblank text, `InteractionType` and `InteractionOutcome`. Date parsing checks the `yyyy-MM-dd` shape
   and uses strict ISO calendar validation. Past dates are allowed. Surrounding text whitespace is stripped;
   internal content and case are preserved without a character whitelist or maximum length.
3. `NoteCommand` checks the displayed index and rejects an exact duplicate in that person's history using
   `InteractionNote.hasSameContent()` (date and stripped text) before making changes. Otherwise, it copies the history, appends the new entry and calls
   `Model#setPerson` with a replacement `Person`. Other fields remain unchanged. The same date with different text,
   or the same text with different dates, is allowed. `Person` stably sorts its defensive copy by date, oldest first,
   preserving order for equal dates.
4. The observable list updates the corresponding `PersonListViewCell`. `LogicManager` saves the client book through
   the existing storage pipeline and returns `Added interaction note for NAME.` on success.

`Person#getInteractionNotes()` exposes an immutable defensive copy. History participates in `Person` equality and
hashing, but not duplicate identity detection. Both `EditCommand` and `InfoCommand` carry the existing history into
their replacement persons. `PersonBuilder` also preserves it when copying a person.

#### Persistence and display

`JsonAdaptedPerson` stores an `interactionNotes` array of `JsonAdaptedInteractionNote` objects, each containing
`date`, `text`, `type` and `outcome`. Missing or null arrays in older JSON files become empty histories.
Missing or null metadata fields default independently to `UNSPECIFIED`; unknown or blank stored enum values
raise `IllegalValueException`. Loading validates dates and note text; serialization preserves entry order and duplicates.

`PersonCard` displays dated, wrapped labels in the existing card's interaction-note container. Empty histories are
hidden and unmanaged. Labels have a zero minimum/preferred width and an unbounded maximum width so the parent
provides their available width during measurement. Their preferred width must not be bound to the card's actual
width: a newly replaced card has not yet been laid out, and that binding causes an excessive first wrapped-height
measurement. No fixed card height or forced list refresh is needed.

#### Validation and scope

The parser distinguishes missing index, date and note prefix; malformed calendar dates and blank text have specific
errors. Execution rejects indices outside the currently displayed list before changing the model.

Missing indices, dates or note prefixes produce an error. Dates such as `2026-02-30` are rejected with
`Interaction date must be a valid date in yyyy-MM-dd format.` Blank note text produces
`Interaction note cannot be empty.` An index outside the displayed list produces
`The person index provided is invalid.` These input errors leave the stored records unchanged.

Unsupported or empty `t/` values report
`Interaction type must be MEETING, CALL, MESSAGE, OTHER, or UNSPECIFIED.` Unsupported or empty `o/` values report
`Interaction outcome must be SUCCESSFUL, UNSUCCESSFUL, PENDING, or UNSPECIFIED.` Repeated header prefixes produce
the standard duplicate-prefix error. Metadata written after `n/` remains literal note text and does not set type
or outcome.

This feature provides adding, displaying, editing and deleting dated notes, with chronological sorting and
structured type/outcome metadata. Searching interaction notes (US16) remains outside this feature.

#### Interaction type and outcome (US15)

`InteractionType` contains `MEETING`, `CALL`, `MESSAGE`, `OTHER`, and `UNSPECIFIED`.
`InteractionOutcome` contains `SUCCESSFUL`, `UNSUCCESSFUL`, `PENDING`, and `UNSPECIFIED`.
The explicit unspecified states represent absent information without inventing historical interaction details.
Both the legacy two-argument `InteractionNote` constructor and old JSON use these defaults.
The four-argument constructor rejects null metadata. Full value equality and hashing include both fields,
so changes to metadata participate in person equality and persistence; duplicate-note detection deliberately
continues to compare only date and text, preserving the existing rule regardless of metadata.

`NoteCommandParser` retains the date-first syntax and splits at the first whitespace-delimited `n/`.
It tokenizes only the header with `PREFIX_DUE_DATE`, `PREFIX_INTERACTION_TYPE` (`t/`) and
`PREFIX_INTERACTION_OUTCOME` (`o/`), using the existing `ArgumentTokenizer` and duplicate-prefix validation.
Header whitespace is normalized for tokenization; the body is preserved independently.
The optional metadata fields can appear in either order after the date. Enum parsers strip surrounding
whitespace and use `Locale.ROOT` for case-insensitive input, returning field-specific validation errors.
The `t/` spelling is shared with tags but interpreted only by each command's own parser; `find` and tag commands
retain their existing meaning. Prefix-like strings after `n/` remain content, keeping URLs and old note bodies compatible.

`NoteCommand` still appends through immutable person replacement and the existing `LogicManager` transaction.
`JsonAdaptedInteractionNote` writes uppercase enum names, accepts case-insensitive stored values, and preserves
its two-argument constructor for existing callers. No file migration or dependency is required. Missing/null
fields are backward compatible; invalid supplied values are rejected rather than silently discarded.
`PersonCard` already renders each note through `InteractionNote.toString()`, which now produces
`DATE [Type: TYPE; Outcome: OUTCOME]: TEXT`; wrapping, numbering and chronological ordering are unchanged.
`NoteEditCommand` copies the selected note's metadata when replacing date or text. Deletion removes only the
selected note; `edit`, `info`, contact preferences, tags and follow-up mutations carry the immutable history through.
`noteedit` does not expose metadata editing; its existing date/text syntax remains unchanged.

Enums were chosen over free text to enforce a small, consistent vocabulary. `UNSPECIFIED` was chosen over
`OTHER`/`PENDING` defaults because those values assert details the user may never have recorded. Full value equality
is separate from the date/text duplicate rule to avoid both missed persistence changes and duplicate-rule regressions.
Keeping metadata before `n/`, rather than scanning the entire command, protects existing free-form note content.

Tests cover all enum values, mixed-case parsing under a Turkish default locale, independent optional fields,
malformed/missing/duplicate prefixes, literal prefixes in the body, explicit defaults, invalid/null values,
JSON round-trips and legacy fields. Logic/storage integration tests exercise original filtered indexes,
chronological ties, unchanged duplicate rejection, note editing/deletion, unrelated contact mutations,
backup/restore and reopening saved records. JavaFX tests assert the exact canonical and unspecified display text.

#### Editing note date and text

`noteedit CONTACT_INDEX NOTE_INDEX [d/DATE] [n/NEW_NOTE_CONTENT]` is routed to `NoteEditCommandParser`.
The parser requires two positive indices and at least one editable field. When supplied, the date comes before
`n/` and is calendar-validated; explicitly supplied future dates are rejected during execution. As with `note`,
everything after `n/` is treated as note content, including further prefix-like strings.

Missing arguments, missing both editable fields, invalid indices, invalid dates, and blank supplied text produce an
error without changing records. Invalid dates produce `Interaction date must be a valid date in yyyy-MM-dd format.`
An out-of-range contact index produces `The person index provided is invalid.` An out-of-range note index, including
when the contact has no notes, produces `The interaction note index provided is invalid.` Blank text produces
`Interaction note cannot be empty.`

`NoteEditCommand` resolves the contact against the filtered list and the note against its chronologically ordered
history, using one-based indices. It rejects out-of-range indices (including empty histories) before mutation.
It copies the history and replaces just the selected entry with an `InteractionNote` containing the supplied
date/text (preserving omitted values), original type and original outcome. Date changes trigger chronological reordering.
It then calls `Model#setPerson` with a replacement person preserving all other fields, including contact preferences
and the next action. The current filter remains active. `LogicManager` persists the change through the existing
JSON pipeline; no additional schema changes or note IDs are needed for editing.

`NoteEditCommandTest` and `NoteEditCommandParserTest` cover validation, filtered lists, duplicate notes,
date and contact-field preservation, command routing, and saving/reloading an edit through `LogicManager`.

#### Deleting a note

The following sequence diagram shows the successful execution of `notedelete 1 2`, which deletes the
second interaction note from the first contact in the currently filtered list.

<puml src="diagrams/NoteDeleteSequenceDiagram.puml" alt="Interaction note deletion sequence" />

`notedelete CONTACT_INDEX NOTE_INDEX` is routed to `NoteDeleteCommandParser`, which requires exactly two
positive integer indices using `ParserUtil.parseIndex`.

Missing or extra arguments and invalid indices produce an error without changing records. An out-of-range contact
index produces `The person index provided is invalid.` An out-of-range note index, including when the contact has
no notes, produces `The interaction note index provided is invalid.`

`NoteDeleteCommand` resolves the contact against the currently filtered list and the note against that
contact's chronologically ordered history. Both indices are one-based. Out-of-range indices, including an empty
history, are rejected before changing the model. The command copies the history and removes the selected
entry by position, preserving duplicates and the order of remaining entries. Subsequent note positions shift
down by one. It calls `Model#setPerson` with a replacement person preserving all other fields, including
contact preferences and the next action, and retains the current filter.

`PersonCard` prefixes each note label with its one-based chronological position, matching command indices.
Replacing the person rebuilds the card through the existing observable list, so remaining notes are renumbered
and an empty history is hidden without forcing a refresh or changing stored notes.

Deletion feedback uses the `InteractionNote` returned by positional removal, including its actual date and text.
Only the feedback excerpt has whitespace collapsed. It is limited to 80 Unicode code points using
`offsetByCodePoints`, followed by `…` when truncated, without splitting surrogate pairs or changing stored content.
Command and UI tests cover excerpt boundaries, supplementary Unicode characters, duplicate notes, chronological
order, automatic renumbering and hiding the last deleted note's history.

`LogicManager` saves the replacement through the existing JSON pipeline. No storage schema or model API
changes are required. `NoteDeleteCommandTest` and `NoteDeleteCommandParserTest` cover validation, filtered
lists, duplicate notes, contact-field preservation, command routing, and saving/reloading deletions,
including an empty history.

Automated and manual regression checks are described in the [Testing guide](Testing.md#us14-interaction-note-regression-checks).

### Follow-ups

Each `Person` has zero or one immutable `NextAction` containing a description, a `LocalDate` due date
and a completion flag. `Person#getNextAction()` exposes absence as an empty `Optional`;
`Person#withNextAction` replaces the action while preserving unrelated fields. The value participates in
full equality and hashing, but not duplicate identity detection.

All indexed follow-up commands resolve people through `Command#getVisiblePersonAtIndex`: the index is the
original one-based position in the complete client book, and that person must also be visible under the
current filter. Filtering and due-date sorting do not renumber contacts. Invalid or hidden indices report
`The person index provided is invalid.`. Rejected commands leave data, the active filter and sorting unchanged.

`next` and `reschedule` share description and date validation: descriptions contain 1–200 Java string
characters after surrounding whitespace is stripped; dates use exactly `yyyy-MM-dd`, strict calendar
validation and years 0001–9999. At execution, both commands reject a requested date before
`LocalDate.now(clock)` with `Follow-up date cannot be before today.`. Their default clock uses
`Clock.systemDefaultZone()`; tests inject fixed clocks. Calendar parsing, model construction and storage
still accept historical dates so saved follow-ups remain loadable as they become overdue.

The parsers use `ParserUtil#parseIndex` and `ArgumentTokenizer`. Space-prefixed `a/` and `d/` delimit
fields in commands that recognise them; they cannot appear literally within an action description.
Other text, including URLs, is accepted. Repeated recognised prefixes produce
`Multiple values specified for the following single-valued field(s): ` followed by the duplicated prefixes.
The error tables below cover command-specific failures; `NAME`, `LABEL` and `N` stand for actual values.

#### Recording or replacing an action

`ClientBookParser` routes `next INDEX a/ACTION d/yyyy-MM-dd` to `NextCommandParser`. It validates the
index, duplicate prefixes, required fields, calendar date and description before constructing `NextCommand`.
The command checks visibility and the execution-date rule above, then calls `Model#setPerson` with
`Person#withNextAction`. Creation and replacement both set `completed = false`, including when replacing
completed work. Success reports `Next action for NAME has been updated successfully.`.

##### Validation errors

Condition | Error message
----------|--------------
Missing index | `Person index is required.`
Zero, negative, non-integer, overflowing or multiple indices | `Index must be a positive integer.`
Missing `a/` | `Next action is required: use a/ACTION.`
Missing or blank `d/` | `Due date is required: use d/yyyy-MM-dd.`
Blank or overlong action | `Next action must contain 1 to 200 characters.`
Invalid calendar date, format or year | `Due date must be a valid date in yyyy-MM-dd format.`

The shared visibility, past-date and duplicate-prefix checks above also apply.

<puml src="diagrams/NextSequenceDiagram.puml" alt="Next command sequence diagram" />

Diagram source: [NextSequenceDiagram.puml](diagrams/NextSequenceDiagram.puml).
The diagram shows the successful path with a date that is today or later.

#### Completing an action

`ClientBookParser` routes `done INDEX` to `DoneCommandParser`, which validates the entire argument as one
index. `DoneCommand` checks visibility, then the presence of an action. `NextAction#markCompleted()` keeps
the description and date and sets `completed = true`; `Person#withNextAction` and `Model#setPerson` preserve
all other fields. Success reports `Next action for NAME has been marked as completed.`.
An already completed action returns `Next action for NAME is already completed.` without mutation.
Yearly reminders are unaffected. The current filter and comparator remain active; in the due view, a person
stays visible only if another qualifying event remains.

##### Validation errors

Condition | Error message
----------|--------------
Missing index | `Person index is required.`
Zero, negative, non-integer, overflowing or multiple indices | `Index must be a positive integer.`
No next action after visibility validation | `NAME has no next action to complete.`

#### Rescheduling existing follow-ups

`ClientBookParser` routes `reschedule INDEX d/yyyy-MM-dd [a/ACTION]` to `RescheduleCommandParser`.
The parser reuses the shared date and description validation; omitting `a/` keeps the existing description.
After parsing, `RescheduleCommand` checks visibility, the presence of an action, then the execution-date rule,
in that order. `NextAction#reschedule` creates a pending copy, reopening completed work, and
`Person#withNextAction` and `Model#setPerson` preserve all other fields.
Success reports `Follow-up for NAME has been rescheduled successfully.`.

The active filter and comparator are retained and re-evaluated. Moving an action into the future removes
its person from `due` only if no upcoming yearly reminder still qualifies; moving it to today makes the
action qualify. Changing the date can also reorder the due view without changing full-list indices.

##### Validation errors

Condition | Error message
----------|--------------
Missing index | `Person index is required.`
Zero, negative, non-integer, overflowing or multiple indices | `Index must be a positive integer.`
Missing or blank `d/` | `Due date is required: use d/yyyy-MM-dd.`
Supplied action is blank or overlong | `Next action must contain 1 to 200 characters.`
Invalid calendar date, format or year | `Due date must be a valid date in yyyy-MM-dd format.`
No follow-up after visibility validation | `NAME has no follow-up to reschedule. Use next to create one.`

The shared visibility, past-date and duplicate-prefix checks above also apply.

#### Yearly recurring reminders

`ClientBookParser` dispatches `remind INDEX a/LABEL d/MM-dd` to `RemindCommandParser` and
`unremind INDEX a/LABEL` to `UnremindCommandParser`. Both validate the index before duplicate and required
prefix checks. `RecurringReminder#parseLabel` strips surrounding whitespace and requires 1–50 Java string
characters without ISO control characters or slashes. `RecurringReminder#parseDate` requires a real
`MonthDay` written as exactly `MM-dd`; February 29 is accepted. No year or general recurrence rule is stored.
`unremind` recognises only `a/`: a trailing `d/` becomes part of the label and fails slash validation.

After visibility validation, `RemindCommand` uses `Person#withRecurringReminder` and `Model#setPerson`.
A case-insensitive label match updates the date in place while retaining the stored display spelling;
another label appends a reminder. Success reports `Yearly reminder for NAME has been updated successfully.`.
`UnremindCommand` finds the label ignoring case, then uses `Person#withoutRecurringReminder` and
`Model#setPerson`. Success reports `Yearly reminder "LABEL" for NAME has been removed successfully.`,
using the stored label spelling. Both commands preserve unrelated fields, the filter and sorting.
Removing the last qualifying reminder hides a person from `due` unless a due next action still qualifies.

`equalsIgnoreCase` handles lookup; `TreeSet(String.CASE_INSENSITIVE_ORDER)` enforces unique labels in the
model and storage. Display spelling participates in reminder value equality. `Person` defensively copies
its reminder list and includes it in equality, hashing and all copy paths.

`RecurringReminder#nextOccurrence(today)` uses this year's occurrence, or next year's if it has passed.
`MonthDay#atYear` maps February 29 to February 28 in non-leap years. Past annual occurrences do not
accumulate as overdue reminders. `PersonCard` shows `Yearly | YYYY-MM-DD: LABEL`; `done` affects only
the one-off action.

##### Validation errors

Condition | Error message
----------|--------------
Missing, zero, negative, non-integer, overflowing or multiple indices | `Index must be a positive integer.`
`remind` missing either required prefix, after index/duplicate checks | `remind INDEX a/LABEL d/MM-dd`
`unremind` missing `a/`, after index/duplicate checks | `unremind INDEX a/LABEL`
Blank, overlong or invalid label | `Reminder label must contain 1 to 50 characters, without control characters or slashes.`
Blank, invalid or incorrectly formatted yearly date | `Yearly date must be a valid date in MM-dd format.`
`unremind` label has no match after visibility validation | `NAME has no yearly reminder matching label "LABEL".`

The shared visibility and duplicate-prefix checks above also apply. An empty `d/` does not delete a reminder.

#### Viewing due actions

`ClientBookParser` routes argument-free `due` to `DueCommand`. Each execution captures
`LocalDate.now(clock)` and supplies a new `NextActionDuePredicate` and comparator to
`Model#updateFilteredPersonList`, replacing any previous filter. A person qualifies once if they have an
incomplete next action dated today or earlier, or a yearly reminder whose next occurrence falls 0–30 days
from the captured date, inclusive. Completed and future one-off actions do not qualify on their own.
The default clock uses the system's local zone; rerunning `due` refreshes the captured date after midnight.

Results sort by the earliest qualifying action or reminder date ascending, so overdue actions come first.
Ties use name ignoring case, then exact spelling. `ModelManager` exposes a `SortedList` over its
`FilteredList`; sorting changes neither stored contact order nor the original indices used by commands.
Replacing a person re-evaluates membership and order. `list` and `find` clear the comparator and return
to the original contact order.

Nonempty results report `N person(s) with due follow-ups or yearly reminders in the next 30 days.`;
empty results report `No follow-ups are due and no yearly reminders are upcoming.`.

##### Validation errors

Condition | Error message
----------|--------------
Any arguments to `due` | `The due command takes no arguments.`

#### Persistence and card display

`LogicManager` executes commands on a temporary model carrying the active predicate and comparator.
Changed data is saved before the temporary state is committed to the live model; a save failure leaves
the live state unchanged. Commands do not write follow-up files themselves.

`JsonAdaptedPerson` stores optional `nextAction` through `JsonAdaptedNextAction`:

```json
"nextAction": {
  "action": "Send retirement proposal",
  "dueDate": "2026-11-02",
  "completed": false
}
```

Missing or null `nextAction` loads as absence. Invalid or missing action/date/completion values are rejected
through `IllegalValueException`. Both pending and completed actions survive a JSON round trip.
`JsonAdaptedRecurringReminder` stores each reminder as `{ "label": "Birthday", "date": "02-29" }` in
`recurringReminders`. A missing, null or empty array loads as an empty list; null elements and duplicate
labels, including case variants, are rejected. The existing backup/restore path preserves both fields.

`PersonCard` hides and unmanages absent follow-ups. Otherwise, completed status takes precedence;
incomplete dates earlier than today show `Overdue`, and remaining actions show `Pending`. Long text wraps.
Status and reminder next occurrences are evaluated when a card is constructed; there is no midnight timer.
Restarting refreshes all cards after a date change.

#### Validation and regression tests

Parser and command tests cover missing/duplicate prefixes, index boundaries and hidden contacts,
blank/overlong descriptions and labels, strict dates, leap days, local-zone boundaries, past-date rejection,
replacement and rescheduling resets, repeated completion, case-insensitive reminder updates/deletion,
field preservation and unchanged data on errors. Model, JSON and integration tests cover old files,
invalid stored values, round trips, historical overdue actions, the inclusive reminder window and year
rollover. `DueSortingIntegrationTest` verifies date/name ties, original indices, stored order and live
membership/order updates through logic and storage. Card tests cover status display and wrapping.

### Backing up and restoring records

`ClientBookParser` accepts `backup` and `restore` only without arguments and creates a `BackupCommand` or `RestoreCommand` respectively. Both commands derive the backup path from the main data-file path, placing `clientbook-backup.json` beside `clientbook.json` in the `data` folder.

`BackupCommand` does not change the in-memory client book. It serializes the current records through `Storage#saveClientBook(ReadOnlyClientBook, Path)` to a temporary file in the backup folder, then replaces the backup file with an atomic move when supported. It falls back to a normal replacement move on file systems that do not support atomic moves. This prevents a failed write from replacing an existing backup with a partial file.

`RestoreCommand` first reads and validates the backup through `Storage#readClientBook(Path)`. Only after a valid `ReadOnlyClientBook` is available does it save those records to the main data file, replace the temporary model's client book, and reset the active filter to show all persons. It marks the result as already saved so `LogicManager` does not write the restored records again. A missing, invalid, or unreadable backup leaves the in-memory records unchanged.

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
| * * * | As a financial consultant | add a client or prospect with their basic contact details | keep a record of every professional relationship I manage. |
| * * * | As a financial consultant | view all active clients and prospects | see the relationships currently under my care. |
| * * | As a financial consultant | update a person's contact details | keep the record accurate when their information changes. |
| * * * | As a financial consultant | delete a record that I created by mistake | prevent incorrect entries from cluttering my records. |

#### Relationship Context

| Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- |
| * * | As a financial consultant handling different kinds of relationships | assign one or more meaningful tags to a person | organise contacts according to how I work. |
| * * * | As a financial consultant managing a relationship pipeline | record whether a person is a prospect or an active client | tell what kind of attention each relationship needs. |
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
| * * | As a financial consultant focusing on a segment of contacts | filter people by client or prospect relationship status | work with only the relevant group. |
| * | As a financial consultant maintaining long-term relationships | identify people I have not contacted recently | notice relationships that may otherwise be neglected. |

#### Long Term Use and Data Continuity

| Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- |
| * * | As a financial consultant moving or safeguarding my local data | create a backup copy of my records | protect the relationship information I have accumulated. |
| * * | As a financial consultant recovering from data loss or changing computers | restore my records from a backup | resume work without rebuilding my relationship history. |
| * * * | As a long-term user | have my data saved between sessions | continue managing my information without having to re-enter it each time. |

### User Stories to be implemented in the future

| Priority | As a … | I want to … | So that I can … |
| --- | --- | --- | --- |
| * * | As a financial consultant preparing to contact someone | view one person's complete profile | recall the relevant relationship context before the conversation. |
| * * | As a financial consultant who relies on personal rapport | record a person's preferences and interests | make future conversations more relevant and personal. |
| * * | As a financial consultant | record important dates shared by a client | remember occasions that matter to the relationship. |
| * * | As a busy financial consultant about to meet or call someone | view a concise summary of their preferences, recent interactions, and next action | refresh my memory without reading the entire record. |
| * * | As a financial consultant planning the order of work | sort contacts by next follow-up or last interaction date | prioritise relationships using timely information. |

### Use cases

(For all use cases below, the **System** is `ClientBook` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Delete a person**

**MSS**

1.  User requests to list persons
2.  ClientBook shows a list of persons
3.  User requests to delete a specific person in the list
4.  ClientBook shows the person's name and asks for confirmation
5.  User confirms with Y or Yes
6.  ClientBook deletes the person

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. ClientBook shows an error message.

      Use case resumes at step 2.

* 5a. User enters N or No.

    * 5a1. ClientBook cancels deletion without changing the records.

      Use case ends.

* 5b. User enters any other response.

    * 5b1. ClientBook asks for Y/Yes or N/No and keeps the deletion pending.

      Use case resumes at step 5.

**Use case: Update a client relationship**

**MSS**

1.  User requests to find a client or prospect by name.
2.  ClientBook displays matching records.
3.  User updates the person’s relationship stage, preferences, or contact details.
4.  ClientBook saves the updated relationship context and displays a confirmation message.

    Use case ends.

**Extensions**

* 2a. No matching record is found.

    * 2a1. ClientBook shows a message that no matching records exist.

      Use case ends.

* 3a. The updated details are invalid.

    * 3a1. ClientBook shows an error message.

      Use case ends.

**Use case: Record a follow-up after a client interaction**

**MSS**

1.  User requests to find a client or prospect.
2.  ClientBook displays the matching record.
3.  User records the outcome of the interaction.
4.  User records the next action and its due date.
5.  ClientBook saves the interaction details and follow-up.

    Use case ends.

**Extensions**

* 2a. No matching client or prospect is found.

    * 2a1. ClientBook shows a message that no matching record exists.

      Use case ends.

* 4a. The supplied due date is invalid.

    * 4a1. ClientBook shows an error message.

      Use case resumes at step 4.

**Use case: Add an important date**

**MSS**

1.  User requests to add an important date to an existing client or prospect.
2.  ClientBook identifies the specified client or prospect.
3.  User provides the important date and its description.
4.  ClientBook validates the supplied information.
5.  ClientBook adds the important date to the person's record and displays a confirmation message.

    Use case ends.

**Extensions**

* 2a. The specified client or prospect does not exist.

    * 2a1. ClientBook shows an error message.

      Use case ends.

* 3a. The required information is not supplied.

    * 3a1. ClientBook shows an error message.

      Use case ends.

* 4a. The supplied date is invalid.

    * 4a1. ClientBook shows an error message.

      Use case resumes at step 3.

**Use case: Convert a prospect to a client**

**MSS**

1.  User issues a search command with name keywords to locate the target prospect.
2.  ClientBook identifies the specified prospect.
3.  User issues a conversion command specifying the target prospect's index number.
4.  ClientBook verifies the contact is currently classified as a prospect and prompts the user to input mandatory client-specific onboarding details (e.g., billing address and tax ID).
5.  User inputs the required onboarding details.
6.  ClientBook validates the new information, changes the contact's classification from "prospect" to "client", saves the data, and displays a confirmation message.

    Use case ends.

**Extensions**

* 1a. The specified client or prospect does not exist.

    * 1a1. ClientBook shows an error message.

      Use case ends.

* 3a. The user provides an invalid index number (e.g., out of bounds or non-numeric).

    * 3a1. ClientBook displays an error message stating the index is invalid.

      Use case resumes at step 3.

* 4a. The selected contact is already classified as a client.

    * 4a1. ClientBook displays an error message stating the contact cannot be converted because they are already a client.

      Use case ends.

* 5a. The user provides invalid formatting for the onboarding details (e.g., an alphanumeric string for a strictly numeric tax ID).

    * 5a1. ClientBook displays an error message detailing the specific parameter constraint and re-prompts the user for the details.

      Use case resumes at step 5.

* 5b. The user inputs an abort command.

    * 5b1. ClientBook cancels the conversion process and leaves the contact as a prospect.

      Use case ends.

**Use case: Find clients or prospects**

**MSS**

1.  User requests to find clients or prospects using one or more name, relationship, tag, or remembered-detail
    filters.
2.  ClientBook searches the stored records using the supplied criterion.
3.  ClientBook displays all matching clients and prospects.

    Use case ends.

**Extensions**

* 1a. The user does not provide a search criterion.

    * 1a1. ClientBook displays an error message explaining the valid command formats.

      Use case resumes at step 1.

* 1b. The user provides an unsupported relationship value.

    * 1b1. ClientBook displays an error message explaining the valid relationship values.

      Use case resumes at step 1.

* 1c. The user combines two or more filter types.

    * 1c1. ClientBook requires each result to match every supplied filter type.

      Use case resumes at step 2.

* 3a. No record matches the supplied criterion.

    * 3a1. ClientBook informs the user that no matching record was found.

      Use case ends.

### Non-Functional Requirements

1. ClientBook should work on any _mainstream OS_ as long as Java `25` or above is installed.
2. ClientBook should be able to store at least 1000 persons.
3. For a data set of up to 1000 persons, ClientBook should process a command and update the graphical user interface within 1 second after the user submits the command.
4. ClientBook should start within 10 seconds on a computer that meets the stated software requirements.
5. A user with above-average typing speed for regular English text should be able to complete most core tasks faster using commands than using the mouse.
6. All core client-record management features should remain available without an Internet connection.
7. Every successful command that modifies client data should be saved automatically to local storage and remain available after ClientBook is restarted.
8. An invalid command should not modify any existing client data.
9. ClientBook should handle a missing, inaccessible, or corrupted data file without crashing or freezing, and should inform the user when recovery is required.
10. ClientBook should not silently overwrite an unreadable or corrupted data file.
11. If ClientBook cannot save a change to local storage, it should inform the user that the change may not persist.
12. ClientBook should store client data locally and should not transmit it to external services.
13. ClientBook should not require an online account or authentication credentials to access locally stored records.
14. All core client-record management operations should be accessible using keyboard input.
15. Error messages should identify the invalid input and provide sufficient information for the user to correct it.
16. ClientBook should be distributed as a single executable JAR file that does not require a separate installation process.
17. ClientBook should store its data in a human-readable JSON format to support inspection and manual backup.
18. The codebase should comply with the project coding standard and pass the configured automated checks before changes are merged.
19. Unexpected errors should be logged with sufficient diagnostic information for developers while avoiding unnecessary exposure of client information.
20. ClientBook should support a single user only; its data file should not be accessed concurrently or shared during normal operation.
21. ClientBook should read and write files only within its home folder (the folder containing the JAR file) and its subfolders,
    and should not change OS-level configuration settings.
22. The graphical user interface should work well at resolutions of 1920x1080 and above at 100% and 125% screen scaling, and
    should remain usable at resolutions of 1280x720 and above at 150% screen scaling.
23. The distributable JAR file should not exceed 100 MB.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Prospect**: A person who is being tracked as a potential future client but is not currently an active client.
* **Client**: A person with whom the financial consultant has an active professional relationship.

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

   2. Run the following command:

      ```sh
      java -jar clientbook.jar
      ```

      Expected: The GUI opens without any contacts. The window size may not be optimal.

2. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   2. Run the following command:

      ```sh
      java -jar clientbook.jar
      ```

      Expected: The most recent window size and location are retained.

### Adding a client/prospect

Use a separate test folder with fictional records.

1. Adding valid records

    1. Prerequisites: Start with an empty client book, or run `clear`.

    2. Test case: `add r/Client n/John Doe e/john@example.com p/91234567 t/referral`<br>
       Expected: A confirmation beginning with `New person added:` appears. A Client record for John Doe is shown
       with the supplied email, phone number, and `referral` tag.

    3. Test case: `add r/Prospect n/Jane Tan e/jane@example.com p/98765432`<br>
       Expected: A confirmation beginning with `New person added:` appears. A Prospect record for Jane Tan is
       added at index 2.

2. Rejecting duplicate records

    1. Prerequisites: John Doe has been added as above.

    2. Test case: `add r/Client n/John Doe e/john@example.com p/91234567`<br>
       Expected: `This contact already exists.` is shown and no additional record is created.

3. Rejecting invalid input

    1. Test case: `add r/Client n/John Doe e/john@example.com p/123`<br>
       Expected: An error message is shown for the invalid phone number and no record is created.

### Editing a client/prospect

Use a separate test folder with fictional records. Add John Doe and Jane Tan as described in the preceding section.

1. Editing a displayed record

    1. Prerequisites: Run `list` so that John Doe is at index 1 and Jane Tan is at index 2.

    2. Test case: `edit 1 r/Prospect e/john.new@example.com p/91234568`<br>
       Expected: A confirmation beginning with `Edited person:` appears. John Doe is now a Prospect with the new
       email address and phone number; the existing `referral` tag remains.

2. Editing one field

    1. Test case: `edit 2 r/Client`<br>
       Expected: A confirmation beginning with `Edited person:` appears. Jane Tan is now a Client and her other
       details are unchanged.

3. Rejecting invalid edits

    1. Test case: `edit 3 n/Alex Tan`<br>
       Expected: `The person index provided is invalid.` is shown and neither record changes.

    2. Test case: `edit 1`<br>
       Expected: `At least one field to edit must be provided.` is shown and John Doe's record does not change.

### Financial information and contact preferences

Use a separate test folder with fictional records. Repeat this setup before testing **each** feature below;
`clear` removes all records in that test copy. Enter each line as a separate command.

```text
clear
add r/Client n/Alex Teo e/alex@example.com p/91234567
add r/Prospect n/Jamie Lim e/jamie@example.com p/98765432
info 1 fn/Education pr/Low ai/Savings
contact 1 ch/Email tm/Weekends
info 2 fn/Retirement pr/Medium ai/Insurance
contact 2 ch/Email tm/Weekends
```

#### Testing financial information: `info`

1. Set values with financial notation and reversed field order:
   `info 2 ai/Planning Life/Health insurance pr/High fn/Save $3000/month + CPF/SRS`.<br>
   Expected: Jamie's three financial fields match the supplied text. Contact preferences remain Email and Weekends;
   Alex and all unrelated details are unchanged. Also try accented text such as `info 2 fn/épargne CPF/SRS`.
2. Run `info 2 fn/- ai/-`.<br>
   Expected: Financial need and area of interest disappear; priority remains High. Repeat the command: it remains safe.
3. Try `info 2 pr/- fn/`, `info 2 pr/Low pr/High`, `info 2 /pr Low`, and `info 0 pr/Low` separately.<br>
   Expected: Each reports an error. Priority stays High and the cleared fields stay absent; no partial update occurs.
4. Run `find Jamie`, then `info 2 pr/Protect family income`.<br>
   Expected: Jamie remains the sole result at index 2 and their priority changes. `info 1 pr/High` and `info 3 pr/High`
   both fail. Run `list`: Alex still has Education, Low, Savings and their original contact preferences.
   Run `exit`, reopen the same test copy and check Jamie: the new priority and cleared fields are retained.

#### Testing contact preferences: `contact`

1. Run `contact 2 tm/Weekdays AM/PM ch/Phone/WhatsApp`.<br>
   Expected: Jamie's timing and channel match the supplied text. Financial information remains Retirement, Medium,
   Insurance; Alex and all unrelated details are unchanged.
2. Run `contact 2 ch/Phone tm/-`.<br>
   Expected: Channel changes to Phone and timing disappears. Then run `contact 2 tm/-`: Phone remains unchanged.
3. Try `contact 2 ch/- tm/`, `contact 2 ch/Email ch/Phone`, and `contact 0 ch/Email` separately.<br>
   Expected: Each reports an error; channel stays Phone and timing stays absent.
   Also try `contact 2 /ch Email` and `contact 2 ch/WhatsApp tmm/Evenings` separately.<br>
   Expected: Both report `Invalid contact parameter. Use ch/CHANNEL or tm/TIMING; use - to clear a preference.`
   Displayed and saved preferences remain Phone with no timing; neither rejected command changes them.
4. Run `find Jamie`, then `contact 2 tm/Lunch break`.<br>
   Expected: Jamie stays visible at index 2 with Phone and Lunch break. `contact 1 ch/WhatsApp` and
   `contact 3 ch/WhatsApp` both fail. Run `list`: Alex's details remain unchanged.
   Run `exit`, reopen the same test copy and confirm Jamie's Phone and Lunch break preferences remain.
5. Run `contact 2 ch/电话/WhatsApp 📞 tm/-`, then `exit` and reopen the same test copy.<br>
   Expected: Jamie's channel remains exactly `电话/WhatsApp 📞` and timing stays absent. Financial information
   remains Retirement, Medium, Insurance; Alex's details are unchanged.

### Deleting a client/prospect

#### Command behaviour and feedback

`ClientBookParser` routes `delete INDEX` to `DeleteCommandParser`. It trims surrounding whitespace,
requires exactly one argument, then uses `ParserUtil#parseIndex`. `DeleteCommand` resolves the original
full-list index through `Command#getVisiblePersonAtIndex`; a hidden person is rejected even if that index
exists in the full list. The same indices apply after `find` or due-date sorting.

`Model#deletePerson` removes the entire record, including its financial information, contact preferences,
tags, interaction notes, next action and yearly reminders. Other records retain their details; remaining
full-list indices shift down. The active filter and comparator are retained. `LogicManager` saves the
temporary model before committing it to the live model, so command or save failures leave live data unchanged.
Confirmed deletion cannot be undone. Success reports `RELATIONSHIP NAME has been deleted successfully.`,
for example `Client John Doe has been deleted successfully.` or `Prospect Jane Tan has been deleted successfully.`.

##### Validation errors

Condition | Error message
----------|--------------
Missing index | `Client index is required.`
More than one argument | `Delete command only accepts a client index.`
Zero, negative, non-integer or overflowing index | `Client index must be a positive integer.`
Index is outside the full list or its person is hidden | `No client exists at index N.`
Unrecognised response during confirmation | `Please enter Y/Yes or N/No.`

Checks run in the order shown. Rejection preserves the data, filter and sorting.

Before deletion, `DeleteCommand#getConfirmationMessage` validates the currently visible index without changing data or saved files. The shared `LogicManager` confirmation flow shows:

```text
Are you sure you want to delete this?
Contact: NAME
Enter Y/Yes to confirm or N/No to cancel.
```

`Y` or `Yes` confirms deletion. `N` or `No` cancels with `Person deletion cancelled.`. Matching ignores case and surrounding whitespace. Other responses show `Please enter Y/Yes or N/No.` and keep confirmation pending. Confirmation revalidates the index before deletion. Confirmed deletion cannot be undone.

#### Manual regression checks

Use a separate test folder with fictional records. Run these commands to prepare the test data:

```text
clear
add r/Client n/John Doe e/john@example.com p/91234567
add r/Prospect n/Jane Tan e/jane@example.com p/98765432
info 2 fn/Retirement planning pr/High ai/Life insurance
note 2 d/2026-10-06 n/Discussed next steps.
```

1. Deleting a client/prospect while the full list is shown

    1. Prerequisites: Prepare the records above and run `list`.

    2. Test case: `delete 1`, then `Y`<br>
       Expected: The prompt names John Doe without deleting him. After `Y`,
       `Client John Doe has been deleted successfully.` John is removed. Jane moves to index 1,
       with her financial information and interaction note unchanged.

    3. Test case: `exit`, then reopen the app from the same folder.<br>
       Expected: John remains absent. Jane and her recorded details are retained.

    4. Test case: `delete 1`, then `Yes`<br>
       Expected: The prompt names Jane Tan. After confirmation,
       `Prospect Jane Tan has been deleted successfully.` The list becomes empty.

    5. Test case: `exit`, then reopen the app again.<br>
       Expected: The list stays empty; sample records do not replace the saved empty list.

2. Deleting from a filtered list

    1. Prerequisites: Recreate the test records above, then run `find Jane`.

    2. Test case: `delete 2`, then `yes`<br>
       Expected: The prompt names Jane Tan. Confirmation deletes Jane using her unchanged full-list index.
       The search results become empty.

    3. Test case: `list`<br>
       Expected: John is still present. Repeat this test using `find r/prospect` to check relationship-filtered deletion.

3. Rejecting invalid deletion

    1. Prerequisites: Recreate the test records above and run `list`. None of the following commands should change
       either record, their details or the current search results.

    2. Test case: `delete`<br>
       Expected: `Client index is required.`

    3. Test case: `delete 1 2` or `delete 1 abc`<br>
       Expected: `Delete command only accepts a client index.`

    4. Test case: `delete 0`, `delete -1`, `delete abc`, `delete 1.5`, or `delete 2147483648`<br>
       Expected: `Client index must be a positive integer.`

    5. Test case: `delete 3`<br>
       Expected: `No client exists at index 3.`

    6. Test case: `find Jane`, then `delete 1`<br>
       Expected: `No client exists at index 1.` Jane remains visible at index 2, and John is still present in the full
       list.

    7. Test case: `find Nobody`, then `delete 1`<br>
       Expected: `No client exists at index 1.` The empty search stays active. Use `list` and restart the app
       to confirm that both records and their details are retained.

4. Cancelling and correcting confirmation responses

    1. Prerequisites: Recreate the records above and run `find Jane`.

    2. Test case: `delete 2`, then `maybe`, then `list`<br>
       Expected: The prompt names Jane. Both later inputs report `Please enter Y/Yes or N/No.`.
       The confirmation stays pending; neither the records nor the current search changes.

    3. Test case: `nO`<br>
       Expected: `Person deletion cancelled.` Jane remains visible and all saved details are unchanged.

    4. Test case: `list`, then `delete 2`, then `yEs`<br>
       Expected: `list` works normally after cancellation. The new prompt names Jane; confirmation deletes her.

These cases and unchanged-file checks are covered in `DeleteIntegrationTest`. Invalid indices must produce
their errors immediately, with no pending prompt; a subsequent `list` must work normally.
Existing `TagDeleteCommandTest` cases cover the unchanged tag confirmation flow.
Parser whitespace and integer boundaries are covered in
`DeleteCommandParserTest`.

### Interaction notes

Use a separate test folder with fictional records. Recreate this setup before each numbered group below:

```text
clear
add r/Client n/John Doe e/john@example.com p/91234567
add r/Prospect n/Jane Tan e/jane@example.com p/98765432
note 2 d/2020-02-29 t/CALL o/PENDING n/Discussed retirement planning.
note 2 d/2020-03-02 n/Sent policy details.
```

John has no notes. Jane has two notes, numbered chronologically. Run each group's test cases in order.
Rejected commands should leave both records, their notes and the current filter unchanged.

1. Adding notes with `note`

   1. Prerequisites: Prepare the records above and run `list`.

   2. Test case: `note 1 d/2020-03-01 n/Shared https://example.com/plans; discussed option A/B.`<br>
      Expected: `Added interaction note for John Doe.` The note appears immediately with type and outcome
      `UNSPECIFIED`; Jane's notes are unchanged.

   3. Test case: `note 2 d/2020-02-28 t/meeting o/successful n/Reviewed goals.`<br>
      Expected: `Added interaction note for Jane Tan.` The new note appears first, with type `MEETING` and outcome
      `SUCCESSFUL`. The existing notes become numbers 2 and 3.

   4. Test case: Repeat `note 2 d/2020-02-28 t/meeting o/successful n/Reviewed goals.`<br>
      Expected: `This interaction note already exists for this contact.` No fourth note is added.

   5. Test case: `note`, `note 1 n/Missing date`, or `note 1 d/2020-03-01`<br>
      Expected, respectively: `Person index is required.`,
      `Interaction date is required: use d/yyyy-MM-dd before n/NOTE.`, or
      `Interaction note is required: use n/NOTE after the date.`

   6. Test case: `note 1 d/2020-03-01 n/` or the same command with only spaces after `n/`<br>
      Expected: `Interaction note cannot be empty.`

   7. Test case: `note 1 d/2020-02-30 n/Invalid date`<br>
      Expected: `Interaction date must be a valid date in yyyy-MM-dd format.`

   8. Test case: `note 1 d/2020-03-01 t/EMAIL n/Unsupported type`<br>
      Expected: `Interaction type must be MEETING, CALL, MESSAGE, OTHER, or UNSPECIFIED.`

   9. Test case: `note 0 d/2020-03-01 n/Test` or `note abc d/2020-03-01 n/Test`<br>
      Expected: `Index must be a positive integer.`

   10. Test case: `note 3 d/2020-03-01 n/Test`<br>
       Expected: `The person index provided is invalid.`

2. Editing notes with `noteedit`

   1. Prerequisites: Recreate the setup above and run `find Jane`. Jane remains contact 2, despite being the only result.

   2. Test case: `noteedit 2 1 n/Discussed insurance renewal.`<br>
      Expected: `Updated interaction note 1 for Jane Tan.` Only the first note's text changes; its date,
      type `CALL` and outcome `PENDING` are preserved. The search remains active.

   3. Test case: `noteedit 2 2 d/2020-02-28`<br>
      Expected: `Updated interaction note 2 for Jane Tan.` The edited note now appears first with its text
      `Sent policy details.` unchanged. The former first note becomes number 2.

   4. Test case: `noteedit 2 2 d/2020-03-01 n/Confirmed renewal details.`<br>
      Expected: `Updated interaction note 2 for Jane Tan.` Both fields change; type `CALL` and outcome `PENDING`
      remain. John and the current filter are unchanged.

   5. Test case: `noteedit 1 1 n/Hidden contact` or `noteedit 3 1 n/No contact`<br>
      Expected: `The person index provided is invalid.`

   6. Test case: `noteedit 2 3 n/No note`<br>
      Expected: `The interaction note index provided is invalid.`

   7. Test case: `noteedit 0 1 n/Test`, `noteedit 2 0 n/Test`, or `noteedit 2 abc n/Test`<br>
      Expected: `Index must be a positive integer.`

   8. Test case: `noteedit`, `noteedit 2 1`, or `noteedit 2 1 x/Unsupported`<br>
      Expected: Invalid command format feedback showing the `noteedit` usage.

   9. Test case: `noteedit 2 1 n/` or the same command with only spaces after `n/`<br>
      Expected: `Interaction note cannot be empty.`

   10. Test case: `noteedit 2 1 d/2020-02-30`<br>
       Expected: `Interaction date must be a valid date in yyyy-MM-dd format.`

   11. Test case: `list`, then `noteedit 1 1 n/No history`<br>
       Expected: `The interaction note index provided is invalid.` John's empty history is unchanged.

3. Deleting notes with `notedelete`

   1. Prerequisites: Recreate the setup above and run `find Jane`.

   2. Test case: `notedelete 1 1` or `notedelete 3 1`<br>
      Expected: `The person index provided is invalid.` Jane remains visible at contact index 2.

   3. Test case: `notedelete 2 3`<br>
      Expected: `The interaction note index provided is invalid.`

   4. Test case: `notedelete 0 1`, `notedelete 2 0`, or `notedelete 2 abc`<br>
      Expected: `Index must be a positive integer.`

   5. Test case: `notedelete`, `notedelete 2`, or `notedelete 2 1 extra`<br>
      Expected: Invalid command format feedback showing the `notedelete` usage.

   6. Test case: `notedelete 2 1`<br>
      Expected: `Deleted interaction note 1 for Jane Tan (2020-02-29): Discussed retirement planning.`
      Only that note is removed. `Sent policy details.` becomes note 1; the search remains active.

   7. Test case: `notedelete 2 1`<br>
      Expected: `Deleted interaction note 1 for Jane Tan (2020-03-02): Sent policy details.`
      Jane's empty interaction-note section is hidden. Other contact details remain unchanged.

   8. Test case: `notedelete 2 1` again<br>
      Expected: `The interaction note index provided is invalid.` The empty history is unchanged.

4. Saving additions, edits and deletions across restarts

   1. Prerequisites: Recreate the setup above.

   2. Test case: `note 1 d/2020-03-01 n/First discussion.`, then `exit`. Reopen the app from the same test folder
      and run `list`.<br>
      Expected: John's new note and both of Jane's original notes remain.

   3. Test case: `noteedit 2 1 n/Corrected discussion.`, then `exit`. Reopen from the same folder and run `list`.<br>
      Expected: Jane's first note retains its original date, type and outcome, with text `Corrected discussion.`
      Her second note and John's note remain unchanged.

   4. Test case: `notedelete 2 2`, then `exit`. Reopen from the same folder and run `list`.<br>
      Expected: `Sent policy details.` remains absent. Jane's corrected note and John's note are retained.

   5. Test case: `notedelete 2 1`, then `exit`. Reopen from the same folder and run `list`.<br>
      Expected: Jane still has no interaction notes; John's note remains.

### Finding persons by relationship
### Managing tags

Use a separate test folder with fictional records. Enter each setup command on a separate line:

```text
clear
add r/Client n/John Doe e/john@example.com p/91234567 t/referral
add r/Prospect n/Jane Tan e/jane@example.com p/98765432
```

#### Adding tags: `tag`

1. Adding one or more tags

   1. Test case: `tag 2 priority insurance`<br>
      Expected: Jane Tan gains both tags. Her relationship and contact details remain unchanged.

   2. Test case: `find r/prospect`, followed by `tag 2 followup`<br>
      Expected: Jane Tan gains `followup`. The command uses her unchanged full-list index even in filtered results.

2. Rejecting invalid or duplicate tags

   1. Test case: `tag 2 PRIORITY warm`<br>
      Expected: An error reports that `PRIORITY` already exists. Matching is case-insensitive, and `warm` is not
      added because the command is all-or-nothing.

   2. Test cases: `tag 2 close-friend`, `tag 3 priority`<br>
      Expected: The first command rejects the non-alphanumeric tag; the second rejects the invalid index. Existing
      tags and other details remain unchanged.

#### Deleting tags: `tagdelete`

1. Deleting selected tags after confirmation

   1. Prerequisites: Run `list`. Jane Tan is at index 2 with `priority`, `insurance`, and `followup`.

   2. Test case: `tagdelete 2 insurance`, followed by `YeS`<br>
      Expected: The first command displays Jane Tan and `[insurance]` without changing her record. The confirmation
      removes only `insurance`; confirmation matching is case-insensitive.

   3. Test case: `tagdelete 1 REFERRAL`, followed by `Y`<br>
      Expected: John Doe's `referral` tag is removed despite the different letter case.

2. Cancelling or rejecting deletion

   1. Test case: `tagdelete 2 missing`<br>
      Expected: An error is shown before confirmation because Jane Tan has no such tag. No tags are removed.

   2. Test case: `tagdelete 2`, followed by `maybe`, then `No`<br>
      Expected: The first command requests deletion of every remaining tag. `maybe` is rejected and leaves the
      confirmation pending; `No` cancels it, leaving all tags unchanged.

   3. Test case: Repeat `tagdelete 2`, followed by `Y`.<br>
      Expected: Every tag is removed from Jane Tan while her other details remain unchanged.

### Finding persons

1. Name filtering

   1. Prerequisites: Add persons named `Alex Tan`, `Alexander Lim`, and `Anne-Marie Lee`.

   2. Test case: `find Alex`<br>
      Expected: Alex Tan and Alexander Lim are displayed because name matching is case-insensitive and contiguous.

   3. Test case: `find Anne`<br>
      Expected: Anne-Marie Lee is displayed because a keyword can match part of a hyphenated name.

   4. Test cases: `find Alex123`, `find A/ex`<br>
      Expected: Each command rejects the invalid name keyword and leaves the current list unchanged.

2. Filtering clients and prospects

   1. Prerequisites: Add multiple clients and prospects, then display all persons using the `list` command.

   2. Test case: `find r/client`<br>
      Expected: Only clients are displayed, in the same relative order as in the full list.

   3. Test case: `find r/PROSPECT`<br>
      Expected: Only prospects are displayed, in the same relative order as in the full list.

   4. Test case: `find r/Client`<br>
      Expected: The same persons are displayed as for `find r/client`.

3. Invalid relationship filters

    <ol type="i">
      <li>
        <p>Test case: <code>find r/customer</code><br>
        Expected: No filtering occurs. The status message explains that the relationship must be <code>Client</code> or <code>Prospect</code>.</p>
      </li>
      <li>
        <p>Other incorrect commands to try: <code>find r/</code>, <code>find r/client r/prospect</code>.<br>
        Expected: No filtering occurs. The status message shows the relevant error details.</p>
      </li>
    </ol>

4. Combined name and relationship filtering

   1. Test case: `find Alex r/client`.<br>
      Expected: Only clients whose names contain the contiguous text `Alex` are displayed. This includes names such
      as `Alexander`, and matching is case-insensitive.

5. Tag filtering

   1. Prerequisites: Add persons tagged `friend`, `friends`, `bestfriend`, and `family`.

   2. Test case: `find t/friend`.<br>
      Expected: The persons tagged `friend`, `friends`, and `bestfriend` are displayed. The person tagged `family`
      is not displayed.

6. Remembered-detail filtering

   1. Prerequisites: Add a client named Alex with preferred channel `WhatsApp`, financial need `Retirement planning`,
      a `CALL` interaction note dated `2026-10-06` with outcome `SUCCESSFUL` and text
      `Discussed university fund`, and follow-up text `Call about investment review` due on `2026-12-15`. Add another
      client without those details.

   2. Test cases: `find k/WhatsApp`, `find k/retire`, `find k/CALL`, `find k/success`, `find k/university`,
      `find k/2026-10-06`, `find k/investment`, `find k/2026-12-15`, and `find k/2026-12`.<br>
      Expected: Alex is displayed for every search. Matching is case-insensitive and accepts contiguous partial text.

   3. Test case: `find Alex r/client t/priority k/retirement`.<br>
      Expected: Alex is displayed only if the record also has the `priority` tag, demonstrating `AND` across filter
      types.

7. Find-result keyboard navigation

   1. Prerequisites: Add enough persons matching the same search to require scrolling in the result list.

   2. Test case: Enter a `find` command that returns multiple persons.<br>
      Expected: The first result is selected automatically.

   3. Test case: Press the <kbd>Down</kbd> arrow key repeatedly.<br>
      Expected: Each press selects the next result and scrolls it into view. Selection remains on the last result
      after the end of the list is reached.

   4. Test case: Press the <kbd>Up</kbd> arrow key repeatedly.<br>
      Expected: Each press selects the previous result and scrolls it into view. Selection remains on the first
      result after the start of the list is reached.

   5. Test case: Enter a `find` command with no matches, then press either arrow key.<br>
      Expected: No person is selected and arrow-key navigation does not occur.

   6. Test case: After a successful `find`, press <kbd>Shift</kbd>, <kbd>Ctrl</kbd>, <kbd>Alt</kbd>, or <kbd>Meta</kbd>
      together with an arrow key.<br>
      Expected: Find-result navigation does not change the selected person.

   7. Test case: Display persons without using `find`, select a person with the mouse, return focus to the command
      box, and press either arrow key.<br>
      Expected: The find-result navigation handler does not change the selected person.

   8. Test case: Enter a `find` command with matches, then execute `list` and press either arrow key while the command
      box has focus.<br>
      Expected: Executing the non-find command disables find-result navigation.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

2. _{ more test cases … }_

