$(function() {

    //<editor-fold desc="PAGINATION PROVISIONING">
    var paginator_data = $('#paginator-provisioning');
    if (ump.getUrlQueryValue('page')) {
        ump.updateUrlQueryString('page', paginator_data.attr('data-number'));
    }
    $('.twbs-prev-next').twbsPagination({
        totalPages: parseInt(paginator_data.attr('data-totalPages')),
        visiblePages: 4,
        startPage: parseInt(paginator_data.attr('data-number')),
        prev: '&#8672;',
        next: '&#8674;',
        first: '&#8676;',
        last: '&#8677;',
        onPageClick: function (event, page) {
            if(page !== parseInt(paginator_data.attr('data-number'))) {
                ump.updateUrlQueryString('limit', (document.location.search.search('limit') > 0) ? ump.getUrlQueryValue('limit') : null);
                ump.updateUrlQueryString('page', page, function () {
                    location.reload();
                });
            }
        }
    });
    $('.go-page-btn').click(function () {
        goPage();
    });
    $('.go-page-input').on('keydown', function(e) {
        if (e.which == 13) {
            goPage();
        }
    });
    function goPage() {
        var goPageInput = parseInt($('.go-page-input').val());
        goPageInput = (1 <= goPageInput && goPageInput <= parseInt(paginator_data.attr('data-totalPages'))) ? goPageInput : parseInt(paginator_data.attr('data-number'));
        ump.updateUrlQueryString('limit', (document.location.search.search('limit') > 0) ? ump.getUrlQueryValue('limit') : null);
        ump.updateUrlQueryString('page', goPageInput, function () {
            location.reload();
        });
    };
    $('.limit-page-input').change(function () {
        var limitPageInput = parseInt($('.limit-page-input').val());
        ump.updateUrlQueryString('limit', limitPageInput, function () {
            location.reload();
        });
    });
    if(ump.getUrlQueryValue('limit') && parseInt(ump.getUrlQueryValue('limit')) > 0) {
        $('.limit-page-input').val(ump.getUrlQueryValue('limit'));
    }
    //</editor-fold>

    //<editor-fold desc="CREATE PROVISIONING">

    var modal_create_provisioning = $('.modal-create-provisioning');
    var form_create_provisioning = $('.form-create-provisioning');
    var form_create_provisioning_validate = form_create_provisioning.validate(ump.validator);

    var deviceTypeVersionsTree = []; // Get data from server
    var deviceTypeVersionsSelected = [];
    var deviceTypeVersionsById = [];

    var parameters = []; // Get from server
    var parametersByPath = [];
    var parametersSelected = [];
    var parametersAutocomplete = [];

    var profiles = []; // Get from server
    var profilesById = [];

    var subscriberTemplates = []; // Get from server
    var subscriberTemplatesOptionSelect = []; // Get from server

    function resetFormProvisioning() {
        deviceTypeVersionsSelected = [];
        deviceTypeVersionsById = [];

        parametersSelected = [];
        parametersByPath = [];
        parametersAutocomplete = [];

        profiles = [];
        profilesById = [];

        $('.input-tag-name-error').hide();
        $('.list-device-type-versions').html('');
        $('.list-device-type-versions-no-data').show();
        $('.list-parameters').html('');
        $('.list-parameters-no-data').show();

        form_create_provisioning.find(".input-parameter").autocomplete();
        form_create_provisioning.find('.input-device-type-version').select2();
        form_create_provisioning.find('.input-profile').select2();
        form_create_provisioning.find('input').val('');
        form_create_provisioning_validate.resetForm();
    }
    
    function initAutocompleteParameter() {
        parametersAutocomplete = [];
        parametersByPath = [];
        $.each(deviceTypeVersionsSelected, function (index, value) {
            if(parameters[value.id]) {
                $.each(parameters[value.id], function (index2, value2) {
                    if(parametersSelected.indexOf(value2.path) < 0 && parametersAutocomplete.indexOf(value2.path) < 0) {
                        parametersAutocomplete.push(value2.path)
                    }
                    parametersByPath[value2.path] = value2;
                });
            }
        });
        form_create_provisioning.find(".input-parameter").autocomplete('destroy').autocomplete({
            source: parametersAutocomplete
        });
    }

    function initRemoveDeviceTypeVersion() {
        form_create_provisioning.find('.device-type-version-remove-btn').unbind( "click" )
            .click(function () {
            var idRemove = $(this).attr('data-deviceTypeVersionId');
            if(deviceTypeVersionsById[idRemove]) {
                form_create_provisioning.find('.list-device-type-versions').find('tr[data-deviceTypeVersionId="'+idRemove+'"]').remove();
                deviceTypeVersionsSelected.splice(deviceTypeVersionsSelected.indexOf(deviceTypeVersionsById[idRemove]), true);
                generateDeviceTypeVersionInput();
                generateProfileInput();
                initAutocompleteParameter();
            }
        });
    }

    function initRemoveParameter() {
        form_create_provisioning.find('.parameter-remove-btn').unbind( "click" )
            .click(function () {
                var pathRemove = $(this).attr('data-parameterPath');
                form_create_provisioning.find('.list-parameters').find('tr[data-parameterPath="'+pathRemove+'"]').remove();
                parametersSelected.splice(parametersSelected.indexOf(pathRemove), true);
                initAutocompleteParameter();
            });
    }

    function initParameterCheckboxInput() {
        form_create_provisioning.find('.parameter-checkbox-input').unbind( "click" )
            .click(function () {
                var pathCheckbox = $(this).attr('data-parameterPath');

                if($(this).is(":checked")) {
                    form_create_provisioning.find('.parameter-checkbox-value[data-parameterPath="'+pathCheckbox+'"]').val('true');
                } else {
                    form_create_provisioning.find('.parameter-checkbox-value[data-parameterPath="'+pathCheckbox+'"]').val('false');
                }

            });
    }

    function initSubscriberDataInput() {
        $('.checkbox-parameter').unbind( "change" ).change(function () {
            var parameterPath = $(this).attr('data-parameterPath');

            $('.list-parameters').find('tr[data-parameterPath="'+parameterPath+'"]')
                .find('.subscriber-data-input').select2({
                placeholder: '- Select subscriber data -'
            });

            if($(this).is(":checked")) {
                $('.list-parameters').find('tr[data-parameterPath="'+parameterPath+'"]')
                    .find('.subscriber-data-block').show()
                    .find('.subscriber-data-input').select2('destroy').empty().select2({
                        placeholder: '- Select subscriber data -',
                        data: subscriberTemplatesOptionSelect
                    });
            } else {
                $('.list-parameters').find('tr[data-parameterPath="'+parameterPath+'"]')
                    .find('.subscriber-data-block').hide()
                    .find('.subscriber-data-input').val('').change();
            }
        });
    }

    function generateProfileInput() {

        var inputOptions = [];
        inputOptions.push({id:'', text:''});
        $.each(profiles, function (index, value) {
            if(deviceTypeVersionsById[value.deviceTypeVersionId]) {
                if(deviceTypeVersionsSelected.indexOf(deviceTypeVersionsById[value.deviceTypeVersionId]) >= 0) {
                    inputOptions.push({id: value.id, text: value.name});
                }
            }
        });

        // Init select2
        form_create_provisioning.find('.input-profile').select2('destroy').empty().select2({
            placeholder: '- Select Profile -',
            data: inputOptions

        }).on('select2:select', function (e) {
            var profileSelected = profilesById[e.params.data.id];
            if(profileSelected) {
                $.each(profileSelected.parameters, function (index, value) {
                    if(typeof parametersByPath[value.path] !== 'undefined') {
                        addParameterToTable2(value);
                    }
                });
                initAutocompleteParameter();
                initSubscriberDataInput();
                initRemoveParameter();
                initParameterCheckboxInput();
            }
            $(this).val('').change();
        });

    }

    function generateDeviceTypeVersionInput() {

        // Get list option for select box
        var inputOptions = [];
        inputOptions.push({id: '', text: ''});
        $.each(deviceTypeVersionsTree, function (index, value) {
            var optGroup = { text: index, children: []};
            $.each(value, function (index2, value2) {
                if(deviceTypeVersionsSelected.indexOf(value2) < 0) {
                    optGroup.children.push({ id: value2.id, text: value2.firmwareVersion});
                }
            });

            if(optGroup.children.length > 0) {
                inputOptions.push(optGroup);
            }
        });

        // Init select2
        form_create_provisioning.find('.input-device-type-version').select2('destroy').empty().select2({
            placeholder: '- Select Device Type Version -',
            data: inputOptions

        }).on('select2:select', function (e) {
            if(deviceTypeVersionsById[e.params.data.id]) {
                addDeviceTypeVersionToTable(deviceTypeVersionsById[e.params.data.id])
            }
        });
    }
    
    function generateSubscriberDataInput() {
        var inputOptions = [];
        inputOptions.push({id: '', text: ''});
        $.each(subscriberTemplates, function (index, value) {
            var optGroup = { text: value.name, children: []};
            $.each(value.templateKeys, function (index2, value2) {
                optGroup.children.push({ id: value.name+'-'+value2, text: value2});
            });
            if(optGroup.children.length > 0) {
                inputOptions.push(optGroup);
            }

        });
        subscriberTemplatesOptionSelect = inputOptions;
    }

    function addDeviceTypeVersionToTable(deviceTypeVersion) {

        // Check exist in list
        if(deviceTypeVersionsSelected.indexOf(deviceTypeVersion) < 0) {

            $('.list-device-type-versions-no-data').hide();

            // Add html
            var newRow = $('.list-device-type-versions-template').html();
            newRow = newRow.split('{manufacturer}').join(deviceTypeVersion.manufacturer);
            newRow = newRow.split('{modelName}').join(deviceTypeVersion.modelName);
            newRow = newRow.split('{firmwareVersion}').join(deviceTypeVersion.firmwareVersion);
            newRow = newRow.split('{oui}').join(deviceTypeVersion.oui);
            newRow = newRow.split('{id}').join(deviceTypeVersion.id);
            newRow = newRow.split('disabled="disabled"').join('');
            $('.list-device-type-versions').append(newRow);

            deviceTypeVersionsSelected.push(deviceTypeVersion);

            // Get new list parameters by device type version
            var ids = {};
            $.each(deviceTypeVersionsSelected, function (index, value) {
                ids['id_' + index] = value.id;
            });
            $.ajax({
                type : "GET",
                url : document.location.origin + '/provisioning/get-list-parameter',
                data: {ids: ids},
                success : function(response) {
                    parameters = response;

                    initRemoveDeviceTypeVersion();
                    initAutocompleteParameter();
                    generateDeviceTypeVersionInput();
                    generateProfileInput();
                }
            });
        }
    }

    function addParameterToTable(paramter) {

        if(parametersSelected.indexOf(paramter.path) < 0) {
            if(parametersByPath.indexOf(paramter.path) >= 0) {
                add();
            } else {
                // Check create new parameter detail
                $.ajax({
                    type: "GET",
                    url: location.origin + '/parameter-detail/get-check-new-instance',
                    data: {path: paramter.path},
                    success: function (response) {
                        if(response === true) {
                            add();
                        } else {
                            // Cannot add this parameter
                            new PNotify({ text: 'Cannot add this parameter.', addclass: 'bg-danger'});
                        }
                    }
                })
            }
        } else {
            // Parameter is selected
            new PNotify({ text: 'Parameter is selected.', addclass: 'bg-danger'});
        }

        function add() {
            $('.list-parameters-no-data').hide();

            // Add html
            var newRow = $('.list-parameters-template').html();
            newRow = newRow.split('{path}').join(paramter.path);
            newRow = newRow.split('{value}').join(paramter.value && paramter.value !== '' ? paramter.value : paramter.defaultValue);
            newRow = newRow.split('{inputParameter}').join(ump.generateParameterInput(paramter));
            newRow = newRow.split('disabled="disabled"').join('');
            $('.list-parameters').append(newRow);

            $('.input-parameter').val('');
            parametersSelected.push(paramter.path);

            // Init input mask if date time
            if(paramter.dataType === 'dateTime') {
                form_create_provisioning.find('input[name="path_'+paramter.path+'"]').inputmask("datetime")
            }

            initAutocompleteParameter();
            initSubscriberDataInput();
            initRemoveParameter();
            initParameterCheckboxInput();
            form_create_provisioning_validate.resetForm();
        }
    }


    function addParameterToTable2(paramter) {

        // Check exist in list
        if(parametersSelected.indexOf(paramter.path) < 0 && paramter.path !== '') {

            $('.list-parameters-no-data').hide();

            // Add html
            var newRow = $('.list-parameters-template').html();
            newRow = newRow.split('{path}').join(paramter.path);
            newRow = newRow.split('{value}').join(paramter.value && paramter.value !== '' ? paramter.value : paramter.defaultValue);
            newRow = newRow.split('{inputParameter}').join(ump.generateParameterInput(paramter));
            newRow = newRow.split('disabled="disabled"').join('');
            $('.list-parameters').append(newRow);

            $('.input-parameter').val('');
            parametersSelected.push(paramter.path);

            // Init input mask if date time
            if(paramter.dataType === 'dateTime') {
                form_create_provisioning.find('input[name="path_'+paramter.path+'"]').inputmask("datetime")
            }

            // initAutocompleteParameter();
            // initSubscriberDataInput();
            // initRemoveParameter();
        }
    }


    // CREATE PROVISIONING OPENER
    $('#create-provisioning-opener').click(function () {
        modal_create_provisioning.modal('show');
        modal_create_provisioning.find('.modal-title').text('CREATE NEW');
        resetFormProvisioning();

        form_create_provisioning.attr('action', '/provisioning/save/0');

        // Get form provisioning
        $.ajax({
            type : "GET",
            url : document.location.origin + '/provisioning/get-form-create',
            data: {},
            success : function(response) {
                deviceTypeVersionsTree = response.deviceTypeVersionTree;
                parameters = response.parameters;
                profiles = response.profiles;
                subscriberTemplates = response.subscriberTemplates;

                // Get device type version by id
                $.each(response.deviceTypeVersionTree, function (index, value) {
                    $.each(value, function (index2, value2) {
                        deviceTypeVersionsById[value2.id] = value2;
                    });
                });

                $.each(response.profiles, function (index, value) {
                    profilesById[value.id] = value;
                });

                generateDeviceTypeVersionInput();

                generateSubscriberDataInput();

                generateProfileInput();
            }
        });
    });

    // EDIT PROVISIONING OPENER
    $('.edit-provisioning-opener').click(function () {
        modal_create_provisioning.modal('show');
        modal_create_provisioning.find('.modal-title').text('EDIT TAG');
        resetFormProvisioning();

        var tagEditId = $(this).attr('data-tagId');
        form_create_provisioning.attr('action', '/provisioning/save/'+tagEditId);

        // Get form provisioning
        $.ajax({
            type : "GET",
            url : document.location.origin + '/provisioning/get-form-update?id='+tagEditId,
            data: {},
            success : function(response) {

                deviceTypeVersionsTree = response.deviceTypeVersionTree;
                parameters = response.parameters;
                profiles = response.profiles;
                subscriberTemplates = response.subscriberTemplates;

                // Get device type version by id
                $.each(response.deviceTypeVersionTree, function (index, value) {
                    $.each(value, function (index2, value2) {
                        deviceTypeVersionsById[value2.id] = value2;
                    });
                });

                $.each(response.profiles, function (index, value) {
                    profilesById[value.id] = value;
                });

                generateDeviceTypeVersionInput();
                generateSubscriberDataInput();
                generateProfileInput();

                $('.input-tag-name').val(response.rootTag.name).attr('check-api', '/provisioning/'+tagEditId+'/existed?name=');
                $.each(response.deviceTypeVersionIdsSelected, function (index, value) {
                    addDeviceTypeVersionToTable(deviceTypeVersionsById[value]);
                });
                $.each(response.parametersSelected, function (index, value) {
                    addParameterToTable2(value);
                });

                initSubscriberDataInput();
                initRemoveParameter();
                initParameterCheckboxInput();
                initAutocompleteParameter();

                // Set value for subscriber data
                $.each(response.parametersSelected, function (index, value) {
                    if(value.useSubscriberData && value.useSubscriberData === 1) {
                        $('.checkbox-parameter[data-parameterPath="'+value.path+'"]').prop('checked', true).change();
                        $('.list-parameters').find('tr[data-parameterPath="'+value.path+'"]')
                            .find('.subscriber-data-input').val(value.subscriberData).change();
                    }
                });

            }
        });
    });

    // Add parameter to table
    $('.parameter-input-btn').click(function () {
        var parameterInput = $('.input-parameter').val();
        var parameterInsert = (typeof parametersByPath[parameterInput] !== 'undefined')
            ? parametersByPath[parameterInput] : {path: parameterInput, defaultValue: ''};
        addParameterToTable(parameterInsert)
    });

    // SUBMIT FORM
    form_create_provisioning.submit(function (e) {
        e.preventDefault();
        beforeSubmit(form_create_provisioning);
    });

    function beforeSubmit(form) {
        var isValid = true,
            messageError = '',
            currentName = form.find('input[name=name]').val();

        // Validate special character
        var alNumRegexUsername = /[^a-zA-Z0-9_-]/g; //only letters and numbers
        if (alNumRegexUsername.test(currentName)) {
            messageError += 'Name has special character. \n';
            isValid = false;
        }

        if (parametersSelected.length === 0) {
            messageError += 'Please select parameter to before submit form. \n';
            isValid = false;
        }

        // Submit form
        if(form.valid() && isValid) {
            form.unbind('submit').submit().find('[type=submit]').trigger('click');
        } else {
            messageError = messageError === '' ? 'Fill all required.' : messageError;
            new PNotify({ text: messageError, addclass: 'bg-danger'});
            return false;
        }
    }

    //</editor-fold>

});