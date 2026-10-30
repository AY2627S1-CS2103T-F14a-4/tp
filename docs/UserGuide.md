---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# ClientBook User Guide

ClientBook is a desktop application for financial consultants to manage clients and prospects, recall their
high-level financial needs, and record dated interactions. It supports typed commands with a graphical interface
and stores records locally.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

2. Download the latest `.jar` file from the [ClientBook releases](https://github.com/AY2627S1-CS2103T-F14a-4/tp/releases).

3. Copy the file to the folder you want to use as the _home folder_ for your ClientBook.

4. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar clientbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Your client book starts empty.<br>
   ![Ui](images/Ui.png)

5. Type a command in the command box and press Enter to execute it. The result box below the command box shows
   the command you entered followed by its result.
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add r/Client n/John Doe e/johnd@example.com p/98765432` : Adds a client contact named `John Doe`.

   * `delete 1` : Deletes the 1st contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

6. Refer to the [Features](#features) section below for details of each command.

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

* Extraneous parameters for `help`, `list`, `exit`, and `clear` are ignored.<br>
  For example, `help 123` is interpreted as `help`. Other commands reject unsupported arguments.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the client book.

Format: `add r/RELATIONSHIP n/NAME e/EMAIL p/PHONE_NUMBER [t/TAG]...`

* `RELATIONSHIP` must be either `Client` or `Prospect`.
* `NAME` must contain 1–100 characters after leading and trailing whitespace is removed. It may include letters,
  spaces, hyphens (`-`), apostrophes (`'` or `’`), periods (`.`), commas (`,`), and the relationship markers
  `s/o`, `d/o`, or `w/o`.
* A name must not duplicate another contact's name. Name comparison ignores letter case and repeated spaces.
* `EMAIL` must contain 5–254 characters, one `@`, no whitespace, text in both the local and domain parts, and at
  least one dot in the domain. Its local part may use common characters such as `.`, `_`, `+`, and `-`; each domain
  label may use letters, digits, and hyphens, and cannot exceed 63 characters.
* Each phone number must contain 8 to 15 digits, excluding an optional leading `+`, following the E.164 international
  maximum. Spaces, hyphens, parentheses, and periods may separate digits. You can record multiple numbers in one
  value by separating them with labels, for example, `home: 92849584 ph: 02848594`.
* Email addresses and phone numbers must be unique. Email comparison ignores casing in the domain, so
  `alice@example.com` and `alice@EXAMPLE.COM` are duplicates; the local part remains case-sensitive. Phone-number
  comparison ignores formatting and compares each complete numeric sequence, so `9999 9999` and `99999999` are
  duplicates. Entries that share any complete phone number are also duplicates, regardless of the order in which
  multiple numbers are entered.
* Parameters may be entered in any order.
* Each optional `TAG` must contain only letters and numbers, with no spaces. Use `t/` before every tag.

Examples:
* `add r/Client n/John Doe e/john.doe@example.com p/91234567`
* `add n/Ravi s/o Kumar p/98765432 r/Prospect e/ravi.kumar@example.com`
* `add r/Client n/Jane Tan e/jane+client@firm.co.uk p/98765432`
* `add r/Client n/Amir Khan e/amir@example.com p/87654321 t/referral t/priority`

### Listing all persons: `list`

Shows a list of all persons in the client book.

Format: `list`

Each person's tags are shown with their contact and relationship details.
Tags are displayed in alphabetical order, ignoring letter case.

Each person keeps the same index when the list is filtered or their details are edited. Search and due results show
the person's original `list` index, so use the number shown beside the person for subsequent commands. Indexes are
renumbered only after a person is deleted.

### Adding tags to a person: `tag`

Adds one or more tags without removing the person's existing tags.

Format: `tag INDEX TAG [MORE_TAGS...]`

* `INDEX` must be a positive integer that refers to a person in the currently displayed list, including filtered
  search results.
* Each `TAG` must contain only letters and numbers, with no spaces.
* Existing tags are preserved. Tags are compared case-insensitively, so adding `PRIORITY` when `priority` exists
  produces an error and no tags are added.
* Tags supplied in the same command must also be unique case-insensitively. For example,
  `tag 1 priority PRIORITY` is rejected without adding either tag.

Examples:

* `tag 1 priority` adds `priority` to person `1`.
* `tag 2 referral insurance` adds both tags to person `2`.

### Deleting tags from a person: `tagdelete`

Deletes selected tags, or all tags, after confirmation.

Format: `tagdelete INDEX [TAG...]`

* `INDEX` is the number shown beside the person in the current list, including a filtered list.
* Omit `TAG` to delete all tags. Otherwise, separate the tags to delete with spaces.
* Tag matching is case-insensitive. If the person does not have every supplied tag, no tags are deleted.
* The result box shows the person and tags to be deleted. Enter `Y` or `Yes` to confirm, or `N` or `No` to cancel.
  Responses are case-insensitive.
* Until you confirm or cancel, any other input is rejected and the deletion remains pending.

Examples:

* `tagdelete 1` requests deletion of every tag from person `1`.
* `tagdelete 1 friend referral` requests deletion of only `friend` and `referral` from person `1`.

### Editing a person: `edit`

Edits an existing person in the client book.

Format: `edit INDEX [r/RELATIONSHIP] [n/NAME] [p/PHONE] [e/EMAIL]`

<box type="tip" seamless>

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* `RELATIONSHIP` must be either `Client` or `Prospect`.
* The edited name must not duplicate another contact's name. Name comparison ignores letter case and repeated spaces.
* `EMAIL` must meet the same format requirements as `add`, including a total length of 5–254 characters and a
  maximum of 63 characters for each domain label.
* The edited email address and phone number must not duplicate those of another contact. Phone numbers use the same
  formatting-insensitive, multiple-number comparison as `add`; for example, `9999 9999` and `99999999` conflict.
  Email-domain casing is also ignored, so `alice@example.com` conflicts with `alice@EXAMPLE.COM`.

</box>

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 1 r/Client` Changes the 1st person's relationship to `Client`.
*  `edit 2 n/Betsy Crower` Edits the name of the 2nd person to be `Betsy Crower`.

### Updating financial information: `info`

Record a client's or prospect's financial need (`fn/`), priority (`pr/`), and area of interest (`ai/`).
These details appear on their card; cleared fields are hidden.

Format: `info INDEX [fn/VALUE|-] [pr/VALUE|-] [ai/VALUE|-]`

* Use the positive index shown on the person's card. Search results keep their original full-list indices:
  if the only result is labelled `2`, use `info 2`. The person must be visible in the current results.
* Supply at least one field, each at most once, in any order. Separate fields with spaces and use lowercase prefixes.
* A supplied value replaces that field; omitted fields and other person details stay unchanged.
  Use an exact `-` to clear a field. Spaces around `-` are allowed; `Long-term` remains ordinary text.
* Values must contain 1–200 characters after surrounding ordinary spaces are removed. Internal spaces are kept.
  Unicode letters, including accented text, digits, and `. , ' ( ) & - $ % + /` are accepted.
  Tabs, line breaks, blank values and other punctuation (such as `!` or `:`) are rejected.
  Slash-containing text can appear anywhere in a value, including `Planning CPF/SRS` and `Save $3000/month`.
* Updates are saved automatically. If any supplied field is invalid, none of the fields change.

Examples:

* `info 2 fn/Save $3000/month + CPF/SRS pr/High ai/Planning Life/Health insurance` records all three fields.
* `info 2 ai/Insurance pr/Protect family income` updates two fields, keeping the financial need.
* `info 2 pr/High ai/-` updates priority and clears the area of interest.
* `info 2 fn/- pr/- ai/-` clears all financial information.

**Prefix restrictions:** A space-separated `fn/`, `pr/`, or `ai/` always starts a field, even inside text.
For example, `fn/Review pr/High options` stores financial need `Review` and priority `High options`.
There is no quoting or escaping; rephrase literal prefix-like text as `fn/Review priority High options`.
Malformed forms such as `/fn VALUE`, `FN/` and `fnn/` are rejected. Other slash-containing text inside a value,
including some misspellings such as `xyz/Value`, may be saved as text, so check the displayed result.

### Updating contact preferences: `contact`

Record how and when a client or prospect prefers to be contacted. Recorded preferences appear on their card;
cleared preferences are hidden.

Format: `contact INDEX [ch/CHANNEL|-] [tm/TIMING|-]`

* Use the positive index shown on the person's card. Search results keep their original full-list indices:
  if Bob is labelled `2` after `find Bob`, use `contact 2`. The person must be visible in the current results.
* Supply at least one preference, each at most once, in either order. Separate prefixes from preceding text
  with ordinary spaces.
* A supplied value replaces that preference; omitted preferences and other person details stay unchanged.
  An exact `-`, optionally surrounded by ordinary spaces, clears a preference. Clearing an absent preference is safe.
  Type `-` directly to clear; a larger value such as `Phone-call` remains text.
* Each value must contain 1–200 characters of valid Unicode text after surrounding whitespace is removed.
  Internal spaces and case are kept. Free text, Unicode and punctuation are accepted,
  including `Phone/WhatsApp` and `Weekdays 6:30–8 pm`.
  Empty or whitespace-only values are invalid.
* Preferences are saved automatically. If either supplied preference is invalid, neither changes.
  Cleared preferences remain absent after restarting the application.

**Prefix restrictions:** Use lowercase `ch/` and `tm/`. A space-separated `ch/` or `tm/` starts another
preference even inside a value; there is no quoting or escaping. Malformed forms such as `/ch`, `/tm`,
`cch/` and `tmm/` are rejected after an ordinary space, while some other slash-containing text may be
treated as part of a preference value. Check the displayed preferences after updating them.

Examples:

* `contact 2 ch/WhatsApp tm/Weekdays after 6 pm` records both preferences.
* `contact 2 tm/Lunch break ch/Phone call` updates both preferences in the opposite order.
* `contact 2 ch/Email tm/-` changes the channel and clears the timing.
* `contact 2 ch/-` clears only the channel, keeping any recorded timing.

### Adding a dated interaction note: `note`

Adds a dated note to a client or prospect. Notes appear on the person's card in ascending date order (oldest first).

Format: `note INDEX d/DATE [t/TYPE] [o/OUTCOME] n/NOTE`

* `INDEX` refers to the currently displayed list, including search results.
* `DATE` must be a real calendar date in `yyyy-MM-dd` format, with a year from `0001` to `9999`.
  Year `0000` is rejected with `Interaction date must be a valid date in yyyy-MM-dd format.`
  Today's date and past dates are accepted.
  Future dates are rejected with `Interaction date cannot be in the future.` using the computer's local date.
* Put `d/DATE` first, then any optional `t/` and `o/` fields in either order, before `n/`.
  Everything after `n/` is note text, including URLs, punctuation and prefix-like text.
* `TYPE` describes the interaction: `MEETING`, `CALL`, `MESSAGE`, or `OTHER`.
  `OUTCOME` records its result: `SUCCESSFUL`, `UNSUCCESSFUL`, or `PENDING`.
  Values ignore case and are saved and displayed in uppercase. For example, `t/call` means `CALL`.
* Both fields are optional and independent. An omitted field is `UNSPECIFIED`, meaning no detail was recorded;
  this does not imply `OTHER`, a successful interaction, or a pending result. You may also supply `UNSPECIFIED`
  explicitly. Existing commands without either field still work, and older saved notes load with these defaults.
* Each header prefix (`d/`, `t/`, `o/`) may appear only once. Blank or unsupported metadata values are rejected.
* Note text cannot be blank. Surrounding whitespace is removed; case and internal text are preserved.
* Adding a note keeps all previous notes. An exact duplicate (same date and text) for the same contact is rejected
  with `This interaction note already exists for this contact.` Existing records remain unchanged.
* Changing type or outcome does not bypass the same-date-and-text duplicate check.
* Notes with the same date but different text, or the same text but different dates, are allowed.
  Text comparison is case-sensitive and preserves internal spaces; surrounding whitespace is removed as usual.
  Identical notes for different contacts are allowed.
* Notes are saved automatically and preserved when using `edit` or `info`.
* Each entry shows its date, type, outcome and text under **Interaction notes:** on the person's card. Long text wraps, and the
  card grows to fit its contents immediately. Scroll the client list to read longer histories.
* There is no maximum note length imposed by this command. Enter a note on one command line.

Example: `note 1 d/2026-10-06 n/Discussed retirement planning and agreed to follow up next month.`

Example with interaction details:

`note 1 d/2026-10-28 t/CALL o/UNSUCCESSFUL n/Client did not answer`

The card displays `1. 2026-10-28 [Type: CALL; Outcome: UNSUCCESSFUL]: Client did not answer`
if this is the first note. Use today's date or a past date when trying the example.

Partial metadata is supported: `note 1 d/2026-10-28 t/MESSAGE n/Sent policy details` displays
`[Type: MESSAGE; Outcome: UNSPECIFIED]`. With neither field, the display shows
`[Type: UNSPECIFIED; Outcome: UNSPECIFIED]`.

Success message: `Added interaction note for PERSON_NAME.`

Examples of other accepted notes:

* `note 1 d/2020-02-29 n/Reviewed the client's long-term goals.` (Backdated entry.)
* `note 1 d/2026-10-06 n/Shared https://example.com/plans/retirement; discussed option A/B.`

Invalid input leaves existing notes unchanged.

Backdated notes are placed in chronological order. Notes with the same date retain their existing order;
new notes with that date appear after them. Displayed note numbers update to match this order.
Use `find k/KEYWORD` to search interaction note dates, types, outcomes and text together with other remembered
details.

### Editing a dated interaction note: `noteedit`

Updates the date, text, or both fields of one existing interaction note belonging to a client or prospect.

Format: `noteedit CONTACT_INDEX NOTE_INDEX [d/DATE] [n/NEW_NOTE_CONTENT]`

* Both indices must be positive integers. `CONTACT_INDEX` refers to the currently displayed contact list,
  including search results. The current search filter remains active after editing.
* `NOTE_INDEX` is the number displayed beside the note under **Interaction notes:** on that contact's card.
  Numbering starts at 1 and follows chronological order (oldest first).
* Supply at least one of `d/` or `n/`. Omitted fields retain their existing values.
* A replacement date must be a real calendar date in `yyyy-MM-dd` format, following the same rules as `note`.
  An explicitly supplied future date is rejected with `Interaction date cannot be in the future.` using the computer's
  local date. Today's date and past dates are accepted. Omitting `d/` preserves the existing date, even for legacy
  future-dated notes.
  Put `d/DATE` before `n/` when editing both fields.
* Changing a date reorders the notes chronologically and updates their displayed numbers.
  Notes with identical dates retain their relative order. Other notes and all other contact information are preserved.
* New text cannot be blank. Surrounding whitespace is removed; case and internal text are preserved.
  Everything after `n/` is note text, including URLs and prefix-like strings. Enter it on one command line.
* Type and outcome are preserved when changing either date or text. `noteedit` does not change these fields;
  `t/` and `o/` after `n/` are literal note text.
* Changes are saved automatically.

Example: `noteedit 1 2 n/Discussed insurance renewal`

Other examples:
* `noteedit 1 1 d/2026-10-19` changes only the date, preserving the text.
* `noteedit 1 1 d/2026-10-19 n/Corrected meeting description` changes both fields.

This updates the second note of the first displayed contact. Success message:
`Updated interaction note 2 for PERSON_NAME.`

Invalid input does not change existing records.

### Deleting a dated interaction note: `notedelete`

Deletes one interaction note belonging to a client or prospect.

Format: `notedelete CONTACT_INDEX NOTE_INDEX`

* Both indices must be positive integers. `CONTACT_INDEX` refers to the currently displayed contact list,
  including search results. The current filter remains active after deletion.
* `NOTE_INDEX` is the number displayed beside the note under **Interaction notes:** on that contact's card.
  Numbering starts at 1 and follows chronological order (oldest first), e.g., `1. 2026-10-15: Called`.
* Only the specified note is removed, even if identical notes exist. Remaining notes retain their order,
  dates and text; subsequent displayed note numbers shift down by one automatically.
* All other contact information is preserved, including financial information, tags, contact preferences
  and next actions. Deleting the last note leaves the contact with an empty history.
* Changes are saved automatically and remain after restarting the application.

Example: `notedelete 1 2`

This deletes the second note of the first displayed contact. Success message:
`Deleted interaction note 2 for PERSON_NAME (2026-10-18): Client interested in investment`

Deletion cannot be undone. Invalid input does not change existing records.

### Locating persons by name, relationship, tags or remembered details: `find`

Filters persons by name, relationship, tags, remembered details, or any combination of these.

Format: `find [KEYWORD [MORE_KEYWORDS...]] [r/RELATIONSHIP] [t/TAG [MORE_TAGS...]] [k/DETAIL_KEYWORD [MORE_KEYWORDS...]]`

| Filter         | Syntax and matching rule                                                                                                                                                                                                                          |
|:---------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| KEYWORD        | Write one or more name keywords before any prefixed filter. Each keyword matches contiguous text in names only. Each keyword contains letters with optional internal hyphens or apostrophes, and may end with a period or comma; `s/o`, `d/o`, and `w/o` are also accepted.           |
| RELATIONSHIP   | Use `r/client` or `r/prospect`. The value must match the relationship exactly.                                                                                                                                                                    |
| TAG            | Put one or more alphanumeric keywords after one `t/`. Each keyword matches contiguous text in a tag; for example, `friend` matches `friend`, `friends`, and `bestfriend`.                                                                         |
| DETAIL_KEYWORD | Put one or more non-whitespace keywords after one `k/`. Punctuation and digits are accepted. This searches contact preferences, financial information, interaction-note dates, types, outcomes and text, and follow-up action text and due dates. |

* Provide at least one filter. Each prefix may appear only once; `r/`, `t/`, and `k/` may appear in any order.
* Matching is case-insensitive. Multiple values within the name, tag, or detail filter use **OR**. Different supplied
  filter types use **AND**.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* `k/` does not search names, phone numbers, email addresses, relationships, tags, or follow-up completion status.
* Results keep their `list` order and indexes. The first result is selected automatically; use <kbd>Up</kbd> and
  <kbd>Down</kbd> to move through results. Selection and scrolling stop at the list boundaries. No result means no
  selection.

Examples:

* `find alex david` returns names containing `alex` or `david`, such as `Alex Yeoh` and `David Li`.<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)
* `find r/client` returns all clients.
* `find Jack s/o` returns names containing `Jack` or the standalone marker `s/o`.
* `find t/friend referral` returns persons with a tag containing `friend` or `referral`.
* `find k/WhatsApp 2026-10-06` returns persons whose remembered details contain either value.
* `find alex r/client t/priority referral k/retirement WhatsApp` returns clients named `alex` who have a `priority`
  or `referral` tag and whose remembered details contain `retirement` or `WhatsApp`.

