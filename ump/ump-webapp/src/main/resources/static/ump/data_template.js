$(function () {

    // CREATE NEW DATA TEMPLATE
    var data_template_modal = $('#add-data-template-modal');
    var add_data_template_form = $('#add-data-template-form');
    var validator = add_data_template_form.validate(ump.validator);
        add_data_template_form.attr('onsubmit', 'return false');
    var subscriberTemplateId = 0;

    var parameter_name_input = $('#add-data-template-parameter-name-input');
    var parameterNameStrArr = [];

    // Open modal create
    $('#add-data-template-opener').click(function () {
        validateDataTemplateName();
        parameterNameStrArr = [];
        subscriberTemplateId = 0;
        data_template_modal.modal('show');
        add_data_template_form.find('input').val(null);
        $('#add-data-template-list-parameter').html('');
        validator.resetForm();
    });

    // Event add parameter name
    var parameter_name_row_html = '' +
        '<div class="row" data-parameter-name="{parameterName}">' +
        '   <div class="col-md-10"> <input type="text" class="form-control" name="parameter" value="{parameterName}" placeholder="Write a name..." unique="unique"></div>' +
        '   <div class="col-md-2"> <a class="btn btn-icon btn-default btn-rounded legitRipple add-data-template-remove-row-btn" data-parameter-name="{parameterName}"> <i class="icon-minus3"></i></a></div>' +
        '</div>';
    function addParameterToList(inputStr) {
        if(validateParameter(inputStr)) {
            parameterNameStrArr.push(inputStr);
            $('#add-data-template-list-parameter').append(parameter_name_row_html.split("{parameterName}").join("" + inputStr + ""));
            parameter_name_input.val(null);

            // Add function remove row
            $('.add-data-template-remove-row-btn').unbind( "click" ).click(function () {
                var parameterRemove = $(this).attr('data-parameter-name');
                if(parameterNameStrArr.indexOf(parameterRemove) >= 0) {
                    parameterNameStrArr.splice(parameterNameStrArr.indexOf(parameterRemove), true);
                    $('#add-data-template-list-parameter').find('div[data-parameter-name="'+parameterRemove+'"]').remove();
                }
            })
        } else {
            new PNotify({ text: "Parameter has special character.", addclass: 'bg-danger'});
        }
    }

    function validateParameter(parameterString) {
        var result = true;
        var alNumRegexUsername = /[^a-zA-Z0-9]/g; //only letters and numbers
        if (alNumRegexUsername.test(parameterString)) {
            result = false;
        }

        return result;
    }

    parameter_name_input.keyup(function (e) {
        var inputStr = parameter_name_input.val().trim();
        if(e.keyCode === 13 && inputStr && inputStr !== '' && parameterNameStrArr.indexOf(inputStr) < 0) {
            addParameterToList(inputStr);
        }
    });
    $('#add-data-template-parameter-name-btn').click(function () {
        var inputStr = parameter_name_input.val().trim();
        if(inputStr && inputStr !== '' && parameterNameStrArr.indexOf(inputStr) < 0) {
            addParameterToList(inputStr);
        }
    });

    // Save form add parameter
    $('#add-dat-template-btn-save').click(function () {
        if(add_data_template_form.valid() && parameterNameStrArr.length > 0) {
            var templateName = $('#template-name').val();
            var params = {};

            $('#add-data-template-list-parameter').find('input[name=parameter]').each(function () {
                var currentValue = $(this).val();
                params['param_' + currentValue] = currentValue;
            });

            $.ajax({
                type : "POST",
                url : document.location.origin + '/subscriber/data-template/'+subscriberTemplateId+'/save',
                data: {
                    name: templateName,
                    params: params
                },
                success : function() {
                    location.replace(location.origin + location.pathname);
                }
            });
        }
    })


    // EDIT DATA TEMPLATE
    $('.data-template-edit-opener').click(function () {
        validateDataTemplateName();
        var dataTemplateIdEdit = $(this).attr('data-subscriberTemplateId');
        $.ajax({
            type : "GET",
            url : document.location.origin + '/subscriber/data-template/'+dataTemplateIdEdit,
            data: {},
            success : function(data) {
                parameterNameStrArr = [];
                subscriberTemplateId = data.object.id;
                data_template_modal.modal('show');
                add_data_template_form.find('input').val(null);
                $('#add-data-template-list-parameter').html('');
                validator.resetForm();

                add_data_template_form.find('input#template-name').val(data.object.name);
                add_data_template_form.find('input#template-name').attr('check-api', '/subscriber/data-template/'+subscriberTemplateId+'/existed?name=');

                $.each(data.object.templateKeys, function (index, value) {
                    addParameterToList(value);
                });

                if(data.isUsed) {
                    add_data_template_form.find('.add-data-template-remove-row-btn').remove();
                }

            }
        });

    });


    // LIST SUBSCRIBER TEMPLATE
    $('.template-parameter-select2').select2({
        closeOnSelect: false,
    }).on('select2:open', function (evt) {
        $('.select2-dropdown').remove();
    });


    // DELETE PARAMETER NAME
    var confirm_delete_parameter_name_modal = $('#confirm-delete-parameter-name-modal');
    var parameterNameRemove = null;
    var subscriberTemplateId = null;
    $('.template-parameter-select2').on('select2:unselecting', function (evt) {

        evt.preventDefault();
        $('.select2-dropdown').remove();
        confirm_delete_parameter_name_modal.modal('show');
        confirm_delete_parameter_name_modal.find('.modal-title').text('Do you want to delete: '+evt.params.args.data.text+'?');
        parameterNameRemove = evt.params.args.data.id;
        subscriberTemplateId = $(this).attr('data-subscriberTemplateId');
    });

    confirm_delete_parameter_name_modal.find('.confirm-delete-btn').click(function () {
        $.ajax({
            type : "POST",
            url : document.location.origin + '/subscriber/data-template/'+subscriberTemplateId+'/post-remove-parameter',
            data: { parameterName: parameterNameRemove},
            success : function(response) {
                if(response.length > 0) {
                    new PNotify({ text: 'Template '+response.join(', ')+' already in use!', addclass: 'bg-danger'});
                } else {
                    location.reload();
                }
            }
        });
    });


    // REMOVE DATA TEMPLATE
    var dataTemplateIdArrChecked = [];
    var remove_data_template_opener = $('#remove-data-template-opener');
    var remove_data_template_opener_text = remove_data_template_opener.html();
    var confirm_delete_data_template_modal = $('#confirm-delete-data-template-modal');
    function showRemoveButton(isShow) {
        if(isShow) {
            remove_data_template_opener.show();
            remove_data_template_opener.html(remove_data_template_opener_text + ' ('+dataTemplateIdArrChecked.length+')')
        } else {
            remove_data_template_opener.hide();
        }
    }

    function updateCheckAllStatus() {
        var checked = null;
        var unChecked = null;
        $('.checkbox-row').each(function () {
            if ($(this).is(":checked")) {
                checked = true;
            } else {
                unChecked = true;
            }
        });

        if(checked != null && unChecked != null) {
            $('input#checkbox-all').prop('indeterminate', true);
        } else {
            $('input#checkbox-all').prop('indeterminate', false);
            if(checked) {
                $('input#checkbox-all').prop('checked', 'checked');
            } else if(unChecked) {
                $('input#checkbox-all').prop('checked', false);
            }
        }
    }

    function initCheckbox () {
        var queryChecked = ump.getUrlQueryValue('checked');
        if(queryChecked != null) {
            dataTemplateIdArrChecked = queryChecked.split(',');
            $('.checkbox-row').each(function() {
                var idChecked = $(this).attr('data-subscriberTemplateId');
                if(dataTemplateIdArrChecked.indexOf(idChecked) >= 0) {
                    $(this).prop('checked', true);
                }
            });
            showRemoveButton(dataTemplateIdArrChecked.length > 0);
            updateCheckAllStatus();
        }
    }

    initCheckbox();
    showRemoveButton(dataTemplateIdArrChecked.length > 0);

    function checkAll() {
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-subscriberTemplateId');
            $('input[data-subscriberTemplateId="'+path+'"]').prop('checked', true);
            if(dataTemplateIdArrChecked.indexOf(path) < 0) {
                dataTemplateIdArrChecked.push(path);
            }
        });
        showRemoveButton(dataTemplateIdArrChecked.length > 0);
        ump.updateUrlQueryString('checked', dataTemplateIdArrChecked.join(','));
    }
    function unCheckAll() {
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-subscriberTemplateId');
            $('input[data-subscriberTemplateId="'+path+'"]').prop('checked', false);
            dataTemplateIdArrChecked.splice(dataTemplateIdArrChecked.indexOf(path), 1);
        });
        showRemoveButton(dataTemplateIdArrChecked.length > 0);
        ump.updateUrlQueryString('checked', dataTemplateIdArrChecked.length > 0 ? dataTemplateIdArrChecked.join(',') : null);
    }
    $('input#checkbox-all').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });
    $('input.checkbox-row').change(function() {
        var path = $(this).attr('data-subscriberTemplateId');
        if ($(this).is(":checked")) {
            dataTemplateIdArrChecked.push(path);
        } else {
            dataTemplateIdArrChecked.splice(dataTemplateIdArrChecked.indexOf(path), 1);
        }
        showRemoveButton(dataTemplateIdArrChecked.length > 0);
        updateCheckAllStatus();
        ump.updateUrlQueryString('checked', dataTemplateIdArrChecked.length > 0 ? dataTemplateIdArrChecked.join(',') : null);
    });

    remove_data_template_opener.click(function () {
        confirm_delete_data_template_modal.modal('show');
        $.ajax({
            type : "POST",
            url : document.location.origin + '/subscriber/data-template/get-name-remove',
            data: { idsStr: dataTemplateIdArrChecked.join(',')},
            success : function(response) {
                confirm_delete_data_template_modal.find('.modal-title').text('Do you want to delete: '+response.join(', ')+'?')
            }
        });

    });
    confirm_delete_data_template_modal.find('.confirm-delete-btn').click(function () {
        var ids = {};
        $.each(dataTemplateIdArrChecked, function (index, value) {
            ids['id_' + index] = value;
        });

        $.ajax({
            type : "POST",
            url : document.location.origin + '/subscriber/data-template/remove',
            data: { ids: ids},
            success : function(response) {
                if(response.length > 0) {
                    new PNotify({ text: 'Template '+response.join(', ')+' already in use!', addclass: 'bg-danger'});
                } else {
                    location.replace(location.origin + location.pathname);
                }
            }
        });
    });


    // PAGINATION
    var paginator_data = $('#data-template-paginator-data');
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
        var goPageInput = parseInt($('.go-page-input').val());
        goPageInput = goPageInput <= parseInt(paginator_data.attr('data-totalPages')) ? goPageInput : parseInt(paginator_data.attr('data-number'));
        ump.updateUrlQueryString('limit', (document.location.search.search('limit') > 0) ? ump.getUrlQueryValue('limit') : null);
        ump.updateUrlQueryString('page', goPageInput, function () {
            location.reload();
        });
    });
    $('.limit-page-input').change(function () {
        var limitPageInput = parseInt($('.limit-page-input').val());
        ump.updateUrlQueryString('limit', limitPageInput, function () {
            location.reload();
        });
    });
    if(ump.getUrlQueryValue('limit') && parseInt(ump.getUrlQueryValue('limit')) > 0) {
        $('.limit-page-input').val(ump.getUrlQueryValue('limit'));
    }

    // Validate special character
    $('#add-data-template-form').find('#template-name').on('change keyup paste input', function () {
        validateDataTemplateName();
    });


    function validateDataTemplateName() {
        var inputValue = $('#add-data-template-form').find('#template-name').val();
        var alNumRegex = /[^a-zA-Z0-9]/g; //only letters and numbers
        $('.error-special-character').hide();
        if(!alNumRegex.test(inputValue)) {
            $('.error-special-character').hide();
            $('#add-data-template-form').find('#add-dat-template-btn-save').attr('disabled', false);
        } else {
            $('.error-special-character').show();
            $('#add-data-template-form').find('#add-dat-template-btn-save').attr('disabled', true);
        }
    }
});