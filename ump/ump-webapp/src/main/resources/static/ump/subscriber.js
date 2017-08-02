$(function () {
    var subscriber_page = $('#subscriber-page');

    // ADD SUBSCRIBER VARIABLE
    var add_subscriber = $('#add-subscriber-modal');
    var add_subscriber_form = $('#add-subscriber-form');
    var add_subscriber_form_validator = add_subscriber_form.validate(ump.validator);
    var subscriberId = 0; // 0: create
    var dataTemplateArr = [];
    var dataTemplateInputOptionArr = [];

    add_subscriber_form.find('select.select2').select2();

    // ADD SUBSCRIBER EVENT
    subscriber_page.find('#add-subscriber-opener').click(function () {
        add_subscriber.modal('show');
        subscriberId = 0;
        dataTemplateArr = [];
        dataTemplateInputOptionArr = [];
        add_subscriber_form.find('input').val('');
        add_subscriber_form.find('.list-devices').html('<tr class="list-devices-no-data"> <td colspan="10" align="center"> No data.</td> </tr>');
        add_subscriber_form.find('.list-data-template').html('<tr class="list-data-template-no-data"> <td colspan="10" align="center">No data.</td> </tr>');
        add_subscriber_form.attr('action', '/subscriber/0/save');

        // Reset form
        add_subscriber_form_validator.resetForm();

        $.ajax({
            type: 'GET',
            url: document.location.origin + '/data-template/get-list',
            data: {
                subscriberId: subscriberId
            },
            success: function (data) {
                dataTemplateArr = [];
                dataTemplateInputOptionArr = [];
                $.each(data, function (index, value) {
                    dataTemplateInputOptionArr.push({
                        id: value.id,
                        text: value.name
                    });
                    dataTemplateArr[value.id] = value;
                });
                generateDataTemplateInput();

            }
        });
    });


    // Add new template to table
    add_subscriber_form.find('#data-template-input-btn').click(function () {
        addNewTemplateToTable();
    });
    add_subscriber_form.find('#data-template-input').on('select2:select', function () {
        addNewTemplateToTable();
    });


    var data_template_first_row_html = '' +
        '<tr data-templateId="{id}">' +
        '   <td rowspan="{rowspan}"> {name}</td>' +
        '   <td> {paramName}</td>' +
        '   <td>' +
        '       <input name="parameter_{name}-{paramName}" type="text" class="form-control" placeholder="Write a value...">' +
        '       <input name="subscriberTemplateId_{id}" value="{id}" type="text" style="display: none;">' +
    '       </td>' +
        '   <td rowspan="{rowspan}" align="center"> <ul class="icons-list"> <li><a data-templateId="{id}" class="remove-template-btn"><i class="icon-trash"></i></a></li> </ul> </td>' +
        '</tr>';
    var data_template_next_row_html = '' +
        '<tr data-templateId="{id}">' +
        '   <td> {paramName}</td>' +
        '   <td><input name="parameter_{name}-{paramName}" type="text" class="form-control" placeholder="Write a value..."></td>' +
        '</tr>';

    function generateDataTemplateInput() {
        var dataTemplateInputOptionArrClone = [].concat(dataTemplateInputOptionArr);
        if(dataTemplateInputOptionArrClone.length > 0) {
            dataTemplateInputOptionArrClone[0] = {
                id: dataTemplateInputOptionArrClone[0].id,
                text: dataTemplateInputOptionArrClone[0].text,
                selected: true
            }
        }
        add_subscriber_form.find('#data-template-input').select2('destroy').empty().select2({
            closeOnSelect: true,
            placeholder: '- Select an option -',
            data: dataTemplateInputOptionArrClone
        });
    }

    function initEventRemoveTemplateRow() {
        add_subscriber_form.find('.remove-template-btn').unbind( "click" );
        add_subscriber_form.find('.remove-template-btn').click(function () {
            var templateIdRemove = $(this).attr('data-templateId');
            add_subscriber_form.find('.list-data-template').find('tr[data-templateId="'+templateIdRemove+'"]').remove();
            dataTemplateInputOptionArr.push({
                id: templateIdRemove,
                text: dataTemplateArr[templateIdRemove ].name
            });
            generateDataTemplateInput();
        });
    }

    function addNewTemplateToTable() {
        $('.list-data-template-no-data').remove();
        var templateIdSelected = add_subscriber_form.find('#data-template-input').val();
        if(templateIdSelected && templateIdSelected > 0) {

            $.each([].concat(dataTemplateInputOptionArr), function (index, value) {
                var dataTemplateObj = dataTemplateArr[value.id] ? dataTemplateArr[value.id] : {};
                if(dataTemplateObj.id == templateIdSelected) {

                    // Update select template
                    dataTemplateInputOptionArr.splice(index, true);
                    generateDataTemplateInput();

                    // Update table list template
                    $.each(dataTemplateObj.templateKeys, function (index2, value2) {
                        if(index2 == 0) {
                            var newRow = data_template_first_row_html;
                            newRow = newRow.split('{rowspan}').join(dataTemplateObj.templateKeys.length);
                            newRow = newRow.split('{name}').join(dataTemplateObj.name);
                            newRow = newRow.split('{id}').join(dataTemplateObj.id);
                            newRow = newRow.split('{paramName}').join(value2);
                            $('.list-data-template').append(newRow)
                        } else {
                            var newRow = data_template_next_row_html;
                            newRow = newRow.split('{name}').join(dataTemplateObj.name);
                            newRow = newRow.split('{id}').join(dataTemplateObj.id);
                            newRow = newRow.split('{paramName}').join(value2);
                            $('.list-data-template').append(newRow)
                        }
                    });

                    initEventRemoveTemplateRow();
                }
            })
        }
    }


    
    // Add new serial number to table
    var serial_number_row_html = '' +
        '<tr data-deviceStr="{oui}-{productClass}-{serialNumber}">' +
        '   <td style="display: none"> <input type="text" name="deviceStr_{oui}-{productClass}-{serialNumber}" value="{oui}-{productClass}-{serialNumber}"></td>' +
        '   <td> {manufacturer}</td>' +
        '   <td> {oui}</td>' +
        '   <td> {productClass}</td>' +
        '   <td> {modelName}</td>' +
        '   <td> {serialNumber}</td>' +
        '   <td align="center"> <ul class="icons-list"> <li><a data-deviceStr="{oui}-{productClass}-{serialNumber}" class="remove-device-btn"><i class="icon-trash"></i></a></li> </ul> </td>' +
        '</tr>';
    var listSerialNumber = [];
    add_subscriber_form.find('#device-input-btn').click(function () {
        var deviceInputStr = add_subscriber_form.find('#device-input').val().trim();
        var deviceInputStrArr = deviceInputStr.split('-');
        if(deviceInputStrArr.length === 3) {
            var ouiInput = deviceInputStrArr[0];
            var productClassInput = deviceInputStrArr[1];
            var serialNumberInput = deviceInputStrArr[2];

            $.ajax({
                type: 'GET',
                url: document.location.origin + '/subscriber/get-device',
                data: {
                    oui: ouiInput,
                    productClass: productClassInput,
                    serialNumber: serialNumberInput
                },
                success: function (data) {

                    if(data.isUse) {
                        new PNotify({ text: "Device already in use!", addclass: 'bg-danger'});
                    } else {
                        if (data.object && listSerialNumber.indexOf(deviceInputStr) < 0) {
                            // Add new row
                            var newRow = serial_number_row_html;
                            newRow = newRow.split('{deviceId}').join(data.object.id);
                            newRow = newRow.split('{manufacturer}').join(data.object.parameters["_deviceId._Manufacturer"]);
                            newRow = newRow.split('{oui}').join(data.object.parameters["_deviceId._OUI"]);
                            newRow = newRow.split('{productClass}').join(data.object.parameters["_deviceId._ProductClass"]);
                            newRow = newRow.split('{modelName}').join(data.object.parameters["summary.modelName"]);
                            newRow = newRow.split('{serialNumber}').join(data.object.parameters["_deviceId._SerialNumber"]);
                            add_subscriber_form.find('.list-devices').find('.list-devices-no-data').remove();
                            add_subscriber_form.find('.list-devices').append(newRow);

                            // Init remove row
                            add_subscriber_form.find('.remove-device-btn').unbind("click");
                            add_subscriber_form.find('.remove-device-btn').click(function () {
                                var dataRemove = $(this).attr('data-deviceStr');
                                add_subscriber_form.find('.list-devices').find('tr[data-deviceStr="' + dataRemove + '"]').remove();
                                listSerialNumber.splice(listSerialNumber.indexOf(dataRemove), true);
                            });

                            // Reset
                            add_subscriber_form.find('#device-input').val('');
                            listSerialNumber.push(deviceInputStr);
                        } else {

                            if (listSerialNumber.indexOf(deviceInputStr) < 0) {
                                // Add new row
                                var newRow = serial_number_row_html;
                                newRow = newRow.split('{deviceId}').join('');
                                newRow = newRow.split('{manufacturer}').join('');
                                newRow = newRow.split('{oui}').join(ouiInput);
                                newRow = newRow.split('{productClass}').join(productClassInput);
                                newRow = newRow.split('{modelName}').join('');
                                newRow = newRow.split('{serialNumber}').join(serialNumberInput);
                                add_subscriber_form.find('.list-devices').find('.list-devices-no-data').remove();
                                add_subscriber_form.find('.list-devices').append(newRow);

                                // Init remove row
                                add_subscriber_form.find('.remove-device-btn').unbind("click");
                                add_subscriber_form.find('.remove-device-btn').click(function () {
                                    var dataRemove = $(this).attr('data-deviceStr');
                                    add_subscriber_form.find('.list-devices').find('tr[data-deviceStr="' + dataRemove + '"]').remove();
                                    listSerialNumber.splice(listSerialNumber.indexOf(dataRemove), true);
                                });

                                // Reset
                                add_subscriber_form.find('#device-input').val('');
                                listSerialNumber.push(deviceInputStr);
                            }
                        }
                    }
                }
            });
        }
    });


    // EDIT SUBSCRIBER
    $('.subscriber-edit-opener').click(function () {
        add_subscriber.modal('show');
        subscriberId = $(this).attr('data-subscriberId');
        add_subscriber_form.attr('action', '/subscriber/'+subscriberId+'/save');
        add_subscriber_form.find('input.subscriber-id').attr('check-api', '/subscriber/'+subscriberId+'/existed?subscriberId=');

        // Reset form
        add_subscriber_form_validator.resetForm();
        $.ajax({
            type: 'GET',
            url: document.location.origin + '/subscriber/' + subscriberId + '/get-form',
            success: function (data) {

                dataTemplateArr = [];
                dataTemplateInputOptionArr = [];
                listSerialNumber = [];
                add_subscriber_form.find('.list-devices').html('<tr class="list-devices-no-data"> <td colspan="10" align="center"> No data.</td> </tr>');
                add_subscriber_form.find('.list-data-template').html('<tr class="list-data-template-no-data"> <td colspan="10" align="center">No data.</td> </tr>');

                $.each(data.subscriberTemplate, function (index, value) {
                    dataTemplateInputOptionArr.push({
                        id: value.id,
                        text: value.name
                    });
                    dataTemplateArr[value.id] = value;
                });
                generateDataTemplateInput();

                add_subscriber_form.find('input.subscriber-id').val(data.subscriber.subscriberId);
                $.each(data.subscriber.subscriberDataTemplateIds, function (index, value) {
                    add_subscriber_form.find('#data-template-input').val(value).change();
                    addNewTemplateToTable()
                });

                $.each(data.subscriber.subscriberData, function (index, value) {
                    add_subscriber_form.find('.list-data-template').find('input[name="parameter_'+index+'"]').val(value);
                });

                $.each(data.subscriberDevice, function (index, value) {
                    // Add new row
                    var newRow = serial_number_row_html;
                    newRow = newRow.split('{deviceId}').join(value.deviceId ? value.deviceId : '');
                    newRow = newRow.split('{manufacturer}').join(value.manufacturer ? value.manufacturer : '');
                    newRow = newRow.split('{oui}').join(value.oui);
                    newRow = newRow.split('{productClass}').join(value.productClass);
                    newRow = newRow.split('{modelName}').join(value.modelName ? value.modelName : '');
                    newRow = newRow.split('{serialNumber}').join(value.serialNumber);
                    add_subscriber_form.find('.list-devices').find('.list-devices-no-data').remove();
                    add_subscriber_form.find('.list-devices').append(newRow);

                    // Init remove row
                    add_subscriber_form.find('.remove-device-btn').unbind( "click" );
                    add_subscriber_form.find('.remove-device-btn').click(function () {
                        var dataRemove = $(this).attr('data-deviceStr');
                        add_subscriber_form.find('.list-devices').find('tr[data-deviceStr="' + dataRemove + '"]').remove();
                        listSerialNumber.splice(listSerialNumber.indexOf(dataRemove), true);
                    });

                    // Reset
                    add_subscriber_form.find('#device-input').val('');
                    listSerialNumber.push(value.oui + '-' + value.productClass + '-' + value.serialNumber);
                });
            }
        })



    });

    // SELECT BOX subscriber template event
    $('.subscriber-template-select2').select2().on('select2:unselecting', function (e) {
        if (!confirm('Do you want to remove: ' + e.params.args.data.text + '?')) {
            e.preventDefault();
        } else {
            var subscriberTemplateIdRemove = e.params.args.data.id;
            var id = $(this).attr('data-id');
            $.ajax({
                type: 'POST',
                url: document.location.origin + '/subscriber/'+id+'/remove-subscriber-template/' + subscriberTemplateIdRemove,
                success: function () {
                    $('.select2-dropdown').remove();
                    location.reload();
                }
            })
        }

    }).on('select2:select', function (e) {
        var subscriberId = $(this).attr('data-id');
        var subscriberTemplateId = e.params.data.id;
        $.ajax({
            type: 'POST',
            url: document.location.origin + '/subscriber/add-subscriber-template',
            data: {
                id: subscriberId,
                subscriberTemplateId: subscriberTemplateId
            },
            success: function () {
                location.reload();
            }
        })
    });

    // Find data selected frist load
    $('.subscriber-template-select2').each(function () {
        var subscriberDataTemplateIdsStr = $(this).attr('data-subscriberDataTemplateIds');
        var subscriberDataTemplateIdsArr = subscriberDataTemplateIdsStr.substring(1, subscriberDataTemplateIdsStr.length - 1).split(', ');
        $(this).val(subscriberDataTemplateIdsArr).change();
    });


    // SELECT BOX subscriber device event
    var selectValues = [];
    $(".subscriber-device-select2").select2({
        "language": {
            "noResults": function () {
                return "Add new";
            }
        },
        ajax: {
            url: document.location.origin + '/subscriber/search-device',
            dataType: 'json',
            delay: 250,
            data: function (params) {
                return {
                    q: params.term, // search term
                    page: params.page
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                var result = $.map(data, function (item) {
                    if($.map(selectValues, function (item) {return item.text}).indexOf(item.id) < 0) {
                        return {
                            text: item.id,
                            id: item.id
                        }
                    }
                });
                if(result.length === 0) {
                    var termArr = params.term ? params.term.split('-') : [];
                    if(termArr.length === 3 && termArr[2] !== '' && $.map(selectValues, function (item2) {return item2.text}).indexOf(params.term) < 0) {
                        result.push({text: params.term, id: params.term})
                    }
                }

                return {
                    results: result
                };
            },
            cache: true
        }

    }).on('select2:select', function (e) {
        // Save new subscriber device mapping device
        var subscriberId = $(this).attr('data-subscriberId');
        var deviceStr = e.params.data.text;
        var deviceStrArr = deviceStr.split("-");
        if(deviceStrArr.length === 3) {
            $.ajax({
                type: 'POST',
                url: document.location.origin + '/subscriber/add-subscriber-device',
                data: {
                    subscriberId: subscriberId,
                    oui: deviceStrArr[0],
                    productClass: deviceStrArr[1],
                    serialNumber: deviceStrArr[2]
                },
                success: function (response) {
                    if(!response) {
                        new PNotify({ text: "Device already in use!", addclass: 'bg-danger'});
                    }
                    setTimeout(function() {
                        location.reload();
                    }, 1000);

                }
            })
        }

    }).on('select2:open', function (e) {
        var subscriberId = $(this).attr('data-subscriberId');
            selectValues = $('.subscriber-device-select2[data-subscriberId="'+subscriberId+'"]').select2('data');

        $(document).unbind('keyup');
        $(document).keyup(function (e) {
            if ((e.keyCode == 13)) {
                var deviceStr = '';
                $('input.select2-search__field').each(function () {
                    if ($(this).val() !== '') {
                        deviceStr = $(this).val();
                    }
                });
                var deviceStrArr = deviceStr.split('-');
                if (deviceStrArr.length === 3 && deviceStrArr[0] !== '' && deviceStrArr[1] !== '' && deviceStrArr[2] !== ''
                    && $.map(selectValues, function (item) {return item.text}).indexOf(deviceStr) < 0) {
                    // Save new subscriber device not map device
                    $.ajax({
                        type: 'POST',
                        url: document.location.origin + '/subscriber/add-subscriber-device',
                        data: {
                            subscriberId: subscriberId,
                            oui: deviceStrArr[0],
                            productClass: deviceStrArr[1],
                            serialNumber: deviceStrArr[2]
                        },
                        success: function () {
                            if(!response) {
                                new PNotify({ text: "Device already in use!", addclass: 'bg-danger'});
                            }
                            setTimeout(function() {
                                location.reload();
                            }, 1000);
                        }
                    })
                }
            }
        });

    }).on('select2:unselecting', function (e) {
        if (!confirm('Do you want to remove: ' + e.params.args.data.text + '?')) {
            e.preventDefault();
        } else {
            var subscriberDeviceIdRemove = e.params.args.data.id;
            $.ajax({
                type: 'POST',
                url: document.location.origin + '/subscriber/remove-subscriber-device/' + subscriberDeviceIdRemove,
                success: function () {
                    $('.select2-dropdown').remove();
                    location.reload();
                }
            })
        }

    }).on('select2:close', function (e) {
        $(document).unbind('keyup');
    });

    // PAGINATION SUBSCRIBER
    var paginator_data = $('#data-subscriber-paginator');
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
        //goPageInput = (1 <= goPageInput && goPageInput <= parseInt(paginator_data.attr('data-totalPages'))) ? goPageInput : parseInt(paginator_data.attr('data-number'));
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

    // REMOVE DATA TEMPLATE
    var dataSubscriberIdArrChecked = [];
    var remove_subscriber_opener = $('#remove-subscriber-opener');
    var remove_subscriber_opener_html = remove_subscriber_opener.html();
    var confirm_delete_subscriber_modal = $('#confirm-delete-subscriber-modal');
    function showRemoveButton(isShow) {
        if(isShow) {
            remove_subscriber_opener.show();
            remove_subscriber_opener.html(remove_subscriber_opener_html + ' ('+dataSubscriberIdArrChecked.length+')')
        } else {
            remove_subscriber_opener.hide();
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
            dataSubscriberIdArrChecked = queryChecked.split(',');
            $('.checkbox-row').each(function() {
                var idChecked = $(this).attr('data-subscriberId');
                if(dataSubscriberIdArrChecked.indexOf(idChecked) >= 0) {
                    $(this).prop('checked', true);
                }
            });
            showRemoveButton(dataSubscriberIdArrChecked.length > 0);
            updateCheckAllStatus();
        }
    }

    initCheckbox();
    showRemoveButton(dataSubscriberIdArrChecked.length > 0);

    function checkAll() {
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-subscriberId');
            $('input[data-subscriberId="'+path+'"]').prop('checked', true);
            if(dataSubscriberIdArrChecked.indexOf(path) < 0) {
                dataSubscriberIdArrChecked.push(path);
            }
        });
        showRemoveButton(dataSubscriberIdArrChecked.length > 0);
        ump.updateUrlQueryString('checked', dataSubscriberIdArrChecked.join(','));
    }
    function unCheckAll() {
        dataSubscriberIdArrChecked = [];
        $('.checkbox-row').each(function() {
            var path = $(this).attr('data-subscriberId');
            $('input[data-subscriberId="'+path+'"]').prop('checked', false);
            dataSubscriberIdArrChecked.splice(dataSubscriberIdArrChecked.indexOf(path), 1);
        });
        showRemoveButton(dataSubscriberIdArrChecked.length > 0);
        ump.updateUrlQueryString('checked', dataSubscriberIdArrChecked.length > 0 ? dataSubscriberIdArrChecked.join(',') : null);
    }
    $('input#checkbox-all').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });
    $('input.checkbox-row').change(function() {
        var path = $(this).attr('data-subscriberId');
        if ($(this).is(":checked")) {
            dataSubscriberIdArrChecked.push(path);
        } else {
            dataSubscriberIdArrChecked.splice(dataSubscriberIdArrChecked.indexOf(path), 1);
        }
        showRemoveButton(dataSubscriberIdArrChecked.length > 0);
        updateCheckAllStatus();
        ump.updateUrlQueryString('checked', dataSubscriberIdArrChecked.length > 0 ? dataSubscriberIdArrChecked.join(',') : null);
    });

    remove_subscriber_opener.click(function () {
        confirm_delete_subscriber_modal.modal('show');
    });
    confirm_delete_subscriber_modal.find('.confirm-delete-btn').click(function () {
        var ids = {};
        $.each(dataSubscriberIdArrChecked, function (index, value) {
            ids['id_' + index] = value;
        });

        $.ajax({
            type : "POST",
            url : document.location.origin + '/subscriber/remove',
            data: { ids: ids},
            success : function(data) {
                location.replace(location.origin + location.pathname);
            }
        });
    });

    // Validate special character
    var alNumRegex = /^([a-zA-Z0-9]+)$/; //only letters and numbers
    $('.error-special-character').hide();
    $('#add-subscriber-form').find('input[name=subscriberId]').change(function () {
        if(alNumRegex.test($(this).val())) {
            $('.error-special-character').hide();
            $('#add-subscriber-form').attr('onsubmit', 'return true');
        } else {
            $('.error-special-character').show();
            $('#add-subscriber-form').attr('onsubmit', 'return false');
        }
    });


});
