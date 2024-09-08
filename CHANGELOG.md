# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.0.95] - 2024-09-08
### Added
- XML data export (Collect format): show records count before generation; added filter;

## [4.0.94] - 2024-09-02
### Added
- CE surveys: random grid generation;

## [4.0.93] - 2024-08-22
### Fixed
- Fixed cannot demote record in analysis step;

## [4.0.92] - 2024-07-04
### Added
- CSV data export: added filter condition (expression);

## [4.0.91] - 2024-04-13
### Added
- CSV data export: added option to always evaluate calculated attributes (useful for CE surveys);

## [4.0.90] - 2024-03-11
### Added
- Coordinate attribute: get current location from browser;

## [4.0.89] - 2023-11-10
### Fixed
- Code lists batch import: allow importing zip files generated with MacOS;

## [4.0.88] - 2023-11-09
### Added
- Code lists batch import: allow importing zip files containing Excel files;

## [4.0.87] - 2023-07-30
### Fixed
- Cannot add more rows in tables where taxon attribute is defined as key;

## [4.0.86] - 2023-06-14
### Fixed
- Record attribute validator: fixed non-relevant attributes considered as required;

## [4.0.85] - 2023-05-16
### Added
- Taxonomy import: export errors list to Excel;

## [4.0.84] - 2023-05-04
### Fixed
- Survey export: fixed error exporting survey to Collect Mobile;

## [4.0.83] - 2023-05-03
### Added
- File attribute: generate file name using an expression (optional);

## [4.0.82] - 2023-04-18
### Fixed
- Fixed dependent calculated attributes evaluation (Collect Earth surveys);

## [4.0.81] - 2023-04-17
### Fixed
- Fixed cannot add more than one multiple entity when key attributes are not defined;

## [4.0.80] - 2023-04-13
### Fixed
- Fixed Collect Earth surveys nested calculated attributes evaluation;

## [4.0.79] - 2023-04-04
### Added
- Optimized record validator (species data loading);

## [4.0.78] - 2023-04-03
### Added
- Optimized record validator (species data loading);

## [4.0.77] - 2023-04-01
### Fixed
- Avoiding loops in record validation with nested depending entities;

## [4.0.76] - 2023-03-31
### Added
- Optimized record validator (species data loading);

## [4.0.75] - 2023-03-15
### Fixed
- Bug fixes;

## [4.0.74] - 2023-03-13
### Fixed
- Bug fixes (table add button);

## [4.0.73] - 2023-03-01
### Added
- Data entry: prevent adding multiple empty entities (in forms and tables);

## [4.0.72] - 2023-02-23
### Fixed
- Survey editor: fixed Collect Earth surveys preview (dependent calculated attributes evaluation);

## [4.0.71] - 2023-02-13
### Added
- Survey editor: layout adjustments for Collect Earth surveys;

## [4.0.70] - 2023-02-02
### Added
- Survey editor: layout adjustments;

## [4.0.69] - 2023-01-20
### Added
- Survey editor: updated bottom button bar's layout;

## [4.0.68] - 2023-01-18
### Fixed
- Expressions: evaluation of calculated attributes inside single entities;

## [4.0.67] - 2023-01-01
### Added
- Updated third party libraries;

## [4.0.66] - 2022-12-21
### Added
- Survey Designer: simplifications for Collect Earth surveys;

## [4.0.65] - 2022-11-01
### Added
- Data export (Excel): added option in Additional Options to include images;

## [4.0.64] - 2022-09-09
### Fixed
- Collect Earth: calculated attribute sending EXTRA_attrName values;

### Added
- Data export: include sampling design item label (if defined);

## [4.0.63] - 2022-06-30
### Fixed
- Removed dependency on deprecated Apache Commons DDLUtils;

## [4.0.62] - 2022-06-24
### Fixed
- Fix for Collect Earth: dependent code attributes not refreshed on parent update;

## [4.0.61] - 2022-06-24
### Fixed
- Fix for Collect Earth: dependent code attributes not refreshed on parent update;

## [4.0.60] - 2022-06-23
### Added
- Sampling point data: use extra info columns label_XX as labels;

