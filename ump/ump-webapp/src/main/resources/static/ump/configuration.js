$(function() {

     var arrayObject = [];
     var dynamicInstance = 1000;
     $('#_tableConfiguration').DataTable({
        "paging": false,
        "width": '70%',
        "info": false,
        "filter": false,
        "scrollX": 'true'

    });
    $('input[type=checkbox]').on('change', function() {
        if ($(this).is(':checked')) {
            $(this).val("true");
        } else {
            $(this).val("false");
        }
    });

    /**
    this event for button created dynamic
    */
    $(document).on("click", ".delete-instance", function() {
        var instanceId = $(this).parent().parent().attr('id');
        $('#' + instanceId).remove();
        var arrayLength = arrayObject.length;
        for (var i = 0; i < arrayLength; i++) {
            if (arrayObject[i]['instanceId'] === instanceId) {
                arrayObject.splice(i, 1);
                break;
            }
        }
        var tagId = $('#form-input-data').find('input[name="tagId"]').val();
        var objectsInput = $('form[data-tag-id="' + tagId + '"]').find('input[name="objects"]');
        objectsInput.val(JSON.stringify(arrayObject));
    });
    $(document).on("click", ".edit-instance", function() {
        var dialog = $('#object-input-dialog');
        var instanceId = $(this).parent().parent().attr('id');
        var arrayLength = arrayObject.length;
        for (var i = 0; i < arrayLength; i++) {
            if (arrayObject[i]['instanceId'] === instanceId) {
                $(dialog).data('dataRow', arrayObject[i]);
                break;
            }
        }
        $(dialog).data('action', "edit");
        $(dialog).data('dataType', "newData");
        $(dialog).data('arrayObject', arrayObject);
        $(dialog).dialog('open');
        $('.ui-dialog').css({
            'max-height': '60%',
            'top': '20%',
            'left': '20%',
            'overflow-y': 'auto'
        });
    });



    var loadData = function() {
        var tableBody = $('#_tableConfiguration').find('tbody');
        var rows = tableBody.find('tr.object-data');
        for (var j = 0; j < rows.size(); j++) {
            var row = rows.get(j);
            var instaceId = $(row).attr('id');
            var cells = row['cells'];
            var instanceTmp = {};
            for (var i = 0; i < cells.length; i++) {
                var cell = cells[i];
                instanceTmp[$(cell).attr('name')] = {};
                instanceTmp[$(cell).attr('name')]['_value'] = $(cell).text();
                instanceTmp['action'] = 'none';
                instanceTmp['parentObject'] = $('#_parentObject').text();
                instanceTmp['instanceId'] = instaceId;

            }
            arrayObject.push(instanceTmp);
        }
    }
    loadData();
    $('#object-input-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '70%',
        height: 'auto',
        cache: false,
        open: function() {
            var action = $(this).data('action');
            var dataRwi = $(this).data('dataRow');
            clearWarning('object-input-dialog');
            var tagId = $('.object-input-dialog-opener').attr('data-tag-id');
            if (action === 'edit') {
                $(this).load('/tags/' + tagId + '/input-form', function() {
                    var inputs = $(this).find('input.form-control');
                    for (var i = 0; i < inputs.size(); i++) {
                        var input = inputs.get(i);
                        if (dataRwi[input.name] !== null) {
                            input.value = dataRwi[input.name]["_value"];
                            if (input.value === 'true')
                                input.checked = true;
                            else if (input.value === 'false') {
                                input.checked = false
                            }
                        }
                    }
                });

            } else if (action === 'add') {
                $(this).load('/tags/' + tagId + '/input-form');
            }
        },
        buttons: {
            Submit: function() {
                var arrayObject = $(this).data('arrayObject');
                clearWarning('object-input-dialog');
                var checkInput = false;
                var tagId = $(this).find('input[name="tagId"]').val();
                var objectsInput = $('form[data-tag-id="' + tagId + '"]').find('input[name="objects"]');
                var instance = {};
                var inputs = $(this).find('input');
                for (var i1 = 0; i1 < inputs.size(); i1++) {
                    var input = inputs.get(i1);
                    instance[input.name] = {};
                    if (input.type === 'checkbox') {
                        instance[input.name]['_value'] = input.checked;
                    } else {
                        instance[input.name]['_value'] = input.value;
                    }
                    if (valiadateInputForm(input) === true) {
                        checkInput = true;
                    }
                }
                if (checkInput === false) {
                    var selects = $(this).find('select');
                    for (var i2 = 0; i2 < selects.size(); i2++) {
                        var select = selects.get(i2);
                        instance[select.name]['_value'] = select.value;
                    }
                    var tableBody = $('#_tableConfiguration').find('tbody');
                    var rowBody = tableBody.find('tr.object-row-template').clone();
                    var cellBodys = rowBody.find('td.row-data');
                    for (var i3 = 0; i3 < cellBodys.size(); i3++) {
                        var cell = cellBodys.get(i3);
                        $(cell).text(instance[$(cell).attr('name')]['_value']);
                    }
                    var action = $(this).data('action');
                    var dataRow = $(this).data('dataRow');
                    if (action === 'edit') {
                        var dataType = $(this).data('dataType');
                        if (dataType !== null && dataType === 'newData') {
                            instance['action'] = 'add';
                            instance['parentObject'] = $('#_parentObject').text();
                            instance['instanceId'] = dataRow['instanceId'];
                            var arrayLength1 = arrayObject.length;
                            for (var i4 = 0; i4 < arrayLength1; i4++) {
                                if (arrayObject[i4]['instanceId'] === instance['instanceId']) {
                                    arrayObject[i4] = instance;
                                    break;
                                }
                            }
                            var row = tableBody.find('tr#' + dataRow['instanceId']);
                            var cells1 = row.find('td.row-data');
                            for (var i5 = 0; i5 < cells1.size(); i5++) {
                                var cell = cells1.get(i5);
                                $(cell).text(instance[$(cell).attr('name')]['_value']);
                            }
                            $(this).data('dataType', null);
                        } else {
                            instance['action'] = 'edit';
                            instance['instanceId'] = dataRow['instanceId'];
                            var ins = instance['instanceId'];
                            var insArray = ins.split("_");
                            instance['parentObject'] = $('#_parentObject').text() + insArray[1] + ".";
                            var arrayLength = arrayObject.length;
                            for (var i = 0; i < arrayLength; i++) {
                                if (arrayObject[i]['instanceId'] === instance['instanceId']) {
                                    arrayObject[i] = instance;
                                    break;
                                }
                            }
                            var row = tableBody.find('tr#' + dataRow['instanceId']);
                            var cells2 = row.find('td.object-data');
                            for (var i6 = 0; i6 < cells2.length; i6++) {
                                var cellTmp = cells2[i6];
                                $(cellTmp).text(instance[$(cellTmp).attr('name')]['_value']);
                            }

                        }
                    } else if (action === 'add') {
                        row.removeClass("object-row-template");
                        dynamicInstance++;
                        row.attr('id', 'row_' + dynamicInstance);
                        row.appendTo(tableBody);
                        instance['action'] = action;
                        instance['parentObject'] = $('#_parentObject').text();
                        instance['instanceId'] = row.attr('id');
                        arrayObject.push(instance);
                    }
                    objectsInput.val(JSON.stringify(arrayObject));
                    $(this).dialog('close');
                } else {
                    return false;
                }

            },
            Cancel: function() {
                $(this).dialog('close');
            }
        }
    });

    $('.object-input-dialog-opener').click(function() {
        var dialog = $('#object-input-dialog');
        $(dialog).data('action', "add");
        $(dialog).dialog('open');
        $(dialog).data('arrayObject', arrayObject);
        $('.ui-dialog').css({
            'max-height': '60%',
            'top': '20%',
            'left': '20%',
            'overflow-y': 'auto'
        });
    });

    $('.edit-instance-default').click(function() {
        var dialog = $('#object-input-dialog');
        var instanceId = $(this).parent().parent().attr('id');
        var arrayLength = arrayObject.length;
        for (var i = 0; i < arrayLength; i++) {
            if (arrayObject[i]['instanceId'] === instanceId) {
                $(dialog).data('dataRow', arrayObject[i]);
                break;
            }
        }
        $(dialog).data('action', "edit");
        $(dialog).data('arrayObject', arrayObject);
        $(dialog).dialog('open');
        $('.ui-dialog').css({
            'max-height': '60%',
            'top': '20%',
            'left': '20%',
            'overflow-y': 'auto'
        });
    });

    $('.delete-instance-default').click(function() {

        var instanceId = $(this).parent().parent().attr('id');
        $('#' + instanceId).remove();
        var arrayLength = arrayObject.length;
        for (var i = 0; i < arrayLength; i++) {
            if (arrayObject[i]['instanceId'] === instanceId) {
                arrayObject[i]['action'] = 'delete';
                var ins = arrayObject[i]['instanceId'];
                var insArray = ins.split("_");
                arrayObject[i]['parentObject'] = arrayObject[i]['parentObject'] + insArray[1] + ".";
                break;
            }
        }
        var tagId = $('#form-input-data').find('input[name="tagId"]').val();
        var objectsInput = $('form[data-tag-id="' + tagId + '"]').find('input[name="objects"]');
        objectsInput.val(JSON.stringify(arrayObject));
    });


    $('form#form-input-data').submit(function() {
        buttonLoading("submitFormdata", "start");
        $.ajax({
            url: '/devices/' + $('#deviceId').text() + '/config',
            type: 'post',
            contentType: "application/json",
            data: $(this).serialize(),
            success: function(data) {
                $("#configuration_tab").html(data);
                loadData();
            },
            error: function() {
                buttonLoading("submitFormdata", "end");
                loadData();
            },
            async: true
        });
        return false;
    });
    $('form#form-input-data-param').submit(function() {
        buttonLoading("submitFormdataParam", "start");
        var dataTosend = "";
        clearWarning('form-input-data-param');
        var validate = false;
        var inputs = $('#form-input-data-param').find('input');
        for (var i = 0; i < inputs.size(); i++) {
            var input = inputs.get(i);
            if (input.name != null && input.value != null && input.value != '') {
                var name = input.name;
                var value = input.value;
                var str = name + "=" + value + "&";
                dataTosend = dataTosend + str;
            }
            if (valiadateInputForm(input) === true) {
                validate = true;
            }
        }
        var selectNow = 'now=' + $('select[name=now]').val();
        dataTosend = dataTosend + selectNow;
        if (validate === false) {
            $.ajax({
                url: '/devices/' + $('#deviceId').text() + '/config',
                type: 'post',
                contentType: "application/json",
                data: dataTosend,
                success: function(data) {
                    $("#configuration_tab").html(data);
                    loadData();
                },
                error: function() {
                    buttonLoading("submitFormdataParam", "end");

                },
                async: true
            });
        } else {
            buttonLoading("submitFormdataParam", "end");
        }
        return false;


    });

});