### Deleting a client/prospect: `delete`

Deletes a client or prospect and all their recorded details after confirmation.

Format: `delete INDEX`

* Use the index shown beside the person in the current list, including search and `due` results.
* Only one index is accepted. An invalid or hidden index shows an error without asking for confirmation.
* The result box asks `Are you sure you want to delete this?` and shows the person's name. Type `Y` or `Yes`
  to delete, or `N` or `No` to cancel. Responses ignore case and surrounding spaces.
* Other input shows `Please enter Y/Yes or N/No.` and keeps the confirmation pending; enter a valid response
  before using another command.
* Confirmed deletion is saved automatically and cannot be undone. Other people's details stay unchanged,
  remaining indices update, and any current search stays active.

Examples:

* `delete 2`, then `Yes` deletes the person shown at index 2 after confirmation.
* `find Jane`, then `delete 2`, then `No` keeps Jane if the index shown beside her is 2.

### Recording the next action: `next`

Records or replaces a client or prospect's next follow-up.

Format: `next INDEX a/ACTION d/yyyy-MM-dd`

* Use the index shown beside the person. They must be visible in the current list.
* Supply both `a/` and `d/` once each, in either order.
* The action must contain 1–200 characters, excluding spaces at the start and end.
* Use a real date in `yyyy-MM-dd` format, such as `2026-11-02`. It must be today or later.
* Each person has one next action. Replacing it sets it to pending and keeps their other details.

