/**
 * jQuery DoubleList plugin with Bootstrap styling v1.0
**/
(function($) {
    /** Initializes the DoubleList code as jQuery plugin. */
	
	function DoubleList(element, paramOptions, selected) {
		var defaults = {
                uri:        'local.json',       // JSON file that can be opened for the data.
                value:      'id',               // Value that is assigned to the value field in the option.
                text:       'name',             // Text that is assigned to the option field.
                title:      'Example',          // Title of the dual list box.
                json:       true,               // Whether to retrieve the data through JSON.
                filterApplyDelay:500,           // Timeout for when a filter search is started.
                horizontal: false,              // Whether to layout the dual list box as horizontal or vertical.
                maxTextLength: 45,              // Maximum text length that is displayed in the select.
                moveAllBtn: true,               // Whether the append all button is available.
                maxAllBtn:  500,                // Maximum size of list in which the all button works without warning. See below.
                selectClass:'form-control',
                warning:    'Are you sure you want to move this many items? Doing so can cause your browser to become unresponsive.'
        };

        var htmlOptions = {
            uri:        element.data('source'),
            value:      element.data('value'),
            text:       element.data('text'),
            title:      element.data('title'),
            json:       element.data('json'),
            filterApplyDelay:    element.data('filterApplyDelay'),
            horizontal: element.data('horizontal'),
            maxTextLength: element.data('maxTextLength'),
            moveAllBtn: element.data('moveAllBtn'),
            maxAllBtn:  element.data('maxAllBtn'),
            selectClass:element.data('selectClass')
        };
        
        var options = $.extend({}, defaults, htmlOptions, paramOptions);

        $.each(options, function(i, item) {
            if (item === undefined || item === null) { throw 'DoubleList: ' + i + ' is undefined.'; }
        });
        	
        this.options = options;
        this.element = $(element.context);
        this.parentElement = this.element.parent();

        selected = $.extend([{}], selected);

        if (options.json) {
            this._addElementsViaJSON(selected);
        } else {
            this._buildUI();
        }
	}
	
	DoubleList.prototype = {
		selectItems : function(items) {
			var $this = this;
			var valueKey = $this.options.value;
			$.each(items, function(i, item) {
				var itemValue = item[valueKey];
				var optionEl = $this.unselectedList.find('option[value="' + itemValue + '"]');
				if (optionEl.length > 0) {
					optionEl.remove().appendTo($this.selectedList);
				}
			});
		},
		getSelectedItemIds : function() {
			var $this = this;
			var ids = new Array();
			var options = $this.selectedList.find('option').each(function(i, option) {
				ids.push(option.value);
			});
			return ids;
		},
		
		/** Retrieves all the option elements through a JSON request. */
		_addElementsViaJSON : function(selected) {
			var $this = this;
			var options = this.options;
			var multipleTextFields = false;
			
			if (options.text.indexOf(':') > -1) {
				var textToUse = options.text.split(':');
				
				if (textToUse.length > 1) {
					multipleTextFields = true;
				}
			}
			
			$.getJSON(options.uri, function(json) {
				$.each(json, function(key, item) {
					var text = '';
					
					if (multipleTextFields) {
						textToUse.forEach(function (entry) { text += item[entry] + ' '; });
					} else {
						text = item[options.text];
					}
					
					$('<option>', {
						value: item[options.value],
						text: text,
						selected: (selected.some(function (e) { return e[options.value] === item[options.value] }) === true)
					}).appendTo(options.element);
				});
				
				$this._buildUI();
			});
		},
		
		/** Adds the event listeners to the buttons and filters. */
		_addListeners : function() {
			var $this = this;
			var options = this.options;
			var parentElement = this.parentElement;
			var unselected = this.unselectedList;
			var selected = this.selectedList;
			
			this.parentElement.find('button').bind('click', function() {
				switch ($(this).data('type')) {
				case 'str': /* Selected to the right. */
					unselected.find('option:selected').appendTo(selected);
					$(this).prop('disabled', true);
					break;
				case 'atr': /* All to the right. */
					var optionElements = unselected.find('option');
					if (optionElements.length >= options.maxAllBtn && confirm(options.warning) ||
							optionElements.length < options.maxAllBtn) {
						optionElements.each(function () {
							if ($(this).isVisible()) {
								$(this).remove().appendTo(selected);
							}
						});
					}
					break;
				case 'stl': /* Selected to the left. */
					selected.find('option:selected').remove().appendTo(unselected);
					$(this).prop('disabled', true);
					break;
				case 'atl': /* All to the left. */
					var selectedOptionElements = selected.find('option');
					if (selectedOptionElements.length >= options.maxAllBtn && confirm(options.warning) ||
							selectedOptionElements.length < options.maxAllBtn) {
						selectedOptionElements.each(function () {
							if ($(this).isVisible()) {
								$(this).remove().appendTo(unselected);
							}
						});
					}
					break;
				default: break;
				}
				
				$this._updateFilteredItems();
				
				$this._handleMovement();
			});
			
			this.parentElement.closest('form').submit(function() {
				selected.find('option').prop('selected', true);
			});
			
			this.parentElement.find('input[type="text"]').keypress(function(e) {
				if (e.which === 13) {
					event.preventDefault();
				}
			});
			
			$this._updateFilteredItems();
			
			this.selectedItemsFilter.bind('change keyup', function() {
                $this._filterSelectByText(selected, $(this).val());
            }).scrollTop(0).sortOptions();
			
			this.unselectedItemsFilter.bind('change keyup', function() {
                $this._filterSelectByText(unselected, $(this).val());
            }).scrollTop(0).sortOptions();
		},
		
		/** Constructs the jQuery plugin after the elements have been retrieved. */
		_buildUI : function() {
			this._createDoubleList();
			this._parseStubListBox();
			this._addListeners();
		},
		
		/** Counts the elements per list box/select and shows it. */
		_updateSelectedElementsCount : function(parentElement) {
			var countUnselected = 0, countSelected = 0;
			
			this.unselectedList.find('option').each(function() { if ($(this).isVisible()) { countUnselected++; } });
			this.selectedList.find('option').each(function() { if ($(this).isVisible()) { countSelected++ } });
			
			this.unselectedCountField.text(countUnselected);
			this.selectedCountField.text(countSelected);
			
			this._toggleButtons();
		},
		
		_filterSelectByText : function(select, text) {
			var $this = this;
			delay(function() {
                var options = $(select).find('option');
                var search = $.trim(text);
                search = escapeRegExp(search);
                var regex = new RegExp(search,'i');

                $.each(options, function(i, option) {
                	var $option = $(option);
                	var title = $option.prop("title");
                	var titleMatches = regex.test(title);
                	$option.toggle(titleMatches);
                });
                $this._updateSelectedElementsCount();
            }, $this.options.filterApplyDelay);
		},
		
		_updateFilteredItems : function() {
			this._filterSelectByText(this.selectedList, this.selectedItemsFilter.val());
			this.selectedList.scrollTop(0).sortOptions();;
			this._filterSelectByText(this.unselectedList, this.unselectedItemsFilter.val());
			this.unselectedList.scrollTop(0).sortOptions();;
		},
		
		/** Creates a new dual list box with the right buttons and filter. */
		_createDoubleList : function() {
			var $this = this;
			var options = this.options;
			this.parentElement.addClass('row').append(
					(options.horizontal == false ? 
					'   <div class="col-md-5">' : '   <div class="col-md-6">') +
					'       <h4><span class="unselected-title"></span> <small>- showing <span class="unselected-count"></span></small></h4>' +
					'       <input class="filter form-control filter-unselected" type="text" placeholder="Filter" style="margin-bottom: 5px;">' +
					(options.horizontal == false ? '' : $this._createHorizontalButtons(1, options.moveAllBtn)) +
					'       <select class="unselected ' + options.selectClass + '" style="height: 200px; width: 100%;" multiple></select>' +
					'   </div>' +
					(options.horizontal == false ? $this._createVerticalButtons(options.moveAllBtn) : '') +
					(options.horizontal == false ? 
					'   <div class="col-md-5">' : '   <div class="col-md-6">') +
					'       <h4><span class="selected-title"></span> <small>- showing <span class="selected-count"></span></small></h4>' +
					'       <input class="filter form-control filter-selected" type="text" placeholder="Filter" style="margin-bottom: 5px;">' +
					(options.horizontal == false ? '' : $this._createHorizontalButtons(2, options.moveAllBtn)) +
					'       <select class="selected ' + options.selectClass + '" style="height: 200px; width: 100%;" multiple></select>' +
					'   </div>');
			
			this.selectedList = this.parentElement.find(".selected");
			this.unselectedList = this.parentElement.find(".unselected");
			this.selectedItemsFilter = this.parentElement.find('.filter-selected');
			this.unselectedItemsFilter = this.parentElement.find('.filter-unselected');
			this.selectedCountField = this.parentElement.find('.selected-count');
			this.unselectedCountField = this.parentElement.find('.unselected-count');
			
			this.selectedList.prop('name', $(options.element).prop('name'));
			this.parentElement.find('.unselected-title').text('Available ' + options.title);
			this.parentElement.find('.selected-title').text('Selected ' + options.title);
		},
		
		/** Creates the buttons when the dual list box is set in horizontal mode. */
		_createHorizontalButtons : function(number, copyAllBtn) {
			if (number == 1) {
				return (copyAllBtn ? 
				'       <button type="button" class="btn btn-default atr" data-type="atr" style="margin-bottom: 5px;"><span class="glyphicon glyphicon-list"></span> <span class="glyphicon glyphicon-chevron-right"></span></button>': '') +
				'       <button type="button" class="btn btn-default ' + (copyAllBtn ? 'pull-right col-md-6' : 'col-md-12') + ' str" data-type="str" style="margin-bottom: 5px;" disabled><span class="glyphicon glyphicon-chevron-right"></span></button>';
			} else {
				return 
				'       <button type="button" class="btn btn-default ' + (copyAllBtn ? 'col-md-6' : 'col-md-12') + ' stl" data-type="stl" style="margin-bottom: 5px;" disabled><span class="glyphicon glyphicon-chevron-left"></span></button>' +
				(copyAllBtn ? 
				'       <button type="button" class="btn btn-default col-md-6 pull-right atl" data-type="atl" style="margin-bottom: 5px;"><span class="glyphicon glyphicon-chevron-left"></span> <span class="glyphicon glyphicon-list"></span></button>' 
				: '');
			}
		},
		
		/** Creates the buttons when the dual list box is set in vertical mode. */
		_createVerticalButtons : function(copyAllBtn) {
			return '   <div class="col-md-1 center-block" style="margin-top: ' + (copyAllBtn ? '80px' : '130px') +'">' +
			(copyAllBtn ? 
			'       <button type="button" class="btn btn-default atr" data-type="atr" style="margin-bottom: 10px;"><span class="glyphicon glyphicon-list"></span> <span class="glyphicon glyphicon-chevron-right"></span></button>' : '') +
			'       <button type="button" class="btn btn-default str" data-type="str" style="margin-bottom: 20px;" disabled><span class="glyphicon glyphicon-chevron-right"></span></button>' +
			'       <button type="button" class="btn btn-default stl" data-type="stl" style="margin-bottom: 10px;" disabled><span class="glyphicon glyphicon-chevron-left"></span></button>' +
			(copyAllBtn ? 
			'       <button type="button" class="btn btn-default atl" data-type="atl" style="margin-bottom: 10px;"><span class="glyphicon glyphicon-chevron-left"></span> <span class="glyphicon glyphicon-list"></span></button>' : '') +
			'   </div>';
		},
		
		/** Specifically handles the movement when one or more elements are moved. */
		_handleMovement : function () {
			//unselect all options
			this.unselectedList.find('option:selected').prop('selected', false);
			this.selectedList.find('option:selected').prop('selected', false);
			
			//reset filters
			this.parentElement.find('.filter').val('');
			//show all select options
			this.parentElement.find('select').find('option').each(function() { $(this).show(); });
			
			this._updateSelectedElementsCount();
		},
		
		/** Parses the stub select / list box that is first created. */
		_parseStubListBox : function() {
			var $this = this;
			var options = this.options;
			
			var textIsTooLong = false;
			
			this.element.find('option').text(function (i, text) {
				$(this).data('title', text);
				
				if (text.length > options.maxTextLength) {
					textIsTooLong = true;
					return text.substr(0, options.maxTextLength) + '...';
				}
			}).each(function () {
				if (textIsTooLong) {
					$(this).prop('title', $(this).data('title'));
				}
				var list;
				if ($(this).is(':selected')) {
					list = $this.selectedList;
				} else {
					list = $this.unselectedList;
				}
				$(this).appendTo(list);
			});
			
			this.element.remove();
			this._handleMovement();
		},
		
		/** Toggles the buttons based on the length of the selects. */
		_toggleButtons : function() {
			var parentElement = this.parentElement;
			this.unselectedList.change(function() {
				parentElement.find('.str').prop('disabled', false);
			});
			
			this.selectedList.change(function() {
				parentElement.find('.stl').prop('disabled', false);
			});
			
			if (this.unselectedList.has('option').length == 0) {
				parentElement.find('.atr').prop('disabled', true);
				parentElement.find('.str').prop('disabled', true);
			} else {
				parentElement.find('.atr').prop('disabled', false);
			}
			
			if (this.selectedList.has('option').length == 0) {
				parentElement.find('.atl').prop('disabled', true);
				parentElement.find('.stl').prop('disabled', true);
			} else {
				parentElement.find('.atl').prop('disabled', false);
			}
		}
		
	};
		
    $.fn.doubleList = function(paramOptions, selected) {
    	return this.each(function () {
            $(this).data("doubleList", new DoubleList($(this), paramOptions, selected));
        })
    };

    /** Checks whether or not an element is visible. The default jQuery implementation doesn't work. */
    $.fn.isVisible = function() {
        return !($(this).css('visibility') == 'hidden' || $(this).css('display') == 'none');
    };

    /** Sorts options in a select / list box. */
    $.fn.sortOptions = function() {
        return this.each(function() {
            $(this).append($(this).find('option').remove().sort(function(a, b) {
                var at = $(a).text(), bt = $(b).text();
                return (at > bt) ? 1 : ((at < bt) ? -1 : 0);
            }));
        });
    };
    
    var specialCharactersRegExp = /[.?*+^$[\]\\(){}|-]/g;
    
    var escapeRegExp = function(text) {
	  var result = text.replace(specialCharactersRegExp, '\\$&');
	  return result;
	}

    /** Simple delay function that can wrap around an existing function and provides a callback. */
    var delay = (function() {
        var timer = 0;
        return function (callback, ms) {
            clearTimeout(timer);
            timer = setTimeout(callback, ms);
        };
    })();
})(jQuery);