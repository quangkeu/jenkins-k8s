$(function () {

    console.debug('START POLICY CONFIGURATION');

    // DECLARE VARIABLE
    var parameters = [];
    var parametersByPath = [];
    var parametersSelected = [];
    var parametersAutocomplete = [];

    // DECLARE HTML VARIABLE
    var policy_configuration_form = $('.policy-configuration-form');
    var policy_configuration_validator;

    // FIRST LOAD PAGE
    firstLoadPage();


    // EVENT IN PAGE
    $('.add-parameter-btn').click(function () {
        var pathStr = $('.add-parameter-input').val();
        if(parametersSelected.indexOf(pathStr) < 0) {
            var parameterInsert = (typeof parametersByPath[pathStr] !== 'undefined') ? parametersByPath[pathStr] : {path: pathStr, defaultValue: ''};
            addParameter(parameterInsert);
        }
    });

    $('input[name=selectedTargetedDevices]').change(function () {
        generateValidateSelectedTargetedDevices($(this).val())
    });

    $('input[name=schedule]').change(function () {
        generateValidateSchedule($(this).val())
    });


    //<editor-fold desc="FORM PRIVATE FUNCTION">
    function firstLoadPage() {
        initLibrary();
        getListParameters();
        generateValidateSelectedTargetedDevices($('input[name=selectedTargetedDevices][checked=checked]').val());
        generateValidateSchedule($('input[name=schedule][checked=checked]').val());
    }
    
    function initLibrary() {

        $("#anytime-both").AnyTime_picker({
            format: "%d-%m-%Y %H:%i"
        });

        $(".file-styled").uniform({
            fileButtonClass: 'action btn btn-default'
        });

        $('.device-group-select2').select2({
            placeholder: '- Select a group device -'
        });

        $('.add-parameter-input').autocomplete({
            source: []
        });

        policy_configuration_validator = policy_configuration_form.validate(ump.validator);

    }

    function getListParameters() {
        $.ajax({
            type: 'GET',
            url: location.origin + '/policy-configuration' + '/search-parameters',
            success: function (response) {
                parameters = response;
                parametersByPath = [];
                parametersAutocomplete = [];
                $.each(response, function (index, value) {
                    parametersByPath[value.path] = value;
                    parametersAutocomplete.push(value.path);
                });

                initAutocompleteParameter();
            }
        });
    }

    function initAutocompleteParameter() {
        parametersAutocomplete = [];
        $.each(parameters, function (index, value) {
            if(parametersSelected.indexOf(value.path) < 0) {
                parametersAutocomplete.push(value.path)
            }
        });
        policy_configuration_form.find(".add-parameter-input").autocomplete('destroy').autocomplete({
            source: parametersAutocomplete
        });
    }

    function initRemoveParameter() {
        policy_configuration_form.find('.remove-parameter-btn').unbind( "click" )
            .click(function () {
                var pathRemove = $(this).attr('data-parameterPath');
                policy_configuration_form.find('.list-parameters').find('tr[data-parameterPath="'+pathRemove+'"]').remove();
                parametersSelected.splice(parametersSelected.indexOf(pathRemove), true);
                initAutocompleteParameter();
            });
    }

    function initParameterCheckboxInput() {
        policy_configuration_form.find('.parameter-checkbox-input').unbind( "click" )
            .click(function () {
                var pathCheckbox = $(this).attr('data-parameterPath');

                if($(this).is(":checked")) {
                    policy_configuration_form.find('.parameter-checkbox-value[data-parameterPath="'+pathCheckbox+'"]').val('true');
                } else {
                    policy_configuration_form.find('.parameter-checkbox-value[data-parameterPath="'+pathCheckbox+'"]').val('false');
                }

            });
    }

    function addParameter(parameterInsert) {
        if(parameterInsert !== null && parameterInsert.path !== '') {

            $('.list-parameters-no-data').hide();

            // Add html
            var newRow = $('.list-parameters-template').html();
            newRow = newRow.split('{path}').join(parameterInsert.path);
            newRow = newRow.split('{inputParameter}').join(ump.generateParameterInput(parameterInsert));
            $('.list-parameters').append(newRow);

            $('.add-parameter-input').val('');
            parametersSelected.push(parameterInsert.path);

            initAutocompleteParameter();
            initRemoveParameter();
            initParameterCheckboxInput();
        }
    }
    
    policy_configuration_form.find('input[name="radioLimited"]').change(function () {
        if($(this).val() === '0') {
            policy_configuration_form.find('input[name="limited"]').hide().val('').removeAttr('required');
            policy_configuration_validator.resetForm();
        } else {
            policy_configuration_form.find('input[name="limited"]').show().attr('required', 'required');
        }
    });

    function generateValidateSelectedTargetedDevices(value) {
        policy_configuration_validator.resetForm();

        if(value === 'byFilters') {
            $('.byFilters').each(function () {
                $(this).removeAttr('disabled').attr('required', 'required');
            });
            $('.byExternalFile').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            })

        } else if(value === 'byExternalFile') {
            $('.byExternalFile').each(function () {
                $(this).removeAttr('disabled').attr('required', 'required');
            });
            $('.byFilters').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            })

        } else {
            $('.byFilters').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            });
            $('.byExternalFile').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            });
        }

    }

    function generateValidateSchedule(value) {
        policy_configuration_validator.resetForm();

        if(value === 'startAt') {
            $('.startAt').each(function () {
                $(this).removeAttr('disabled').attr('required', 'required');
            });
            $('.unrequired').each(function () {
                $(this).removeAttr('required');
            });
            $('.immediately').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            })

        } else if(value === 'immediately') {
            $('.immediately').each(function () {
                $(this).removeAttr('disabled').attr('required', 'required');
            });

            $('.startAt').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            })

        } else {
            $('.startAt').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            });

            $('.immediately').each(function () {
                $(this).removeAttr('required').attr('disabled', 'disabled');
            });
        }
    }


    // Validate number
    $('.validateInteger').on('keyup change paste', function () {
        var alNumRegexPhone = /[^0-9]/g; //only numbers
        if (alNumRegexPhone.test($(this).val())) {
            $(this).parent().find('.text-error-integer').remove();
            $(this).parent().append('<span class="text-error-integer text-danger">Input invalid, please enter an integer.</span>');
            $('.btnSave').attr('disabled', true);
        } else {
            $(this).parent().find('.text-error-integer').remove();
            if (policy_configuration_form.find('.text-error-integer').length === 0) {
                $('.btnSave').removeAttr('disabled');
            }
        }

    })

    //</editor-fold>


});