Examples:

* `next 1 a/Send retirement proposal d/2026-11-02` sets a follow-up for the person at index 1.
* `next 1 a/Call about revised proposal d/2026-11-05` replaces that person's previous follow-up.

Use today or a future date when trying these examples.

### Rescheduling a follow-up: `reschedule`

Changes an existing follow-up's date and, optionally, its description.

Format: `reschedule INDEX d/yyyy-MM-dd [a/ACTION]`

* Use the index shown beside the person. They must be visible and already have a follow-up.
* Supply `d/` once, with a real date in `yyyy-MM-dd` format that is today or later.
* Leave out `a/` to keep the description, or supply it once with a new description of 1–200 characters.
* Rescheduling sets the follow-up to pending, even if it was completed. Other details stay unchanged.
* A future follow-up leaves `due` unless the person also has an upcoming yearly reminder.

Examples:

* `reschedule 1 d/2026-11-02` changes the date and keeps the description.
* `reschedule 1 d/2026-11-05 a/Send revised proposal` changes both the date and description.

Use today or a future date when trying these examples.

### Completing a follow-up: `done`

Marks a client or prospect's next action as completed.

Format: `done INDEX`

* Use the index shown beside the person. They must be visible and have a next action.
* The description, date and other contact details stay unchanged. Repeating `done` is safe.
* Yearly reminders are unaffected. In `due`, the person stays visible if they have an upcoming reminder.
* Use `next` to replace a completed action, or `reschedule` to make it pending again.

