# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.26.32] - NOT RELEASED YET
### Added
- Data entry new UI: show labels in selected survey language; show warnings feedback.

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
