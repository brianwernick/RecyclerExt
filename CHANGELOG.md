# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.0.0] - 2021-01-17
### Changed
 - Updated support libraries to android X
 - Translated to Kotlin
 - `StickyHeaderDecoration` now tracks the actual header position
 - Added the ability to get the child index to header adapters with `getChildIndex(adapterPosition)`


## [3.0.1] - 2018-05-10
### Fixed
 - Fixed `HeaderListAdapter` incorrect item size



## [3.0.0] - 2018-05-10
### Changed
 - Updated the `minSdk` to `14` (was `9`)
 - Updated support library to `27.1.1`
 - Sticky Headers are now clickable
 - Sticky Header transitions are now smooth
 - Sticky Headers now use ViewHolders

### Fixed
 - `FastScroll` animations are now consistent

### Removed
 - `CursorAdapter`s have been removed



## [2.1.1] - 2017-04-01
### Changed
 - Updated support library to `25.3.1`
 
### Fixed
 - `FastScroll` now correctly stays hidden on load



## [2.1.0] - 2017-01-25
### Added
 - Reorder support added to the `CursorAdapter`
 - `ItemStatefulTouchHelper`

### Changed
 - Updated dependencies

### Deprecated
 - `CursorAdapter` deprecated due to inherent issues with long-lived cursors



## [2.0.1] - 2016-10-31
### Fixed
 - `FastScroll` track clicking now works as expected



## [2.0.0] - 2016-10-24
## Added
 - `FastScroll` can now be hidden on short lists

### Changed
 - Updated the `minSdk` version to `9` (was `7`)
 - Updated support library to `24.2.1`
 - Added a callback with `sectionId` to the `FastScroll` for improved popup change determination
 - `FastScroll` now uses the finger position for the scroll instead of the center
 - Optimized header calculations
 - `FastScrollPopupCallbacks` renamed to `PopupCallbacks`
 
### Fixed
 - Fixed inconsistent positioning in the `FastScroll` between dragging and normal scrolling



## [1.3.1] - 2016-08-17
### Changed
 - `FastScroll` smoothed out, particularly with short lists

### Fixed
 - Vector drawables now work correctly on pre-Lollipop devices
 - `FastScroll` drag handle now stays visible during a drag



## [1.3.0] - 2016-08-11
### Changed
 - Added drag handle animations to `FastScroll`
 - Added popup alignment options to `FastScroll`
 - Track clicks can now be disabled in `FastScroll`

### Fixed
 - Smooth scrolling now works as expected



## [1.2.0] - 2016-08-02
### Updated
 - Adapter position can now be determined from a child position

### Changed
 - Updated support library to `24.1.1`
 
### Deprecated
 - `determineChildPosition()` deprecated in favor or `getChildPosition()`



## [1.1.2] - 2016-06-24
### Changed 
 - Updated support library to `24.0.0`



## [1.1.1] - 2016-04-20
### Fixed
 - `StickyHeaderDecoration` now works correctly with the `RecyclerHeaderListAdapter`


## [1.1.0] - 2016-04-13
### Added
 - `RecyclerHeaderListAdapter`

### Updated
 - Added `set()` to `RecyclerListAdapter`

### Changed
 - Support library updated to `23.3.0`



## [1.0.2] - 2016-03-23
### Fixed
 - `FastScroll` now properly shows text bubble when dynamically enabling/disabling


## [1.0.1] - 2016-03-22
### Fixed
 - `AutoColumnGridLayoutManager` sizing and spacing


## [1.0.0] - 2016-03-13
### Changed
 - Updated support library versions



## [0.18.0] - 2016-03-10
### Fixed
 - `AutoColumnGridLayoutManager` spacing issues caused by support `23.2.0`



## [0.18.0] - 2016-03-02
### Fixed
 - `AutoColumnGridLayoutManager` sizing issues caused by support `23.2.0`

### Removed
 - `WrapLinearLayoutManager` due to the support library `23.2.0` adding native support



## [0.17.1] - 2016-01-29
### Fixed
 - `FastScroll` resource issue



## [0.17.0] - 2016-01-27
### Added
 - `FastScroll` widget



## [0.16.0] - 2016-01-19
### Added
 - Item touch helper callback to support elevation changes



## [0.15.0] - 2016-01-07
### Added
 - `SpacerDecoration`
 
### Changed
 - `AutoColumGridLayoutManager` now supports specifying the max column count, edge separators, and separator sizes



## [0.14.0] - 2015-12-12
### Added
 - Sticky headers can now be made from individual views instead of the entire holder
 - Items can now be treated as a header (see Lollipop & Marshmallow contacts)

### Changed
 - Removed code duplication in the header adapters




## [0.13.0] - 2015-12-11
### Added
 - `ViewTypes` are now available in the header adapters



## [0.12.4] - 2015-12-10
### Fixed
 - `WrapLinearLayoutManager` now abides by `RecyclerView` padding
 - `WrapLinearLayoutManager` now properly wraps in vertical and horizontal lists



## [0.12.3] - 2015-10-22
### Added
 - Created view holders to support clicks and menus

### Fixed
 - methods are now properly available



## [0.12.1] - 2015-10-21
### Changed
 - Added ability to specify a min amount of spacing between cards in the `GridLayoutManager`
 - Added child count to the `HeaderAdapter`s 



## [0.12.0] - 2015-10-19
### Changed
 - Added `swap` and `move` methods to the `RecyclerListAdapter`
 - Updated support library versions

### Fixed
 - General issues with `StickyHeaderDecoration`
 


## [0.11.1] - 2015-10-15
### Changed
 - `ListAdapter` now notifies of data range changes to support animations

### Fixed
 - `GridLayoutManager` now correctly abides by padding
 


## [0.11.0] - 2015-10-12
### Added
 - `GridLayoutManager` that auto-determines column count based on item size

### Changed
 - Rewrote `StickyHeaderDecoration` to properly handle data changes



## [0.10.4] - 2015-09-17
### Changed
 - `RecyclerHeaderCursorAdapter` and `RecyclerHeaderAdapter` updated to standardized methods and parameters



## [0.10.0] - 2015-08-11
### Added
 - `ListAdapter`

### Changed
 - Minor updates to the `CursorAdapter`



## [0.8.0] - 2015-07-02
### Changed
 - `ReorderDecoration` now has smooth reorder animations



## [0.7.0] - 2015-04-12
### Added
 - `CursorAdapter`
 - `ReorderDecoration`