Examples:

* `done 2` completes the next action of the person shown at index 2.
* `done 2` again leaves that action completed without changing its details.

### Setting a yearly reminder: `remind`

Sets a birthday, policy renewal or other date that repeats every year.

Format: `remind INDEX a/LABEL d/MM-dd`

* Use the index shown beside the person. They must be visible in the current list.
* Supply `a/` and `d/` once each, in either order. Labels must contain 1–50 characters after trimming,
  without tabs, line breaks or `/`.
* Use a real date in `MM-dd` format, such as `11-10`. February 29 falls on February 28 in non-leap years.
* Labels ignore case. Reusing a label updates its date and keeps its spelling; a new label adds a reminder.
* Reminders are saved automatically and repeat annually. They do not replace the next action, and `done`
  does not complete them.

Examples:

* `remind 1 a/Birthday d/11-10` adds a birthday reminder for November 10.
* `remind 1 a/Policy renewal d/12-01` adds a separate reminder for December 1.
* `remind 1 a/birthday d/11-11` changes the existing `Birthday` reminder to November 11.

### Removing a yearly reminder: `unremind`

Removes one yearly reminder from a client or prospect.

Format: `unremind INDEX a/LABEL`

* Use the index shown beside the person. They must be visible in the current list.
* Supply `a/` once with an existing reminder's label. Matching ignores case; do not include a date.
* Labels follow the same rules as `remind`. Other reminders and contact details stay unchanged.
* Removal is saved automatically. The person leaves `due` only if no due action or upcoming reminder remains.

