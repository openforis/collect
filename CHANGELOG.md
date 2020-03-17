# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
