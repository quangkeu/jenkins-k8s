/* extends jquery validation */

$.validator.addMethod("unique",function (value, element) {
    var parentForm = $(element).closest('form');
    var timeRepeated = 0;
    $(parentForm.find('input[type="text"]')).each(function () {
        if ($(this)[0].hasAttribute("unique") && $(this).val() === value) {
            timeRepeated++;
        }
    });
    $(parentForm.find('select')).each(function () {
        if ($(this)[0].hasAttribute("unique") && $(this).val() === value) {
            timeRepeated++;
        }
    });
    return (timeRepeated === 1 || timeRepeated === 0);
});

$.validator.addMethod("notExisted",function(value,element) {
    var checkApi = $(element).attr('check-api');
    var existed = true;
    $.ajax({
        url: checkApi + value,
        method: 'GET',
        async: false,
        success: function(response) {
            existed = response;
        },
        error: function(error) {
            //not existed by default
        }
    });
    console.log(existed);
    return !existed;
});

$.validator.classRuleSettings.unique = {
    unique: true
};
$.validator.classRuleSettings.notExisted = {
    notExisted: true
};

$.validator.messages.unique = 'This field is duplicated';
$.validator.messages.notExisted = 'This value is already in use';

var ump = {
    validator : {
        ignore: 'input[type=hidden], .select2-search__field', // ignore hidden fields
        errorClass: 'validation-error-label',
        successClass: 'validation-success-label',
        validClass: "validation-valid-label",
        onkeyup: false,
        onfocusout: function(element) {
            if($(element).attr('type') !== 'file') {
                $(element).val($.trim($(element).val()));
            }
            $(element).valid();
        },
        highlight: function(element, errorClass) {
            $(element).removeClass(errorClass);
            $(element).parents('.form-group').first().find('.control-label').addClass('text-danger');
            $(element).addClass('border-bottom-danger')
        },
        unhighlight: function(element, errorClass) {
            $(element).removeClass(errorClass);
            $(element).parents('.form-group').first().find('.control-label').removeClass('text-danger');
            $(element).removeClass('border-bottom-danger');
        },

        // Different components require proper error label placement
        errorPlacement: function(error, element) {

            //<editor-fold desc="Add text error default">
            // Styled checkboxes, radios, bootstrap switch
            if (element.parents('div').hasClass("checker") || element.parents('div').hasClass("choice") || element.parent().hasClass('bootstrap-switch-container') ) {
                if(element.parents('label').hasClass('checkbox-inline') || element.parents('label').hasClass('radio-inline')) {
                    error.appendTo( element.parent().parent().parent().parent() );
                }
                else {
                    error.appendTo( element.parent().parent().parent().parent().parent() );
                }
            }

            // Unstyled checkboxes, radios
            else if (element.parents('div').hasClass('checkbox') || element.parents('div').hasClass('radio')) {
                error.appendTo( element.parent().parent().parent() );
            }

            // Input with icons and Select2
            else if (element.parents('div').hasClass('has-feedback') || element.hasClass('select2-hidden-accessible')) {
                error.appendTo( element.parent() );
            }

            // Inline checkboxes, radios
            else if (element.parents('label').hasClass('checkbox-inline') || element.parents('label').hasClass('radio-inline')) {
                error.appendTo( element.parent().parent() );
            }

            // Input group, styled file input
            else if (element.parent().hasClass('uploader') || element.parents().hasClass('input-group')) {
                error.appendTo( element.parent().parent() );
            }

            else {
                error.insertAfter(element);
            }
            //</editor-fold>

            var domElement = $(element)[0];
            if($(element).val() !== '' && (domElement.hasAttribute("unique") || domElement.hasAttribute("notExisted"))) {
                error.insertAfter(element);
            }
        },
        validClass: "validation-valid-label",
        success: function(label) {
            $(label).remove();
        },
        rules: {
        }
    },
    generateParameterInput: function (parameter) {
        var inputHtml = '';
        var value = parameter.value && parameter.value !== '' ? parameter.value : parameter.defaultValue;

        if(parameter.dataType === 'boolean') {
            if(value === 'true') {
                inputHtml = '<input type="checkbox" class="parameter-checkbox-input" data-parameterPath="'+parameter.path+'" checked="checked">' +
                    '<input type="text" class="parameter-checkbox-value inputParameter" data-parameterPath="'+parameter.path+'" name="path_'+parameter.path+'" value="true" style="display: none">';
            } else {
                inputHtml = '<input type="checkbox" class="parameter-checkbox-input" data-parameterPath="'+parameter.path+'">' +
                    '<input type="text" class="parameter-checkbox-value inputParameter" data-parameterPath="'+parameter.path+'" name="path_'+parameter.path+'" value="false" style="display: none">';
            }

        } else if(parameter.dataType === 'int' || parameter === 'unsignedInt') {
            if(parameter.rule) {
                parameter.rule = parameter.rule.replace('[', '');
                parameter.rule = parameter.rule.replace(']', '');
                if(parameter.rule.indexOf(';') > 0) {
                    inputHtml = '<select class="inputParameter" name="path_'+parameter.path+'">';

                    var options = parameter.rule.split(';');
                    var selected = '';
                    for(var i=0; i<options.length; i++) {
                        selected = value === options[i] ? ' selected = "selected" ' : '';
                        inputHtml += '<option value="'+options[i]+'" '+selected+'>'+options[i]+'</option>'
                    }

                    inputHtml += '</select>'

                } else if(parameter.rule.indexOf('-') > 0) {
                    options = parameter.rule.split('-');
                    inputHtml = '<input class="form-control inputParameter" type="number" min="'+options[0]+'" max="'+options[1]+'" name="path_'+parameter.path+'" value="'+value+'">';

                } else if(parameter.rule.indexOf('>') >= 0) {
                    options = parameter.rule.split('>');
                    inputHtml = '<input class="form-control inputParameter" type="number" min="'+options[1]+'" name="path_'+parameter.path+'" value="'+value+'">';

                } else if(parameter.rule.indexOf('<') >= 0) {
                    options = parameter.rule.split('<');
                    inputHtml = '<input class="form-control inputParameter" type="number" max="'+options[1]+'" name="path_'+parameter.path+'" value="'+value+'">';
                } else {
                    inputHtml = '<input class="form-control inputParameter" type="number" name="path_'+parameter.path+'" value="'+value+'">'
                }

            } else {
                inputHtml = '<input class="form-control inputParameter" type="number" name="path_'+parameter.path+'" value="'+value+'">';
            }


        } else if(parameter.dataType === 'string') {
            if(parameter.rule) {
                parameter.rule = parameter.rule.replace('[', '');
                parameter.rule = parameter.rule.replace(']', '');
                if(parameter.rule.indexOf(';') > 0) {
                    inputHtml = '<select class="form-control inputParameter" name="path_'+parameter.path+'">';

                    var options = parameter.rule.split(';');
                    var selected = '';
                    for(var i=0; i<options.length; i++) {
                        selected = value === options[i] ? ' selected = "selected" ' : '';
                        inputHtml += '<option value="'+options[i]+'" '+selected+'>'+options[i]+'</option>'
                    }

                    inputHtml += '</select>'

                } else if(parameter.rule.indexOf('-') > 0) {
                    options = parameter.rule.split('-');
                    inputHtml = '<input class="form-control inputParameter" type="text" minlength="'+options[0]+'" maxlength="" x="'+options[1]+'" name="path_'+parameter.path+'" value="'+value+'">';

                } else if(parameter.rule.indexOf('>') >= 0) {
                    options = parameter.rule.split('>');
                    inputHtml = '<input class="form-control inputParameter" type="text" minlength="'+options[1]+'" name="path_'+parameter.path+'" value="'+value+'">';

                } else if(parameter.rule.indexOf('<') >= 0) {
                    options = parameter.rule.split('<');
                    inputHtml = '<input class="form-control inputParameter" type="text" maxlength="'+options[1]+'" name="path_'+parameter.path+'" value="'+value+'">';
                } else {
                    inputHtml = '<input class="form-control inputParameter" type="text" name="path_'+parameter.path+'" value="'+value+'">'
                }
            } else {
                inputHtml = '<input class="form-control inputParameter" type="text" name="path_'+parameter.path+'" value="'+value+'">'
            }


        } else if(parameter.dataType === 'dateTime') {
            inputHtml = '' +
                '<div class="input-group">' +
                '   <input type="text" class="form-control" name="path_'+parameter.path+'" value="' + value + '" >' +
                '   <span class="input-group-addon"><i class="icon-calendar3"></i></span>' +
                '</div>' +
                '<input type="hidden" name="dataType_'+parameter.path+'" value="' + parameter.dataType + '" >';

        } else {
            inputHtml = '<input class="form-control inputParameter" type="text" name="path_'+parameter.path+'" value="'+value+'">'
        }

        return inputHtml;

    },
    getUrlQueryValue: function (queryName) {
        // Function get value query parameter
        // return null if query name not exist
        var locationSearch = decodeURIComponent(window.location.search.substring(1));
        var currentQueryArr = locationSearch.split('&');
        var result = null;

        if(locationSearch.length > 0) {
            for (var i = 0; i < currentQueryArr.length; i++) {
                var queryItem = currentQueryArr[i].split('=');
                var queryNameItem = queryItem[0];
                if(queryNameItem === queryName) {
                    if(queryItem.length >= 1) {
                        result = currentQueryArr[i].substring(queryName.length + 1)
                    }
                    break;
                }
            }
        }

        return result;

    },
    updateUrlQueryString: function (queryName, queryValue, callback) {
        // Function change value query parameter without reload page
        // Remove query parameter if value is null
        var locationSearch = decodeURIComponent(window.location.search.substring(1));
        var currentQueryArr = locationSearch.split('&');
        var isExistQueryName = false;

        if(locationSearch.length > 0) {
            for (var i = 0; i < currentQueryArr.length; i++) {
                var queryItem = currentQueryArr[i].split('=');
                var queryNameItem = queryItem[0];
                if(queryNameItem === queryName) {
                    currentQueryArr[i] = queryNameItem + '=' + queryValue;
                    if(queryValue == null) {
                        currentQueryArr.splice(i, 1);
                    }
                    isExistQueryName = true;
                    break;
                }
            }
        } else {
            currentQueryArr = [];
        }

        // Add new if query not exist
        if(queryValue != null && !isExistQueryName) {
            currentQueryArr.push(queryName + '=' + queryValue);
        }

        // Get new url search string
        var newLocationSearch = currentQueryArr.length > 0 ? '?' + currentQueryArr.join('&') : '';

        // Replace url without refresh page
        history.pushState({}, $(document).find("title").text(), location.origin + location.pathname + newLocationSearch);

        // Callback
        if (callback && typeof callback === "function") {
            callback();
        }
    },
};

$(function() {
    //validate form
    var validator = $(".form-validate-jquery").validate(ump.validator);
});