## [4.0.59] - 2022-06-21
### Fixed
- Fixed attribute conversion;
- Fixed error in code attribute autocomplete with long list of items;

## [4.0.58] - 2022-06-13
### Fixed
- Error importing species list;
- Error starting Collect Earth;

## [4.0.57] - 2022-06-12
### Fixed
- Layout adjustments (enumerated entities);

## [4.0.56] - 2022-06-09
### Fixed
- Fixed errors in survey designer (editing sampling unit);

## [4.0.55] - 2022-06-03
### Fixed
- Fixed error importing survey file;

## [4.0.54] - 2022-06-02
### Added
- Validation report: include only owned record for data cleansing limited user role;

## [4.0.53] - 2022-05-30
### Fixed
- Error importing survey;

## [4.0.52] - 2022-05-23
### Fixed
- Fixed errors editing code lists;

## [4.0.51] - 2022-05-16
### Fixed
- Updated dependencies;
- Fix in collect-core for Collect Mobile;

### Added
- Improved Survey Designer UI

## [4.0.50] - 2022-04-15
### Fixed
- Fixed error exporting surveys to Collect Mobile;

## [4.0.49] - 2022-04-10
### Fixed
- Bug fixes (Data grids);

## [4.0.48] - 2022-04-08
### Fixed
- Websockets not working properly when Collect runs in a server under https with load balancing;
- Blank screen starting Saiku when Collect runs in a server under https with load balancing;

## [4.0.47] - 2022-04-07
### Fixed
- Error uploading data from Collect Mobile;

## [4.0.46] - 2022-04-06
### Fixed
- Validation feedback not showing properly inside tables;

### Added
- Improved data tables layout;

### Changed
- Updated dependencies;

## [4.0.45] - 2022-03-30
### Added
- Data Cleaner Limited user role;

### Fixed
- Bug fixes;

## [4.0.44] - 2022-03-10
### Fixed
- Fixed error exporting Collect Earth survey from Survey Designer;

## [4.0.43] - 2022-03-03
### Fixed
- Bug fixes;

### Added
- Support latest version of PostgreSQL JDBC drivers;

## [4.0.42] - 2022-02-04
### Fixed
- Fixed error importing collect-data files;

## [4.0.41] - 2022-02-02
### Fixed
- Bug fixes; Fixed blank page opening Data Management and Survey Designer;

## [4.0.40] - 2022-02-01
### Fixed
- Bug fixes;

## [4.0.39] - 2022-01-04
### Fixed
- Prevent runtime errors importing records with "null" keys;
- Error importing big records using in SQLite DB;

## [4.0.38] - 2021-12-31
### Fixed
- Error importing big records using in SQLite DB;

## [4.0.37] - 2021-12-29
### Changed
- Updated dependencies;

## [4.0.36] - 2021-12-09
### Fixed
- Survey Designer / Survey files: fixed error adding CE area_per_attribute.csv file;

## [4.0.35] - 2021-12-08
### Fixed
- Data management / Single record export: fixed exported record files filter;

## [4.0.34] - 2021-12-07
### Added
- Data management / Record edit: added current record "Export to Collect format" button;

## [4.0.33] - 2021-11-18
### Added
- Survey Designer / Survey files: allow importing multiple files;

## [4.0.32] - 2021-10-04
### Added
- Data Management / Data Import Summary: added record import warnings icon;

## [4.0.31] - 2021-09-30
### Added
- Data Management / Data Import Summary: added Filled Values column and importability tooltips;

### Fixed
- Internal changes: removed dependencies giving conflicts;

## [4.0.30] - 2021-09-29
### Fixed
- Records validation: fixed error validating hierarchical code attributes using sampling point data in PostgreSQL;
- Survey Designer: fixed Boolean attribute type label not visible in Add Attribute dropdown;

## [4.0.29] - 2021-09-21
### Fixed
- CSV data export: fixed date and time values formatting;
- Survey Designer: fixed error importing labels' translation file;
- Species import: validate extra info column names (cannot use reserved words);

### Added
- Survey export: validate that multiple attribute is supported before exporting to Collect Mobile;

## [4.0.28] - 2021-09-14
### Added
- Survey designer: labels export/import - include tooltip texts;