Examples:

* `unremind 1 a/birthday` removes the reminder labelled `Birthday`.
* `unremind 1 a/Policy renewal` removes the reminder labelled `Policy renewal`.

### Viewing due follow-ups and upcoming reminders: `due`

Shows people with follow-ups to complete or yearly reminders coming up soon.

Format: `due`

* Includes incomplete next actions due today or earlier, and yearly reminders from today through 30 days ahead.
* Completed actions and future next actions do not appear unless the person also has an upcoming reminder.
* Each person appears once, ordered by their earliest due date, with overdue follow-ups first.
* Replaces any current search. Use the index shown beside the person for other commands; `list` shows everyone again.
* Do not add arguments. Run `due` again after midnight to update which follow-ups and reminders are shown.

Examples:

* `due` shows everyone with a due follow-up or an upcoming yearly reminder.
* `due`, then `done 2` completes the next action of the person shown at index 2, if they have one.

### Clearing all entries: `clear`

Clears all entries from the client book.

Format: `clear`

### Backing up records: `backup`

Creates a complete copy of all records, including financial information, interaction notes, and next actions.

Format: `backup`

The backup is saved as `clientbook-backup.json` in ClientBook's `data` folder.
Do not add arguments to this command.

Example: `backup`

### Restoring records: `restore`

