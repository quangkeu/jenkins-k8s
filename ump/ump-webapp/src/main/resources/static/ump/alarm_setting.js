function updateDataTableSelectAllCtrl(table) {
    var $table = table.table().node();
    var $chkbox_all = $('tbody input[type="checkbox"]', $table);
    var $chkbox_checked = $('tbody input[type="checkbox"]:checked', $table);
    var chkbox_select_all = $('thead input[name="checkAll"]', $table).get(0);

    // If none of the checkboxes are checked
    if ($chkbox_checked.length == 0) {
        chkbox_select_all.checked = false;
        if ('indeterminate' in chkbox_select_all) {
            $("#checkAll").prop("indeterminate", false);
        }

        // If all of the checkboxes are checked
    } else if ($chkbox_checked.length == $chkbox_all.length) {
        chkbox_select_all.checked = true;
        if ('indeterminate' in chkbox_select_all) {
        $("#checkAll").prop("indeterminate", false);
        }

        // If some of the checkboxes are checked
    } else {
        chkbox_select_all.checked = true;
        if ('indeterminate' in chkbox_select_all) {
            $("#checkAll").prop("indeterminate", true);
        }
    }
}

$(function(){
    // DECLARE VARIABLE
    var parameters = [];
    var parametersByPath = [];
    var parametersSelected = [];
    var parametersSelectedValue = [];
    var parametersAutocomplete = [];

    var editParameters = [];
    var editParametersByPath = [];
    var editParametersSelected = [];
    var editParametersSelectedValue = [];
    var editParametersAutocomplete = [];

    var form_create_alarm = $('.form-validate-jquery');
    var form_edit_alarm = $('#form_edit_alarm');

    var totalAreaBeChecked = 0;
    var totalAreaInPage = $.trim($('#totalAreaInPage').text());
    var rowDelete = [];
    var rowDeleteName = [];

    var paginator_data = $('#data-data-model-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input').val());

    var table = $('#DataTables_Table_0').DataTable({
        'columnDefs': [{
            'targets': 0,
            'searchable': false,
            'orderable': false,
            'width': '1%',
            'className': 'dt-body-center'
        },{
                                                     "targets": [1],
                                                     "visible": false,
                                                 }],
        'order': [
            [1, 'asc']
        ],
        "sort": false,
        "paging": false,
        "info": false,
        "filter": false,
        "scrollY": "960px",
        "scrollCollapse": true
    });

    firstLoadPage();

    function firstLoadPage() {
        initLibrary();
        getListParameters();
    }

    $('.add-parameter-btn').click(function () {
        var pathStr = $('.add-parameter-input').val();
        if(parametersSelected.indexOf(pathStr) < 0) {
            var parameterInsert = (typeof parametersByPath[pathStr] !== 'undefined') ? parametersByPath[pathStr] : {path: pathStr, defaultValue: ''};
            addParameter(parameterInsert);
        }
    });

    $('.edit-parameter-btn').click(function () {
        var pathStr = $('.edit-parameter-input').val();
        if(editParametersSelected.indexOf(pathStr) < 0) {
            var editParameterInsert = (typeof editParametersByPath[pathStr] !== 'undefined') ? editParametersByPath[pathStr] : {path: pathStr, defaultValue: ''};
            addEditParameter(editParameterInsert);
        }
    });

    function addEditParameter(editParameterInsert) {
        var edit_value_range = $('#editValueRange').val();
                if(editParameterInsert !== null && editParameterInsert.path !== '' && edit_value_range !== null && edit_value_range !== '') {

                    $('.edit-list-parameters-no-data').hide();
                    // Add html
                    var newRow = $('.edit-list-parameters-template').html();
                    newRow = newRow.split('{path}').join(editParameterInsert.path);
                    newRow = newRow.split('{inputParameter}').join(edit_value_range);
                    $('.edit-list-parameters').append(newRow);

                    $('.edit-parameter-input').val('');
                    $('#editValueRange').val('');
                    editParametersSelected.push(editParameterInsert.path);
                    edit_value_range = edit_value_range.replace(/\,/g, ';');
                    editParametersSelectedValue.push(editParameterInsert.path+'|'+edit_value_range);

                    initAutocompleteParameter();
                    initRemoveParameter();
        //            initParameterCheckboxInput();
                }
    }

    function addParameter(parameterInsert) {
        var value_range = $('#valueRange').val();
        var isExit = false;

        if(parameterInsert !== null && parameterInsert.path !== '') {
                                  $.each(parametersAutocomplete, function (index, value) {

                                        if(value == parameterInsert.path) {
                                         isExit = true;
//                                         break;
                                        }
                                  });
        }

        if(isExit == true && value_range !== null && value_range !== '') {

            $('.list-parameters-no-data').hide();
            // Add html
            var newRow = $('.list-parameters-template').html();
            newRow = newRow.split('{path}').join(parameterInsert.path);
            newRow = newRow.split('{inputParameter}').join(value_range);
            $('.list-parameters').append(newRow);

            $('.add-parameter-input').val('');
            $('#valueRange').val('');
            parametersSelected.push(parameterInsert.path);
            value_range = value_range.replace(/\,/g, ';');
            parametersSelectedValue.push(parameterInsert.path+'|'+value_range);

            initAutocompleteParameter();
            initRemoveParameter();
//            initParameterCheckboxInput();
        }
    }

    function getListParameters() {
        $.ajax({
            type: 'GET',
            url: location.origin + '/policy-configuration' + '/search-parameters',
            success: function (response) {
                parameters = response;
                editParameters = response;
                parametersByPath = [];
                parametersAutocomplete = [];
                editParametersByPath = [];
                editParametersAutocomplete = [];
                $.each(response, function (index, value) {
                    parametersByPath[value.path] = value;
                    parametersAutocomplete.push(value.path);
                    editParametersByPath[value.path] = value;
                    editParametersAutocomplete.push(value.path);
                });

                initAutocompleteParameter();
            }
        });
    }

    function initAutocompleteParameter() {
        parametersAutocomplete = [];
        editParametersAutocomplete = [];
        $.each(parameters, function (index, value) {
            if(parametersSelected.indexOf(value.path) < 0) {
                parametersAutocomplete.push(value.path)
            }
        });
        $.each(editParameters, function (index, value) {
            if(editParametersSelected.indexOf(value.path) < 0) {
                editParametersAutocomplete.push(value.path)
            }
        });
        form_create_alarm.find(".add-parameter-input").autocomplete('destroy').autocomplete({
            source: parametersAutocomplete
        });
        form_edit_alarm.find(".edit-parameter-input").autocomplete('destroy').autocomplete({
            source: editParametersAutocomplete
        });
        $('#tableParameter').val(parametersSelectedValue);
        $('#editTableParameter').val(editParametersSelectedValue);
    }

    function initRemoveParameter() {
        form_create_alarm.find('.remove-parameter-btn').unbind( "click" )
            .click(function () {
                var pathRemove = $(this).attr('data-parameterPath');
                form_create_alarm.find('.list-parameters').find('tr[data-parameterPath="'+pathRemove+'"]').remove();
                parametersSelected.splice(parametersSelected.indexOf(pathRemove), true);
                parametersSelectedValue.splice(parametersSelectedValue.indexOf(pathRemove), true);
                initAutocompleteParameter();
            });

            form_edit_alarm.find('.remove-edit-parameter-btn').unbind( "click" )
                .click(function () {
                    var pathRemove = $(this).attr('data-parameterPath');
                    form_edit_alarm.find('.edit-list-parameters').find('tr[data-parameterPath="'+pathRemove+'"]').remove();
                    editParametersSelected.splice(editParametersSelected.indexOf(pathRemove), true);
                    editParametersSelectedValue.splice(editParametersSelectedValue.indexOf(pathRemove), true);
                    initAutocompleteParameter();
            });
    }

    function initLibrary() {
        $('.add-parameter-input').autocomplete({
            source: []
        });

        $('.edit-parameter-input').autocomplete({
            source: []
        });

        $('#aggregatedVolume').val('10');
    }

    $('#edit-alarm-setting-time-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '31%',
        height: 'auto'
    });

    $('#create-alarm-setting-time-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '31%',
        height: 'auto'
    });

    $('#create-alarm-setting-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '75%',
        height: 'auto'
    });

    $('#edit-alarm-setting-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '75%',
        height: 'auto'
    });

    $('#confirm-remove-dialog').dialog({
        autoOpen: false,
        modal: true,
        width:'50%',
        height: 'auto'
    });

    $('#confirm-remove-dialog-row').dialog({
        autoOpen: false,
        modal: true,
        width:'50%',
        height: 'auto'
    });

     $("#edit_notification").hide();

    $('.alarm-edit-btn').click(function() {
        $(".edit-list-parameters").empty();
        var detail = $(this).attr('type');
        var arr = detail.split("|");
        var parameterSplit = '';
        if(arr[5] !== '' && arr[5] !== 'null') {
            $('.edit-list-parameters-no-data').hide();
            parameterSplit = arr[5];
            parameterSplit = parameterSplit.substring(1,parameterSplit.length - 1);
            var listParameter = parameterSplit.split(",");
            var i;
            for (i = 0; i < listParameter.length; ++i) {
                    var newRow = $('.edit-list-parameters-template').html();
                    var path;
                    var value;
                    if(listParameter[i].indexOf("=>=") >= 0 || listParameter[i].indexOf("=<=") >= 0) {
                        var Parameter = listParameter[i].replace(/\=>=/g, ':>=');
                        Parameter = Parameter.replace(/\=<=/g, ':<=');
                        path = Parameter.split(":")[0];
                        value = Parameter.split(":")[1];
                    }
                    else {
                        path = listParameter[i].split("=")[0];
                        value = listParameter[i].split("=")[1];
                    }

                    newRow = newRow.split('{path}').join(path);
                    newRow = newRow.split('{inputParameter}').join(value.replace(/\;/g, ','));
                    $('.edit-list-parameters').append(newRow);

                    editParametersSelected.push(path);
                    editParametersSelectedValue.push(path+'|'+value);

                    initAutocompleteParameter();
                    initRemoveParameter();
            }

        }
        var groupFilter = arr[4];
        var editAlarmName = arr[2];
        $('#alarmId').val(arr[0]);
        $("#listEditGroupFilter").val(groupFilter);
        $('#editAlarmType').val(arr[1]).trigger("change");
        $('#editAlarmSeverity').val(arr[3]).trigger("change");
        $.each(groupFilter.split(","), function(i,e){
            $("#editGroupFilter option[value='" + e.trim() + "']").prop("selected", true).trigger("change");
        });
        $('#editNotifyAggregated').val(arr[8]).trigger("change");
        if(arr[6] == 'true') $("#editAlarmNotify").prop( "checked", true );
        if(arr[9] == 'true') $("#editAlarmMonitor").prop( "checked", true );
        $('#editAggregatedVolume').val(arr[7]);
        showHideNotifyEdit();

        editAlarmType = $("#editAlarmType").val();
        if(editAlarmType == 'PARAMETER_VALUE'){
            $("#edit_select_parameter").show();
            $("#edit_select_parameter_table").show();
            $('#editAlarmName').val(editAlarmName);
            $('#editAlarmName').prop('readonly', false);
        } else {
            if(editAlarmType == 'REQUEST_FAIL') editAlarmType = "Request failed";
            if(editAlarmType == 'CONFIGURATION_FAIL') editAlarmType = "Configuration device failed";
            if(editAlarmType == 'UPDATE_FIRMWARE_FAIL') editAlarmType = "Update  firmware failed";
            if(editAlarmType == 'REBOOT_FAIL') editAlarmType = "Reboot failed";
            if(editAlarmType == 'FACTORY_RESET_FAIL') editAlarmType = "Factory reset failed";

            $('#editAlarmName').val(editAlarmType);
            $('#editAlarmName').prop('readonly', true);
            $("#edit_select_parameter").hide();
            $("#edit_select_parameter_table").hide();
            $(".edit-list-parameters").empty();
            $("#editTableParameter").val('');
            parametersSelectedValue = [];
        }
        $("#edit_notification").show();
        if(arr[10] == '1') {
            $("#edit-real-time").prop( "checked", true );
            $("#editTime").val(arr[11]);
            modalAlarmEditSettingTime.find('.editTimeSetting').val(arr[11]);
        } else if(arr[10] == '2') {
            $("#edit-changes").prop( "checked", true );
        } else if(arr[10] == '3') {
            $("#edit-passive").prop( "checked", true );
        }
        else $("#edit_notification").hide();

        $('#edit-alarm-setting-dialog').dialog('open');

    });

    $('#new-alarm-dialog-opener').click(function() {
        $("#editTime").val('');
        $('#aggregatedVolume').val('10');
        $('#create-alarm-setting-dialog').dialog('open');
    });

    $('#create-alarm-dialog-close').click(function() {
        $('#create-alarm-setting-dialog').dialog('close');
        $('#form_create_alarm')[0].reset();
        $('#alarmTypeCreate').val('').trigger("change");
        $('#alarmSeverity').val('').trigger("change");
        $('#groupFilter').val('').trigger("change");
        $('#notifyAggregated').val('').trigger("change");
    });

    $('#edit-alarm-dialog-close').click(function() {
        $('#edit-alarm-setting-dialog').dialog('close');
        $('#form_edit_alarm')[0].reset();
    });

    $('.close').click(function() {
        $(this).parent().hide();
    });

        $( "#alarmNameCreate" ).focusout(function() {
                var successAlert = "";
                var alarmName = $(this).val();
                var alarmType = $("#alarmTypeCreate").val();
                if(alarmType == 'PARAMETER_VALUE') {
                if(alarmName == 'Request failed' || alarmName == 'Configuration device failed' || alarmName == 'Update  firmware failed' ||
                alarmName == 'Reboot failed' || alarmName == 'Factory reset failed') {
                        var alert_success = $('#alert_valided_input').attr('class');
                        successAlert += alert_success;
                        $('#alert-valid span.text-semibold').text(successAlert);
                        $('#alert-valid').show();
                        $("#alarmNameCreate").val('');
                }

                if(successAlert == '' && alarmName != '') {
                $.ajax({
                type: 'POST',
                url: "/alarm-setting/"+alarmName+"/check",
                success: function(AcsResponse) {
                    var successAlert = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result == 200) // success
                        {
                            successAlert = successAlert + alarmName + ", ";
                        }
                    });
                    if (successAlert !== "") {
                        var alert_success = $('#alert_exited_input').attr('class');
                        successAlert += alert_success;
                        $('#alert-exited span.text-semibold').text(successAlert);
                        $('#alert-exited').show();
                        $("#alarmNameCreate").val('');
                    }
                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });
                }
                }
         });