## [4.0.27] - 2021-09-09
### Fixed
- Map: polygon text attribute not selectable in map;

## [4.0.26] - 2021-09-08
### Fixed
- Data entry: key code attribute label not appearing in enumerated entities;

## [4.0.25] - 2021-09-07
### Fixed
- Data entry: min/max not recalculated properly when expression is used;

## [4.0.24] - 2021-09-01
### Fixed
- Validation report not including missing values;

## [4.0.23] - 2021-08-31
### Fixed
- Data entry: multiple entities (forms) selector showing "empty" for key attributes with value '0';

### Added
- Survey Designer: avoid marking file attribute definitions as "key";

## [4.0.22] - 2021-05-26
### Fixed
- Layout adjustments (sidebar)

## [4.0.21] - 2021-05-04
### Fixed
- Fixed Control Panel not responding;

## [4.0.20] - 2021-04-07
### Added
- Updated dependencies (prepare new Collect Mobile release);

## [4.0.19] - 2021-03-16
### Fixed
- Internal update: removed dependencies from Open Foris Repository (prepare new Collect Mobile release);

## [4.0.18] - 2021-03-06
### Fixed
- Allow adding new items in enumerating code list of published surveys;

## [4.0.17] - 2021-03-03
### Fixed
- Installer fails to install embedded JRE in Windows;

## [4.0.16] - 2021-03-01
### Added
- Allow usage of calculated key attributes with "only one time" option;

## [4.0.15] - 2021-02-22
### Added
- Control Panel: updated to work with OpenJDK;
- Auto-updater: cleanup previous backup folder before updating;

### Fixed
- Tables: relevant multiple attribute column not shown;
- Tables: column grouping layout when inner columns are not visible;

## [4.0.14] - 2021-01-28
### Fixed
- Error generating polygon (geometry) in Collect Mobile

## [4.0.13] - 2021-01-28
### Added
- Schema labels export;

### Fixed
- Schema labels import: validate input file before import;

## [4.0.12] - 2021-01-26
### Fixed
- Taxon attribute: when "show family" is specified, always include family in CSV export and in data entry form;

## [4.0.11] - 2021-01-25
### Fixed
- Error after logout;
- Multiple entities with form layout: show position in selector if keys are missing;
- Error loading code list items in table when column visibility changes;

### Added
- Schema labels import;

## [4.0.10] - 2021-01-21
### Fixed
- Filtering of code items in code attribute autocomplete;
- Made qualifier attributes read-only in UI (for data entry users);

## [4.0.9] - 2021-01-20
### Fixed
- Page reload when saving survey from Survey Designer;

### Added
- Improved multiple entity form selector layout;

## [4.0.8] - 2021-01-16
### Fixed
- Hide not visible fields from taxon autocomplete;
- Record edit for users with ENTRY_LIMITED role;

## [4.0.7] - 2021-01-13
### Fixed
- Issue with special characters in Portuguese
- Error exporting Collect Earth survey with multiple code attribute inside multiple entity;

## [4.0.6] - 2021-01-11
### Fixed
- Data Management list of records not updated after import;
- Record editor: go back to data management page on survey change;

## [4.0.5] - 2021-01-09
### Fixed
- Code attribute: selected item label not shown in autocomplete;

## [4.0.4] - 2021-01-09
### Added
- Code attribute: allow clear selection;

## [4.0.3] - 2020-12-31
### Added
- Support form item multiple columns layout;

## [4.0.2] - 2020-12-27
### Added
- Truncate enumerator label (key code attribute item label) in enumerated entities (table);

### Fixed
- Clear record cache on survey publish/delete/update

## [4.0.1] - 2020-12-24
### Added
- Keep selected survey after browser page reload;

### Fixed
- Error exporting survey to Collect Mobile;

## [4.0.0] - 2020-12-18
### Added
- Removed client side depending on Flash Player!

## [3.26.36] - 2020-12-17
### Added
- Data entry new UI: show number label (if any); use label width specified in survey designer; handle system errors; export edited record to Excel; hide calculated attributes from UI (if specified); numeric attributes: limit decimal places;

### Fixed
- Data entry new UI: fixed value deleted from Taxon attribute after focusing into field without selection;

