---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# AB-3 User Guide

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add r/Client n/John Doe e/johnd@example.com p/98765432` : Adds a client contact named `John Doe`.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add r/RELATIONSHIP n/NAME e/EMAIL p/PHONE_NUMBER`

<box type="tip" seamless>

* `RELATIONSHIP` must be either `Client` or `Prospect`.
* `NAME` must contain 1–100 characters after leading and trailing whitespace is removed. It may include letters,
  spaces, hyphens (`-`), apostrophes (`'`), and the relationship markers `s/o`, `d/o`, or `w/o`.
* `EMAIL` must contain one `@` and no whitespace. Its local part may use common characters such as `.`, `_`, `+`,
  and `-`; each domain label may use letters, digits, and hyphens.
* Parameters may be entered in any order.
</box>

Examples:
* `add r/Client n/John Doe e/john.doe@example.com p/91234567`
* `add n/Ravi s/o Kumar p/98765432 r/Prospect e/ravi.kumar@example.com`
* `add r/Client n/Jane Tan e/jane+client@firm.co.uk p/98765432`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [t/TAG]... `

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Updating client financial information: `info`

Adds or updates a person's financial need, priority, and area of interest. Present fields appear on the person's card as
`Financial need: VALUE`, `Priority: VALUE`, and `Area of interest: VALUE`. Absent fields are hidden.

Format: `info INDEX [/fn FINANCIAL_NEED] [/pr PRIORITY] [/ai AREA_OF_INTEREST]`

* `INDEX` refers to the index in the currently displayed list, including filtered search results. It must be a positive
  integer (1, 2, 3, ...) corresponding to an existing person in that list.
* At least one of `/fn`, `/pr`, or `/ai` is required. Each parameter may be supplied at most once, in any order.
* Supplied values replace existing values. Unspecified fields remain unchanged.
* Each value must contain 1-200 characters after leading and trailing ordinary spaces are ignored. Internal spaces are
  preserved. Empty values are rejected and cannot be used to clear a field.
* Allowed characters are letters, digits, spaces, periods (`.`), commas (`,`), apostrophes (`'`), hyphens (`-`),
  parentheses (`(` and `)`), and ampersands (`&`). Tabs and line breaks are not allowed.
* Forward slash (`/`) is not allowed in a value because it is reserved for parameter prefixes.

Examples:

* `info 1 /fn retirement planning`
* `info 2 /pr protect family income /ai life insurance`
* `info 1 /ai investment products /fn wealth accumulation /pr preserve capital`

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a client/prospect: `delete`

Deletes the specified client or prospect from the client list.

Format: `delete INDEX`

* Deletes the client/prospect at the specified `INDEX`.
* The index refers to the index number shown in the displayed list.
* The index **must be a positive integer** 1, 2, 3, ...
* Leading and trailing spaces around the index are ignored. Only one index can be given.
* After deletion, the remaining clients/prospects are shown with updated index numbers.

Examples:
* `list` followed by `delete 2` deletes the 2nd client/prospect in the list.
* `find Betsy` followed by `delete 1` deletes the 1st client/prospect in the results of the `find` command.

Expected output: `Client John Doe has been deleted successfully.` (or `Prospect Jane Tan has been deleted successfully.`)

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every command. You do not need to save manually.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add r/RELATIONSHIP n/NAME e/EMAIL p/PHONE_NUMBER` <br> e.g., `add r/Client n/James Ho e/jamesho@example.com p/22224444`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**Info**   | `info INDEX [/fn FINANCIAL_NEED] [/pr PRIORITY] [/ai AREA_OF_INTEREST]`<br> e.g., `info 1 /fn retirement planning`
**List**   | `list`
**Help**   | `help`