Replaces all current records with the records from a backup created by ClientBook. The backup is validated before any
current records are changed. Restoring resets any active search so all restored records are visible.

Format: `restore`

The backup is read from `clientbook-backup.json` in ClientBook's `data` folder.
Do not add arguments to this command.

Example: `restore`

<box type="warning" seamless>

Restoring replaces the current records and cannot be undone. Create a backup first if you might need the current
records later.
</box>

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

ClientBook automatically saves changes to your records. You do not need to save manually.

If saving fails, check that the data folder and clientbook.json file are accessible and writable, then try again.

### Editing the data file

ClientBook data is saved automatically as a JSON file `[JAR file location]/data/clientbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, ClientBook starts with an empty client book at the next run. The invalid file remains on disk until ClientBook next successfully saves record data, for example, after a command that changes records. Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the ClientBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous ClientBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add r/RELATIONSHIP n/NAME e/EMAIL p/PHONE_NUMBER [t/TAG]...` <br> e.g., `add r/Client n/James Ho e/jamesho@example.com p/22224444 t/referral`
**Backup** | `backup`<br> e.g., `backup`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [r/RELATIONSHIP] [n/NAME] [p/PHONE_NUMBER] [e/EMAIL]`<br> e.g.,`edit 2 r/Client n/James Lee e/jameslee@example.com`
**Find**   | `find [KEYWORD [MORE_KEYWORDS...]] [r/RELATIONSHIP] [t/TAG [MORE_TAGS...]] [k/DETAIL_KEYWORD [MORE_KEYWORDS...]]`<br> e.g., `find James r/client t/priority referral k/retirement`
**Contact** | `contact INDEX [ch/CHANNEL\|-] [tm/TIMING\|-]`<br> e.g., `contact 1 ch/WhatsApp tm/-`
**Info**   | `info INDEX [fn/VALUE\|-] [pr/VALUE\|-] [ai/VALUE\|-]`<br> e.g., `info 1 fn/retirement planning`
**Note**   | `note INDEX d/DATE [t/TYPE] [o/OUTCOME] n/NOTE`<br> e.g., `note 1 d/2026-10-06 n/Discussed retirement planning.`
**Note edit** | `noteedit CONTACT_INDEX NOTE_INDEX [d/DATE] [n/NEW_NOTE_CONTENT]`<br> e.g., `noteedit 1 2 d/2026-10-19 n/Discussed insurance renewal`
**Note delete** | `notedelete CONTACT_INDEX NOTE_INDEX`<br> e.g., `notedelete 1 2`
**Restore** | `restore`<br> e.g., `restore`
**Tag**    | `tag INDEX TAG [MORE_TAGS...]`<br> e.g., `tag 1 priority referral`
**Tag delete** | `tagdelete INDEX [TAG...]`<br> e.g., `tagdelete 1 friend referral`
**Next**   | `next INDEX a/ACTION d/yyyy-MM-dd`<br> e.g., `next 1 a/Call d/2026-11-02` (use today or later)
**Reschedule** | `reschedule INDEX d/yyyy-MM-dd [a/ACTION]`<br> e.g., `reschedule 1 d/2026-11-02`
**Done**   | `done INDEX`<br> e.g., `done 1`
**Yearly reminder** | `remind INDEX a/LABEL d/MM-dd`<br>e.g. `remind 1 a/Birthday d/11-10`
**Remove yearly reminder** | `unremind INDEX a/LABEL`<br>e.g. `unremind 1 a/birthday`
**Due**    | `due`
**List**   | `list`
**Help**   | `help`
**Exit**   | `exit`