## [3.26.35] - 2020-12-15
### Added
- Data entry new UI: layout adjustments; fixed hierarchical code list items loading;

## [3.26.34] - 2020-12-14
### Added
- Data entry new UI: promote/demote record; support qualifiable code attributes in tables; hide non-relevant tabs;

## [3.26.33] - 2020-12-08
### Added
- Data entry new UI: record validation report; show records as read-only if in analysis phase;

## [3.26.32] - 2020-12-03
### Added
- Data entry new UI: show labels in selected survey language; show warnings feedback; show node definition tooltips (descriptions) and info icons; numeric attributes: do not show unit selector when only one unit is defined;

## [3.26.31] - 2020-12-02
### Fixed
- Data Management: fixed error selecting survey with versions.

### Added
- Data entry new UI / numeric fields: avoid scroll on input fields.

## [3.26.30] - 2020-12-01
### Fixed
- CE: fixed hierarchical code attribute not working in enumerated entities
- Dashboard: fixed error writing dates in period fields.
- Data entry new UI: fixed cannot enter 0 into numeric fields;

## [3.26.29] - 2020-11-25
### Fixed
- Fixed evaluation of relevancy when default value is applied.

### Added
- Data entry new UI: layout improvements; support Range attributes, Boolean attributes with text mode, form versions.

## [3.26.28] - 2020-11-18
### Fixed
- Fixed error showing/downloading uploaded images

### Added
- Data entry new UI: support calculated attributes (shown as disabled)

## [3.26.27] - 2020-11-15
### Added
- Data entry new UI / code attributes: support hierarchical code list; show code item description (as tooltip); support qualifiable code list attributes (specify text)

## [3.26.26] - 2020-11-03
### Fixed
- Fixed automatic login with "admin" user

## [3.26.25] - 2020-11-03
### Fixed
- Fixed error starting up Control Panel
- Upgraded libraries

## [3.26.24] - 2020-11-02
### Added
- Removed Planet API key field from CE surveys
- Activated Planet imagery by default when creating new project
- Data entry new UI: support column groupings in table (single entities inside multiple entities with table layout), Coordinate, Taxon, "memo" (large) Text, Time attributes

## [3.26.23] - 2020-10-14
### Fixed
- Error selecting survey from Data Management

## [3.26.22] - 2020-10-13
### Fixed
- Updated default password for admin user
### Added
- Data entry new UI: support multiple entities with table layout / coordinate attributes

## [3.26.21] - 2020-10-01
### Fixed
- Fixed check available updates

## [3.26.20] - 2020-10-01
### Fixed
- Fixed species list name uniqueness validation
### Added
- Deployment of updaters to Maven Central Repository

## [3.26.17] - 2020-09-18
### Fixed
- Survey preview not working properly
- Layout adjustments

## [3.26.16] - 2020-09-18
### Added
- Survey preview with new UI (without Flash Player) proof of concept

## [3.26.15] - 2020-08-17
### Fixed
- Improved loading of species list items (for Collect Mobile)

## [3.26.14] - 2020-07-24
### Fixed
- Fixed sampling point data table not refreshed after import when it was empty
- Fixed compatibility of collect-core with Collect Mobile

## [3.26.13] - 2020-07-03
### Added
- Removed need for Flash Player from Survey Designer Sampling Point Data and Species List editors.
- Allow exporting survey to SQL format from Survey Designer / List of Surveys.

## [3.26.12] - 2020-06-10
### Fixed
- Fixed error when exporting to CEP in case there is ancillary data shown in the headers.

## [3.26.11] - 2020-05-29
### Fixed
- Code attribute (text): allow inserting codes containing "-" character.

## [3.26.10] - 2020-05-24
### Fixed
- Fixed error exporting Collect Earth projects from Survey Designer.

## [3.26.9] - 2020-05-23
### Added
- Collect Earth 'Open Earth Map' option to survey designer

### Fixed
- Map tool: show record data layer on top of sampling point data layer.

## [3.26.8] - 2020-05-17
### Fixed
- 'idm:contains' function with numeric values
- 'Add node from another survey': copy code list items in correct target survey languages
- 'Schema summary' export / 'Required when' expression