//    $('.add-group-btn').click(function() {
//        var deviceGroup = $("#addGroup").val();
//        var listGroup = $("#listGroup").val();
//        if(deviceGroup !== '') {
//            if (listGroup.indexOf(deviceGroup) < 0){
//                if(listGroup == '') $("#listGroup").val(deviceGroup);
//                else { listGroup = listGroup + ',' + deviceGroup;
//                $("#listGroup").val(listGroup); }
//            }
//
//        }
//    });

    // ADVANCE SEARCH
    $('.select2').select2({
        allowClear: true
    });

    // for other page back to current page
    $('.myCheckBox').each(function() {
            if ($(this).is(':checked')) {
                    totalAreaBeChecked += 1;
            }
     });

    $('#DataTables_Table_0 tbody').on('click', 'input[type="checkbox"]', function(e) {
            var $row = $(this).closest('tr');
            if (this.checked) {
                $row.addClass('selected');
            } else {
                $row.removeClass('selected');
            }
            // Update state of "Select all" control
            updateDataTableSelectAllCtrl(table);

            // Prevent click event from propagating to parent
            e.stopPropagation();

        });

    table.on('draw', function() {
        // Update state of "Select all" control
        updateDataTableSelectAllCtrl(table);
    });

    $('#checkAll').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }

    });

    function checkAll() {
            totalAreaBeChecked = 0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
            totalAreaBeChecked++;
        });
        showRemoveButton(totalAreaBeChecked);
    }


    /**
        function to unCheckAll device in page
    */
    function unCheckAll() {
            totalAreaBeChecked = 0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
        });
        showRemoveButton(totalAreaBeChecked);
    }

    $('.myCheckBox').change(function() {
        if ($(this).is(":checked")) {
            totalAreaBeChecked = totalAreaBeChecked + 1;
        } else {
            totalAreaBeChecked = totalAreaBeChecked - 1;
        }
        if (totalAreaInPage == totalAreaBeChecked) {
            $('#checkAll').prop('checked', true);
        } else {
            $('#checkAll').prop('checked', false);
        }
        // if non device be checkek -> disable button reboot and reset factory
        showRemoveButton(totalAreaBeChecked);
    });

        var dataTemplateIdArrChecked = [];
        var remove_data_template_opener = $('#confirm-remove-dialog-opener');
        var remove_data_template_opener_text = remove_data_template_opener.html();
        function showRemoveButton(total) {
            if(total > 0) {
                remove_data_template_opener.show();
                remove_data_template_opener.html(remove_data_template_opener_text + ' ('+total+')')
            } else {
            $('#confirm-remove-dialog-opener').html(remove_data_template_opener_text);
            }
        }

    $('#confirm-remove-dialog-close').click(function() {
        $('#confirm-remove-dialog').dialog('close');
    });

        $('#confirm-remove-dialog-row-close').click(function() {
            $('#confirm-remove-dialog-row').dialog('close');
        });


    $('#confirm-remove-dialog-opener').click(function() {
        if(totalAreaBeChecked > 0) {
        var listAreaBeRemove = "";
        if (totalAreaBeChecked < 5) {
            $('.myCheckBox').each(function(index, obj) {
                var id = $(this).attr('name');
                if ($(this).is(':checked')) {
                    listAreaBeRemove = listAreaBeRemove + id + ', ';
                }
            });
        listAreaBeRemove = listAreaBeRemove.substring(0, listAreaBeRemove.length - 2);
        listAreaBeRemove = listAreaBeRemove.replace(/\-/g, '');
        } else {
            listAreaBeRemove = totalAreaBeChecked + " alarms";
        }

        var title_dialog = $("#remove-dialog-input").attr('class');
        $('#confirm-remove-dialog').dialog('option', 'title', title_dialog + ' ' + listAreaBeRemove + '?');
        $('#confirm-remove-dialog').dialog('open');
        }
    });

    $('.alarm-remove-btn').click(function() {
        rowDelete = [];
        rowDeleteName = [];
        var id = $(this).attr('id');
        var name = $(this).attr('name');
        rowDelete.push(id);
        rowDeleteName[''+id] = name;
        var title_dialog = $("#remove-dialog-input-row").attr('class');
        $('#confirm-remove-dialog-row').dialog('option', 'title', title_dialog + ' ' + name + '?');
        $('#confirm-remove-dialog-row').dialog('open');
    });

        $('#searchBtn').click(function() {
            var alarmType = $('#alarmType').val();
            var alarmName = $('#alarmName').val();
            var severity = $('#alarmSeverity').val();
            var group = $('#deviceGroup').val();
            var textSearch = $('#textSearch').val();
            if(alarmType != '' || alarmName != '' || severity != '' || group != '' || textSearch != '') {
                var totalParam = '';
                totalParam = "/alarm-setting/search?alarmType="+alarmType+"&alarmName="+alarmName+"&severity="+severity+"&group="+group+"&textSearch="+textSearch;
                window.location.href = totalParam;
            }
        });

    $('#confirm-remove-row-dialog').click(function() {

            $.ajax({
                type: 'POST',
                url: "/alarm-setting/remove",
                data: JSON.stringify(rowDelete),
                success: function(AcsResponse) {
                    var successAlert = "";
                    var failedAlert = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result == 200) // success
                        {
                            successAlert = successAlert + rowDeleteName[''+id] + ", ";
                            $('.'+id).remove();
                            $("#parentId option[value="+id+"]").remove();
                        }
                        else { // failed
                            failedAlert = failedAlert + rowDeleteName[''+id] + ", ";
                        }
                    });
                    if (successAlert !== "") {

                        var alert_success = $('#alert_success_input').attr('class');
                        successAlert += alert_success;
                        $('#alert_success span.text-semibold').text(successAlert);
                        $("#checkAll").prop("indeterminate", false);
                        $('#alert_success').show();
                    }
                    if (failedAlert !== "") {
                        var alert_primary = $('#alert_primary_input').attr('class');
                        failedAlert += alert_primary;
                        $('#alert_primary span.text-semibold').text(failedAlert);
                        $('#alert_primary').show();
                    }
                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });

        $('#confirm-remove-dialog-row').dialog('close');
    });

    $('#confirm-remove-now-dialog').click(function() {
        var hasArea = false;
        var areaNeedRemove = [];
        var nameAreaNeedRemove = [];
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            var name = $(this).attr('name');
            if ($(this).is(':checked')) {
                areaNeedRemove.push(id);
                nameAreaNeedRemove[''+id] = name;
                hasArea = true;
            }
        });

        if (hasArea == false) {
            console.log('No Group to remove');
        } else {
            $.ajax({
                type: 'POST',
                url: "/alarm-setting/remove",
                data: JSON.stringify(areaNeedRemove),
                success: function(AcsResponse) {
                    var successAlert = "";
                    var failedAlert = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result == 200) // success
                        {
                            successAlert = successAlert + nameAreaNeedRemove[''+id] + ", ";
                            $('.'+id).remove();
                            $("#parentId option[value="+id+"]").remove();
                            var remove_data_template_opener_text = $('#confirm-remove-dialog-opener').html();
                            remove_data_template_opener_text = remove_data_template_opener_text.substring(0, remove_data_template_opener_text.length - 3);
                            $('#confirm-remove-dialog-opener').html(remove_data_template_opener_text);
                        }
                        else { // failed
                            failedAlert = failedAlert + nameAreaNeedRemove[''+id] + ", ";
                        }
                    });
                    if (successAlert !== "") {
                        var alert_success = $('#alert_success_input').attr('class');
                        successAlert += alert_success;
                        $('#alert_success span.text-semibold').text(successAlert);
                        $("#checkAll").prop("indeterminate", false);
                        $('#alert_success').show();
                    }
                    if (failedAlert !== "") {
                        var alert_primary = $('#alert_primary_input').attr('class');
                        failedAlert += alert_primary;
                        $('#alert_primary span.text-semibold').text(failedAlert);
                        $('#alert_primary').show();
                    }
                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });
        }
        $('#confirm-remove-dialog').dialog('close');
    });

   $('.twbs-prev-next').twbsPagination({
        totalPages: lastPagesVar,
        visiblePages: 4,
        startPage: startPageVar,
        prev: '&#8672;',
        next: '&#8674;',
        first: '&#8676;',
        last: '&#8677;',
        onPageClick: function (event, page) {
            if(page !== parseInt(paginator_data.attr('data-number'))) {
                var limitPageInput = parseInt($('.limit-page-input').val());
                pagingDataModel(page, limitPageInput);
            }
        }
    });

    function pagingDataModel(index, limit) {
            var totalParam = '';
            totalParam = "/alarm-setting?indexPage=" + index + "&limit=" + limit;
            window.location.href = totalParam;
    }

    $('.go-page-btn').click(function () {
        var goPageInputVar = $('.go-page-input').val();
        if(goPageInputVar.indexOf("-") >= 0
            || goPageInputVar.indexOf("+") >= 0 || goPageInputVar.indexOf(".") >= 0
            || goPageInputVar == ""){
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
        } else {
            var goPageInput = parseInt($('.go-page-input').val());
            var limitPageInput = parseInt($('.limit-page-input').val());
            console.log("lastPagesVar : "+ lastPagesVar);
            console.log("goPageInput : "+ goPageInput);
            if(0 < goPageInput && goPageInput <= lastPagesVar){
                pagingDataModel(goPageInput, limitPageInput);
            } else {
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
            }
        }
    });

    $("#alarmType").change(function(){
        var alarmType = $(this).val();
        var dataArray = [];
        dataArray.push({id: '', text: ''});
        if(alarmType !== '') {
        $('#alarmName').html('').select2({data: [{id: '', text: ''}]});

            $.ajax({
                type: 'POST',
                url: "/alarm-setting/getListAlarmByType",
                data: JSON.stringify(alarmType),
                success: function(AcsResponse) {
                    var successAlert = "";
                    var failedAlert = "";
                    $.each(AcsResponse.mapResult, function(k, v) {
                        dataArray.push({id: v, text: v});
                    });
                    $("#alarmName").html('').select2({data: dataArray});
                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });
        } else {
            $("#alarmName").html('').select2({data: dataArray});
        }

    });

    $("#groupFilter").change(function(){
        $("#listGroupFilter").val($("#groupFilter").val());
    });

    $("#editGroupFilter").change(function(){
        $("#listEditGroupFilter").val($("#editGroupFilter").val());
    });

        $( "#alarmNameCreat" ).focusout(function() {
                var validAlert = "";
                var inputTime = $("#alarmNameCreat").val();
                if(inputTime != '') {
                            $.ajax({
                                type: 'POST',
                                url: "/alarm-setting/checkNameExited",
                                data: JSON.stringify(inputTime),
                                success: function(AcsResponse) {
                                    var successAlert = "";
                                    var failedAlert = "";
                                    $.each(AcsResponse.mapResult, function(k, v) {
                        $("#alarmNameCreat").val('');
                        var alert = $('#alert_valid_input').attr('class');
                        validAlert += alert;
                        $('#time-valid span.text-semibold').text(validAlert);
                        $('#time-valid').show();
                                    });
                                },
                                error: function(data) {
                                    console.log('Error ' + JSON.stringify(data));
                                },
                                async: true
                            });
                    }

         });

        $( "#editAlarmName" ).focusout(function() {
                var validAlert = "";
                var alarmName = $("#editAlarmName").val();
                var alarmId = $("#alarmId").val();
                if(alarmName != '') {
                            $.ajax({
                                type: 'POST',
                                url: "/alarm-setting/"+alarmId+"/checkNameEditExited",
                                data: JSON.stringify(alarmName),
                                success: function(AcsResponse) {
                                    var successAlert = "";
                                    var failedAlert = "";
                                    $.each(AcsResponse.mapResult, function(k, v) {
                                        $("#editAlarmName").val('');
                                        var alert = $('#edit_alert_valid_input').attr('class');
                                        validAlert += alert;
                                        $('#edit-time-valid span.text-semibold').text(validAlert);
                                        $('#edit-time-valid').show();
                                    });
                                },
                                error: function(data) {
                                    console.log('Error ' + JSON.stringify(data));
                                },
                                async: true
                            });
                    }

         });

    showHideNotify();
    $('#alarmMonitorCreate').change(function() {
        showHideNotify();
    });

    $('#editAlarmMonitor').change(function() {
        showHideNotifyEdit();
    });

    function showHideNotify(){
        var isChecked = $('#alarmMonitorCreate').is(':checked');
        if(isChecked){
             $("#alarmNotifyCreate").removeAttr("disabled");
        } else {
             $("#alarmNotifyCreate").attr("disabled", true);
             $("#alarmNotifyCreate").prop( "checked", false );
        }
    }
    function showHideNotifyEdit(){
        var isChecked = $('#editAlarmMonitor').is(':checked');
        if(isChecked){
             $("#editAlarmNotify").removeAttr("disabled");
        } else {
             $("#editAlarmNotify").attr("disabled", true);
             $("#editAlarmNotify").prop( "checked", false );
        }
    }

    $("#select_parameter").hide();
    $("#select_parameter_table").hide();
    $("#select_notification").hide();

    $("#alarmTypeCreate").change(function(){
        alarmTypeCreate = $(this).val();
        if(alarmTypeCreate == 'PARAMETER_VALUE'){
            $("#select_parameter").show();
            $("#select_parameter_table").show();
            $("#select_notification").show();
            $('#alarmNameCreate').val('');
            $('#alarmNameCreate').prop('readonly', false);
        } else {
            if(alarmTypeCreate == 'REQUEST_FAIL') alarmTypeCreate = "Request failed";
            if(alarmTypeCreate == 'CONFIGURATION_FAIL') alarmTypeCreate = "Configuration device failed";
            if(alarmTypeCreate == 'UPDATE_FIRMWARE_FAIL') alarmTypeCreate = "Update  firmware failed";
            if(alarmTypeCreate == 'REBOOT_FAIL') alarmTypeCreate = "Reboot failed";
            if(alarmTypeCreate == 'FACTORY_RESET_FAIL') alarmTypeCreate = "Factory reset failed";

            $('#alarmNameCreate').val(alarmTypeCreate);
            $('#alarmNameCreate').prop('readonly', true);
            getListParameters();
            $("#select_parameter").hide();
            $("#select_notification").hide();
            $("#select_parameter_table").hide();
            $(".list-parameters").empty();
            $("#tableParameter").val('');
            $("#passive").prop( "checked", true );
            parametersSelectedValue = [];
        }
    });


    var modalAlarmSettingTime = $('#create-alarm-setting-time-dialog');
    var modalAlarmEditSettingTime = $('#edit-alarm-setting-time-dialog');

    $('input:radio').change(function() {
        var editTime = $("#editTime").val();
        var notification = $(this).val();
        if(notification == '1' && editTime == '') {
//            modalAlarmSettingTime.modal('show');
            $('#create-alarm-setting-time-dialog').dialog('open');
        }
        if(notification == '1' && editTime !== '') {
//            modalAlarmEditSettingTime.modal('show');
            $('#edit-alarm-setting-time-dialog').dialog('open');
        }
    });

                    modalAlarmSettingTime.unbind('click').find('.btnSave').click(function () {
                    var timeSetting = modalAlarmSettingTime.find('.timeSetting').val();
                    if(timeSetting <= 0) {
                        new PNotify({ text: 'The time is not valid', addclass: 'bg-danger'});
                    } else {
                        $('#time').val(timeSetting);
                        $('#create-alarm-setting-time-dialog').dialog('close');
//                        modalAlarmSettingTime.modal('hide');
                    }
                })

                    modalAlarmEditSettingTime.unbind('click').find('.btnSave').click(function () {
                    var timeSetting = modalAlarmEditSettingTime.find('.editTimeSetting').val();
                    if(timeSetting <= 0) {
                        new PNotify({ text: 'The time is not valid', addclass: 'bg-danger'});
                    } else {
                        $('#editTime').val(timeSetting);
//                        modalAlarmEditSettingTime.modal('hide');
                        $('#edit-alarm-setting-time-dialog').dialog('close');
                    }
                })

    modalAlarmSettingTime.find('.btnCancel').click(function () {
//        modalAlarmSettingTime.modal('hide');
          $('#create-alarm-setting-time-dialog').dialog('close');
    });

    modalAlarmEditSettingTime.find('.btnCancel').click(function () {
//        modalAlarmEditSettingTime.modal('hide');
          $('#edit-alarm-setting-time-dialog').dialog('close');
    });


    $("#editAlarmType").change(function(){
        editAlarmType = $(this).val();
        if(editAlarmType == 'PARAMETER_VALUE'){
            $("#edit_select_parameter").show();
            $("#edit_select_parameter_table").show();
            $('#editAlarmName').val('');
            $('#editAlarmName').prop('readonly', false);
            $("#edit_notification").show();
        } else {
            $('#editAlarmName').val(editAlarmType);
            $('#editAlarmName').prop('readonly', true);
            getListParameters();
            $("#edit_select_parameter").hide();
            $("#edit_select_parameter_table").hide();
            $(".edit-list-parameters").empty();
            $("#editTableParameter").val('');
            editParametersSelectedValue = [];
            $("#edit-passive").prop( "checked", true );
            $("#edit_notification").hide();
        }
    });

});
