---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`

## US14 interaction-note regression checks

Run the full suite and coding checks on Windows with:

```powershell
.\gradlew.bat test checkstyleMain checkstyleTest
git diff --check
```

Relevant tests include `InteractionNoteTest`, `NoteCommandParserTest`, `NoteCommandTest`, `JsonAdaptedPersonTest`,
`LogicManagerTest` and `PersonCardTest`. They cover strict date parsing, realistic note text, first/repeated entries,
filtered indices, old JSON compatibility, persistence across reopening, and preservation through `edit` and `info`.
The JavaFX tests check the preferred height before first layout, immediate ListView resizing after model replacement,
additional/long wrapped notes, compact unaffected cards and scrolling. These tests require a graphical environment.

For manual checks, use disposable records and a separate data file:

1. Display at least three records. Run `note 1 d/2026-10-06 n/Test note`. Confirm the dated entry appears immediately
   and the next card remains directly underneath without clicking or scrolling to correct the layout.
2. Repeat the command. Confirm both identical entries remain visible and the card grows only as needed.
3. Add a long note containing Unicode, punctuation and a URL. Confirm wrapping is readable and scrolling reaches
   other records; records without notes remain compact.
4. Run `find` to display another person, then `note 1 d/2020-02-29 n/Backdated interaction`. Confirm only the person
   at index 1 in the search results changes.
5. Update that person's phone with `edit`, then their financial need with `info`. Confirm all notes remain.
6. Close and reopen the app using the same data file. Confirm the entries, order and duplicates remain.
7. Try an impossible date, blank note and out-of-range index. Confirm clear errors and unchanged records.
8. Load an older valid JSON file without `interactionNotes`. Confirm its records load with no note entries.