## [3.26.7] - 2020-04-13
### Fixed
- Record edit from Map module

## [3.26.6] - 2020-03-25
### Fixed
- File dropzone not highlighted on file drop

## [3.26.5] - 2020-03-24
### Fixed
- Error "You are editing another record" in survey preview
### Added
- Coordinate attribute: altitude and accuracy in data export

## [3.26.4] - 2020-03-24
### Fixed
- Fixed hierarchical code list export to Excel

## [3.26.3] - 2020-03-23
### Added
- Removed Flash Player requirement in Survey Designer / Code List import

## [3.26.2] - 2020-03-20
### Added
- Added Collect Earth 'Open Secure Watch' and 'Open GEE App' options to survey designer

## [3.26.1] - 2020-03-17
### Fixed
- Fixed error publishing survey with Coordinate attributes with altitude/accuracy
- Fixed Map component projection

## [3.26.0] - 2020-03-04
### Added
- Added altitude and accuracy fields (optional) to Coordinate attributes

## [3.25.0] - 2020-01-13
### Added
- Added Reporting label (Saiku) to Schema summary export; changed exported type to Excel
- Added math:random() function to expression language

## [3.24.36] - 2019-12-13
### Added
- Added support for Collect Earth integration with Planet Maps

## [3.24.35] - 2019-12-05
### Fixed
- Fixed error uploading pictures with invalid characters in file name

## [3.24.34] - 2019-12-04
### Added
- Support filter by summary attributes in Collect Earth data export

## [3.24.33] - 2019-11-29
### Fixed
- MacOS installer: support latest version of OSx
- Fixed error editing surveys in PostgreSQL (id column exceeding maximum value)

## [3.24.32] - 2019-11-22
### Fixed
- CSV Data Import: fixed error importing CSV file (record not found) on surveys with Date  record key attributes

## [3.24.31] - 2019-11-17
### Fixed
- Excel data import: fixed error importing file with blank column headings
### Added
- Saiku: add Coordinate attributes to Dimensions

## [3.24.30] - 2019-11-11
### Fixed
- CSV data export: fixed blank values in ancestor columns
### Added
- Collect Earth: Turkish translation

## [3.24.29] - 2019-11-05
### Fixed
- Fixed new survey form issues (loose focus on text input)
### Added
- CSV data export option to avoid prepending grouping (single entity) name to column name

## [3.24.28] - 2019-10-24
### Fixed
- Fixed survey schema attributes import (error uploading CSV file)

## [3.24.27] - 2019-10-22
### Fixed
- Include all "enumerable" entities in CSV export when selecting "Include enumerated entities" option

## [3.24.26] - 2019-10-04
### Added
- Confirm on shutdown
- Added "Include enumerated entities" additional option to CSV/Excel export

## [3.24.25] - 2019-08-02
### Fixed
- Fixed user manager bugs

## [3.24.24] - 2019-06-10
### Fixed
- Surveys list sort by "Published" column
### Added
- Included severity (Error or Warning) in records validation report

## [3.24.23] - 2019-05-29
### Fixed
- Fixed error importing sampling point data
- Allow importing data generated with a survey where node definition cardinality was "single" and became "multiple"
- Fixed Saiku DB generation in PostgreSQL (coordinate attributes)

## [3.24.22] - 2019-05-17
### Fixed
- Fixed bug: Collect Earth survey export error

## [3.24.21] - 2019-05-16
### Fixed
- Fixed bug: code list items not showing properly in hierarchical code lists (sampling point data)
- Fixed bug: cannot publish survey with missing virtual entity node definitions

## [3.24.20] - 2019-05-09
### Added
- Added support to Extra Map URL (Collect Earth)

## [3.24.19] - 2019-04-10
### Fixed
- Default language not set properly in Collect Earth project export

## [3.24.18] - 2019-04-09
### Added
- Survey Guide survey file type

## [3.24.17] - 2019-04-05
### Fixed
- Fixed bug preventing survey publishing when there are only warnings in the survey

## [3.24.16] - 2019-04-03
### Fixed
- Collect Earth: edited plot not opening at last edited tab

## [3.24.15] - 2019-03-29
### Fixed
- Calculated attributes chain evaluation
