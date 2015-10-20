/**
 * jQuery DoubleList plugin with Bootstrap styling v1.0
**/
(function($) {
    /** Initializes the DoubleList code as jQuery plugin. */
	
	function DoubleList(element, paramOptions, selected) {
		var defaults = {
                uri:        'local.json',       // JSON file that can be opened for the data.
                valueKey:   'id',               // Value that is assigned to the value field in the option.
                labelKey:   'name',             // Text that is assigned to the option field.
                title:      'Example',          // Title of the dual list box.
                json:       true,               // Whether to retrieve the data through JSON.
                filterApplyDelay:500,           // Timeout for when a filter search is started.
                horizontal: false,              // Whether to layout the dual list box as horizontal or vertical.
                maxTextLength: 90,              // Maximum text length that is displayed in the select.
                moveAllBtn: true,               // Whether the append all button is available.
                maxAllBtn:  500,                // Maximum size of list in which the all button works without warning. See below.
                selectClass:'form-control',
                listHtmlFormat: 				//the format string will be filled by 4 parameters: 
                								// - column size (6 when horizontal buttons are shown, 5 otherwise)
                								// - list class prefix (unselected or selected)
                								// - horizontal buttons HTML
                								// - select class 
	            	'   <div class="col-md-{0}">' +
					'       <h4><span class="{1}-title"></span> <small>- showing <span class="{1}-count"></span></small></h4>' +
					'       <input class="filter form-control filter-{1}" type="text" placeholder="Filter" style="margin-bottom: 5px;">' +
					'		{2}' +
					'       <select class="{1} {3}" style="height: 200px; width: 100%;" multiple></select>' +
					'   </div>',
                warning:    'Are you sure you want to move this many items? Doing so can cause your browser to become unresponsive.'
        };

        var htmlOptions = element.data();
        
        var options = $.extend({}, defaults, htmlOptions, paramOptions);

        $.each(options, function(i, item) {
            if (item === undefined || item === null) { throw 'DoubleList: ' + i + ' is undefined.'; }
        });
        	
        this.options = options;
        this.originalElement = $(element.context);
        this.parentElement = this.originalElement.parent();

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
			var valueKey = $this.options.valueKey;
			$.each(items, function(i, item) {
				var itemValue = item[valueKey];
				var optionEl = $this.unselectedList.find('option[value="' + itemValue + '"]');
				if (optionEl.length > 0) {
					optionEl.remove().appendTo($this.selectedList);
				}
			});
		},
		getSelectedValues : function() {
			var $this = this;
			var values = new Array();
			var options = $this.selectedList.find('option').each(function(i, option) {
				values.push(option.value);
			});
			return values;
		},
		
		/** Retrieves all the option elements through a JSON request. */
		_addElementsViaJSON : function(selected) {
			var $this = this;
			var options = this.options;
			var multipleTextFields = false;
			
			if (options.labelKey.indexOf(':') > -1) {
				var textToUse = options.labelKey.split(':');
				
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
						text = item[options.labelKey];
					}
					
					$('<option>', {
						value: item[options.valueKey],
						text: text,
						selected: (selected.some(function (e) { return e[options.valueKey] === item[options.valueKey] }) === true)
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
			var unselectedList = this.unselectedList;
			var selectedList = this.selectedList;
			
			this.parentElement.find('button').bind('click', function() {
				switch ($(this).data('type')) {
				case 'str': /* Selected to the right. */
					moveSelectedOption(unselectedList, selectedList);
					$(this).prop('disabled', true);
					break;
				case 'atr': /* All to the right. */
					moveVisibleOptions(unselectedList, selectedList);
					break;
				case 'stl': /* Selected to the left. */
					moveSelectedOption(selectedList, unselectedList);
					$(this).prop('disabled', true);
					break;
				case 'atl': /* All to the left. */
					moveVisibleOptions(selectedList, unselectedList);
					break;
				default: break;
				}
				
				$this._updateFilteredItems();
				
				$this._handleMovement();
			});
			
			function moveSelectedOption(fromSelect, toSelect) {
				fromSelect.find('option:selected').appendTo(toSelect);
			}
			
			function moveVisibleOptions(fromSelect, toSelect) {
				var optionElements = fromSelect.find('option');
				if (optionElements.length < options.maxAllBtn || confirm(options.warning)) {
					optionElements.each(function () {
						if (isVisible($(this))) {
							$(this).appendTo(toSelect);
						}
					});
				}
			};
			
			this.parentElement.closest('form').submit(function() {
				selectedList.find('option').prop('selected', true);
			});
			
			this.parentElement.find('input[type="text"]').keypress(function(e) {
				if (e.which === 13) {
					event.preventDefault();
				}
			});
			
			this.selectedItemsFilter.bind('change keyup', $.proxy($this._filterChangeHandler, $this, selectedList));
			this.unselectedItemsFilter.bind('change keyup', $.proxy($this._filterChangeHandler, $this, unselectedList));
			
			$this._updateFilteredItems();
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
			
			this.unselectedList.find('option').each(function() { if (isVisible($(this))) { countUnselected++; } });
			this.selectedList.find('option').each(function() { if (isVisible($(this))) { countSelected++ } });
			
			this.unselectedCountField.text(countUnselected);
			this.selectedCountField.text(countSelected);
			
			this._toggleButtons();
		},
		
		_filterChangeHandler : function(select, event) {
			var text = $(event.target).val();
			this._filterItems(select, text);
		},
		
		_updateFilteredItems : function() {
			this._filterItems(this.selectedList, this.selectedItemsFilter.val());
			this._filterItems(this.unselectedList, this.unselectedItemsFilter.val());
		},
		
		_filterItems : function(select, text) {
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

			select.scrollTop(0);
			sortOptions(select);
		},
		
		/** Creates a new dual list box with the right buttons and filter. */
		_createDoubleList : function() {
			var $this = this;
			var options = this.options;
			var colSize = options.horizontal == false ? 5 : 6;
			var unselectedHorizontalButtonsHtml = options.horizontal == false ? '' : $this._createHorizontalButtons(1, options.moveAllBtn);
			var selectedHorizontalButtonsHtml = options.horizontal == false ? '' : $this._createHorizontalButtons(2, options.moveAllBtn);
			
			this.parentElement.addClass('row').append(
					formatString($this.options.listHtmlFormat, colSize, 'unselected', unselectedHorizontalButtonsHtml, options.selectClass) +
					(options.horizontal == false ? $this._createVerticalButtons(options.moveAllBtn) : '') +
					formatString($this.options.listHtmlFormat, colSize, 'selected', selectedHorizontalButtonsHtml, options.selectClass)
			);
			
			this.selectedList = this.parentElement.find(".selected");
			this.unselectedList = this.parentElement.find(".unselected");
			this.selectedItemsFilter = this.parentElement.find('.filter-selected');
			this.unselectedItemsFilter = this.parentElement.find('.filter-unselected');
			this.selectedCountField = this.parentElement.find('.selected-count');
			this.unselectedCountField = this.parentElement.find('.unselected-count');
			this.selectedToRightBtn = this.parentElement.find('.str');
			this.selectedToLeftBtn = this.parentElement.find('.stl');
			this.allToRightBtn = this.parentElement.find('.atr');
			this.allToLeftBtn = this.parentElement.find('.atl');
			
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
			
			this.originalElement.find('option').text(function (i, text) {
				$(this).data('title', text);
				
				if (text.length > options.maxTextLength) {
					textIsTooLong = true;
					return text.substr(0, options.maxTextLength) + '...';
				}
			});
			this.originalElement.find('option:not(selected)').appendTo($this.unselectedList);
			this.originalElement.find('option:selected').appendTo($this.selectedList);
			
			this.originalElement.find('option:not(selected)').appendTo($this.unselectedList);
			this.originalElement.find('option:selected').appendTo($this.selectedList);
			
			this.originalElement.remove();
			this._handleMovement();
		},
		
		/** Toggles the buttons based on the length of the selects. */
		_toggleButtons : function() {
			var $this = this;
			var parentElement = this.parentElement;
			this.unselectedList.change(function() {
				$this.selectedToRightBtn.prop('disabled', false);
			});
			
			this.selectedList.change(function() {
				$this.selectedToLeftBtn.prop('disabled', false);
			});
			
			if (this.unselectedList.has('option').length == 0) {
				$this.allToRightBtn.prop('disabled', true);
				$this.selectedToRightBtn.prop('disabled', true);
			} else {
				$this.allToRightBtn.prop('disabled', false);
			}
			
			if (this.selectedList.has('option').length == 0) {
				$this.allToLeftBtn.prop('disabled', true);
				$this.selectedToLeftBtn.prop('disabled', true);
			} else {
				$this.allToLeftBtn.prop('disabled', false);
			}
		}
		
	};
		
   /** Checks whether or not an element is visible. The default jQuery implementation doesn't work. */
    var isVisible = function(el) {
        return !(el.css('visibility') == 'hidden' || el.css('display') == 'none');
    };

    /** Sorts options in a select / list box. */
    var sortOptions = function(select) {
        select.append(select.find('option').remove().sort(function(a, b) {
            var at = $(a).text(), bt = $(b).text();
            return (at > bt) ? 1 : ((at < bt) ? -1 : 0);
        }));
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
    
    var formatString = function(format) {
        var args = Array.prototype.slice.call(arguments, 1);
    	return format.replace(/{(\d+)}/g, function(match, number) {
    		return typeof args[number] != 'undefined' ? args[number] : match;
    	});
    };
    
    $.fn.doubleList = function(paramOptions, selected) {
    	return this.each(function () {
            $(this).data("doubleList", new DoubleList($(this), paramOptions, selected));
        })
    };
})(jQuery);