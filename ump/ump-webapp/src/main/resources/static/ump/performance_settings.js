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

$(document).ready(function() {

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

    $('#create-performance-settings-dialog').dialog({
        autoOpen: false,
        modal: false,
        width: '75%',
        height: 'auto'
    });

    $('#edit-performance-settings-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '75%',
        height: 'auto'
    });

    $('#new-performance-dialog-opener').click(function() {
        $('#create-performance-settings-dialog').dialog('open');
    });

    $('#create-performance-dialog-close').click(function() {
        $('#create-performance-settings-dialog').dialog('close');
        $('#form_create_performance_settings')[0].reset();
    });

    $('#edit-performance-dialog-close').click(function() {
        $('#edit-performance-settings-dialog').dialog('close');
        $('#form_edit_performance_settings')[0].reset();
    });

    firstLoadPage();

    function firstLoadPage() {
        $('#filter').prop('disabled', true);

        $(".file-styled").uniform({
            fileButtonClass: 'action btn btn-default'
        });

        $('#interval').prop('required', true);
        $('#Manufacturer').prop('required', true);
        $('#model').prop('required', true);
        $('#serial').prop('required', true);

        $('#editInterval').prop('required', true);
        $('#editManufacturer').prop('required', true);
        $('#editModel').prop('required', true);
        $('#editSerial').prop('required', true);

        $(":file").filestyle('placeholder', "No file to choose");
    }

    $('#editGroup').click(function() {
        $('#editFilter').prop('disabled', false);
        $('#editFile').prop('checked', false);
        $('#editSingle').prop('checked', false);
        $('#editManufacturer').prop('disabled', true);
        $('#editModel').prop('disabled', true);
        $('#editSerial').prop('disabled', true);

        $('#editManufacturer').prop('required', false);
        $('#editModel').prop('required', false);
        $('#editSerial').prop('required', false);
        $('#editFilter').prop('required', true);
        $('#editExternalFile').prop('required', false);
    });



    $('#editSingle').click(function() {
        $('#editFilter').prop('disabled', true);
        $('#editFile').prop('checked', false);
        $('#editGroup').prop('checked', false);
        $('#editManufacturer').prop('disabled', false);
        $('#editModel').prop('disabled', false);
        $('#editSerial').prop('disabled', false);

        $('#editManufacturer').prop('required', true);
        $('#editModel').prop('required', true);
        $('#editSerial').prop('required', true);
        $('#editFilter').prop('required', false);
        $('#editExternalFile').prop('required', false);
    });

    $('#editFile').click(function() {
        $('#editFilter').prop('disabled', true);
        $('#editSingle').prop('checked', false);
        $('#editGroup').prop('checked', false);
        $('#editManufacturer').prop('disabled', true);
        $('#editModel').prop('disabled', true);
        $('#editSerial').prop('disabled', true);

        $('#editManufacturer').prop('required', false);
        $('#editModel').prop('required', false);
        $('#editSerial').prop('required', false);
        $('#editFilter').prop('required', false);
        $('#editExternalFile').prop('required', true);
    });




    $('#group').click(function() {
        $('#filter').prop('disabled', false);
        $('#file').prop('checked', false);
        $('#single').prop('checked', false);
        $('#Manufacturer').prop('disabled', true);
        $('#model').prop('disabled', true);
        $('#serial').prop('disabled', true);

        $('#Manufacturer').prop('required', false);
        $('#model').prop('required', false);
        $('#serial').prop('required', false);
        $('#filter').prop('required', true);
        $('#externalFile').prop('required', false);
    });



    $('#single').click(function() {
        $('#filter').prop('disabled', true);
        $('#file').prop('checked', false);
        $('#group').prop('checked', false);
        $('#Manufacturer').prop('disabled', false);
        $('#model').prop('disabled', false);
        $('#serial').prop('disabled', false);

        $('#Manufacturer').prop('required', true);
        $('#model').prop('required', true);
        $('#serial').prop('required', true);
        $('#filter').prop('required', false);
        $('#externalFile').prop('required', false);
    });

    $('#file').click(function() {
        $('#filter').prop('disabled', true);
        $('#single').prop('checked', false);
        $('#group').prop('checked', false);
        $('#Manufacturer').prop('disabled', true);
        $('#model').prop('disabled', true);
        $('#serial').prop('disabled', true);

        $('#Manufacturer').prop('required', false);
        $('#model').prop('required', false);
        $('#serial').prop('required', false);
        $('#filter').prop('required', false);
        $('#externalFile').prop('required', true);
    });

    $("#Manufacturer").change(function(){
        var manufacturerDataVar = $(this).val();
        var resultJSON = $("#addNewFileParam").val();

        var result = JSON.parse(resultJSON);
        $('#serial').html('').select2({data: [{id: '', text: ''}]});
        $('#model').html('').select2({data: [{id: '', text: ''}]});
        var dataArray = [];
        dataArray.push({id: '', text: ''});
        $.each(result, function(k, v) {
            if(manufacturerDataVar == k){
                $.each(v, function( index, value ) {
                      $.each(value, function( k, v ) {
                           dataArray.push({id: k, text: k});
                      });
                });
            }
        });
        $("#model").html('').select2({data: dataArray});
    });

    $("#model").change(function(){
        var manufacturerDataVar = $("#Manufacturer").val();
        var modelName = $(this).val();
        $('#serial').html('').select2({data: [{id: '', text: ''}]});
        var dataArray = [];

            $.ajax({
                type: 'POST',
                url: "/performance-setting/getListSerialNumber?Manufacturer="+manufacturerDataVar+"&ModelName="+modelName,
                success: function(data) {
                    var successAlert = "";
                    var failedAlert = "";
                    $.each(data, function(k, v) {
                        if (v == 200) // success
                        {
                            dataArray.push({id: k, text: k});
                        }

                    });
                     $("#serial").html('').select2({data: dataArray});
                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });
    });

    $("#editManufacturer").change(function(){
        var manufacturerDataVar = $(this).val();
        var resultJSON = $("#addNewFileParam").val();
        var result = JSON.parse(resultJSON);
        $('#editSerial').html('').select2({data: [{id: '', text: ''}]});
        $('#editModel').html('').select2({data: [{id: '', text: ''}]});
        var dataArray = [];
        dataArray.push({id: '', text: ''});
        $.each(result, function(k, v) {
            if(manufacturerDataVar == k){
                $.each(v, function( index, value ) {
                      $.each(value, function( k, v ) {
                           dataArray.push({id: k, text: k});
                      });
                });
            }
        });
        $("#editModel").html('').select2({data: dataArray});
    });

    $("#editModel").change(function(){
        var manufacturerDataVar = $("#editManufacturer").val();
        var modelName = $(this).val();
        $('#editSerial').html('').select2({data: [{id: '', text: ''}]});
        var dataArray = [];

            $.ajax({
                type: 'POST',
                url: "/performance-setting/getListSerialNumber?Manufacturer="+manufacturerDataVar+"&ModelName="+modelName,
                success: function(data) {
                    var successAlert = "";
                    var failedAlert = "";
                    $.each(data, function(k, v) {
                        if (v == 200) // success
                        {
                            dataArray.push({id: k, text: k});
                        }

                    });
                     $("#editSerial").html('').select2({data: dataArray});
                     $("#editSerial").val(editSerialValue).change();
                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });
    });

            $("#traffic").change(function(){
                var traffic = $(this).val();
                if(traffic == 'LAN' || traffic == 'WAN' || traffic == 'WLAN') {
                    $("#trafficType").show();
                    $("#type").val('RECEIVED').trigger("change");
                    $("#trafficInterval").show();
                    $("#interval").val('');
                    $('#interval').prop('required', true);
                }
                else {
                    $("#trafficType").hide();
                    $("#trafficInterval").show();
                    $("#interval").val('');
                    $('#interval').prop('required', true);
                }
            });

            $("#editTraffic").change(function(){
                var traffic = $(this).val();
                if(traffic == 'LAN' || traffic == 'WAN' || traffic == 'WLAN') {
                    $("#editTrafficType").show();
                    $("#editTrafficInterval").show();
                    $('#editInterval').prop('required', true);
                }
                else {
                    $("#editTrafficType").hide();
                    $("#editTrafficInterval").show();
                    $('#editInterval').prop('required', true);
                }
            });

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

    $('.performance-remove-btn').click(function() {
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
            var traffic = $('#searchTraffic').val();
            var monitoring = $('#monitoring').val();
            var fromDate = $('#searchFromDate').val();
            var toDate = $('#searchToDate').val();
            var textSearch = $('#textSearch').val();
            if(traffic != '' || monitoring != '' || fromDate != '' || toDate != '' || textSearch != '') {
                var totalParam = '';
                totalParam = "/performance-setting/search?traffic="+traffic+"&monitoring="+monitoring+"&fromDate="+fromDate+"&toDate="+toDate+"&textSearch="+textSearch;
                window.location.href = totalParam;
            }
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

    $('#confirm-remove-row-dialog').click(function() {
            $.ajax({
                type: 'POST',
                url: "/performance-setting/remove",
                data: JSON.stringify(rowDelete),
                success: function(data) {
                    var successAlert = "";
                    var failedAlert = "";
                    $.each(data, function(id, result) {
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
                url: "/performance-setting/remove",
                data: JSON.stringify(areaNeedRemove),
                success: function(data) {
                    var successAlert = "";
                    var failedAlert = "";
                    $.each(data, function(id, result) {
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

    $('#confirm-remove-dialog-close').click(function() {
        $('#confirm-remove-dialog').dialog('close');
    });

        $('#confirm-remove-dialog-row-close').click(function() {
            $('#confirm-remove-dialog-row').dialog('close');
        });

    $('.close').click(function() {
        $(this).parent().hide();
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
        if(!isNaN(index) && !isNaN(limit)){
            var totalParam = '';
            totalParam = "/performance-setting?indexPage=" + index + "&limit=" + limit;
            window.location.href = totalParam;
            }
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

    var editSerialValue = '';

    $('.performance-edit-btn').click(function() {

        var detail = $(this).attr('type');
        var arr = detail.split("|");

        var id = arr[0];
        var stasticsType = arr[1];
        var type = arr[2];
        var stasticsInterval = arr[3];
        var monitoring = arr[4];
        var deviceId = arr[5];
        var deviceGroupId = arr[6];
        var textFromDate = arr[7];
        var textToDate = arr[8];
        var fileName = arr[9];
        var manufacturer = arr[10];
        var modelName = arr[11];
        var serialNumber =arr[12];
        $('#performanceId').val(id);
        $('#editManufacturer').val('').trigger("change");

        $('#editTraffic').val(stasticsType).trigger("change");
        if('LAN' == stasticsType || 'WAN' == stasticsType  || 'WLAN' == stasticsType ) {
            $('#editType').val(type).trigger("change");
        }

        if('VOIP' !== stasticsType && 'STP' !== stasticsType) {
             $('#editInterval').val(stasticsInterval);
        }

                if(stasticsType == 'LAN' || stasticsType == 'WAN' || stasticsType == 'WLAN') {
                    $("#editTrafficType").show();
                    $("#editTrafficInterval").show();
                    $('#editInterval').prop('required', true);
                }
                if(stasticsType == 'CPU' || stasticsType == 'RAM') {
                    $("#editTrafficType").hide();
                    $("#editTrafficInterval").show();
                    $('#editInterval').prop('required', true);
                }
                if(stasticsType == 'VOIP' || stasticsType == 'STB') {
                    $("#editTrafficType").hide();
                    $("#editTrafficInterval").hide();
                    $('#editInterval').prop('required', false);
                }

        $('#editSingle').prop('checked', false);
        editSerialValue = '';
        if(monitoring == '1') {
            $('#editSingle').prop('checked', true);
            $('#editManufacturer').val(manufacturer).change();
            editSerialValue = serialNumber;
            $('#editModel').val(modelName).change();
            $('#editSerial').val(serialNumber).change();
        }
        if(monitoring == '2') {
            $('#editGroup').prop('checked', true);
            $('#editFilter').val(deviceGroupId).trigger("change");
        }

        if(monitoring == '3') {
            $('#editFile').prop('checked', true);
            $(":file").filestyle('placeholder', fileName);
        }
        $('#editFromDate').val(textFromDate);
        $('#editToDate').val(textToDate);

        $('#edit-performance-settings-dialog').dialog('open');
    });


   $('.performance-detail-btn').click(function() {
        var detail = $(this).attr('type');
        var arr = detail.split("|");

        var performanceSettingId = arr[0];
        var statisticType = arr[1];
        var type = arr[2];
        var stasticsInterval = arr[3];
        var monitoring = arr[4];
        var deviceId = arr[5];
        var deviceGroupId = arr[6];
        var textFromDate = arr[7];
        var textToDate = arr[8];
        var fileName = arr[9];
        var manufacturer = arr[10];
        var modelName = arr[11];
        var serialNumber =arr[12];

        console.log('monitoring : '+monitoring);
        console.log('type : '+type);

        $('#performanceSettingRamCpu').val(deviceGroupId+'/'+performanceSettingId+'/'+statisticType+'/'
                                                +textFromDate+'/'+textToDate+'/'+type+'/'+manufacturer+'/'+modelName+'/'+serialNumber+'/'+monitoring);
        $('#performanceSettingLanWanWLan').val(deviceGroupId+'/'+performanceSettingId+'/'+statisticType+'/'
                                                +textFromDate+'/'+textToDate+'/'+type+'/'+manufacturer+'/'+modelName+'/'+serialNumber+'/'+monitoring);
        $('#performanceSettingStb').val(deviceGroupId+'/'+performanceSettingId+'/'+statisticType+'/'
                                                +textFromDate+'/'+textToDate+'/'+type+'/'+manufacturer+'/'+modelName+'/'+serialNumber+'/'+monitoring);
        $('#performanceSettingVoip').val(deviceGroupId+'/'+performanceSettingId+'/'+statisticType+'/'
                                                +textFromDate+'/'+textToDate+'/'+type+'/'+manufacturer+'/'+modelName+'/'+serialNumber+'/'+monitoring);

        if("LAN/WAN/WLAN".indexOf(statisticType) != -1){
            getDataLanWanWLan(statisticType, deviceGroupId, performanceSettingId, textFromDate, textToDate,
                                    monitoring, manufacturer, modelName, serialNumber, type);
        } else if("RAM/CPU".indexOf(statisticType) != -1){
            getDataRamCpu(statisticType, deviceGroupId, performanceSettingId, textFromDate, textToDate,
                                    monitoring, manufacturer, modelName, serialNumber, type);
        } else if("STB".indexOf(statisticType) != -1){
            getDataStb(statisticType, deviceGroupId, performanceSettingId, textFromDate, textToDate,
                                            monitoring, manufacturer, modelName, serialNumber, type);
        } else if("VOIP".indexOf(statisticType) != -1){
            getDataVoiP(statisticType, deviceGroupId, performanceSettingId, textFromDate, textToDate,
                                                    monitoring, manufacturer, modelName, serialNumber, type);
        }

   });

    function getDataVoiP(statisticType, deviceGroupId, performanceSettingId, textFromDate, textToDate,
                                monitoring, manufacturer, modelName, serialNumber, type){
        $.ajax({
        type: "GET",
        url: "/performance-setting/voip-view-detail",
        data: {
            statisticType: statisticType,
            deviceGroupId: deviceGroupId,
            performanceSettingId: performanceSettingId,
            textFromDate: textFromDate,
            textToDate: textToDate,
            monitoring: monitoring,
            manufacturer: manufacturer,
            modelName: modelName,
            serialNumber: serialNumber,
            type: type,
            indexPage:'1',
            limitPage:'20'
        },
        dataType:"text",
        success: function (data) {
            data1 = JSON.parse(data);
            if(data1 != null){
                showModalVoip(data1, statisticType);
            }
            $('#modal_voip_detail').modal({
                show: true
            });
        },
        error: function (e) {
            console.log('error : '+e);
        }
        });
    }

    function getDataStb(statisticType, deviceGroupId, performanceSettingId, textFromDate, textToDate,
                                monitoring, manufacturer, modelName, serialNumber, type){
        $.ajax({
        type: "GET",
        url: "/performance-setting/stb-view-detail",
        data: {
            statisticType: statisticType,
            deviceGroupId: deviceGroupId,
            performanceSettingId: performanceSettingId,
            textFromDate: textFromDate,
            textToDate: textToDate,
            monitoring: monitoring,
            manufacturer: manufacturer,
            modelName: modelName,
            serialNumber: serialNumber,
            type: type,
            indexPage:'1',
            limitPage:'20'
        },
        dataType:"text",
        success: function (data) {
            data1 = JSON.parse(data);
            if(data1 != null){
                showModalStb(data1, statisticType);
            }
            $('#modal_stb_detail').modal({
                show: true
            });
        },
        error: function (e) {
            console.log('error : '+e);
        }
        });
    }

    function showModalStb(data1, statisticType){

        var dataTableFix = '';
        var headerTime = '';
        var dataTime = '';

        $("#div_stb_fix_thead").empty();
        $("#div_stb_fix_tbody").empty();
        $("#div_stb_time_thead").empty();
        $("#div_stb_time_tbody").empty();

        if(headerTime != ''){
            $('#alert_success_stb').hide();
            $('#div_stb_paging').show();
        } else {
            $('#alert_success_stb').show();
            $('#div_stb_paging').hide();
        }
    }

    function showModalVoip(data1, statisticType){

        var dataTableFix = '';
        var headerTime = '';
        var dataTime = '';

        $("#div_voip_fix_thead").empty();
        $("#div_voip_fix_tbody").empty();
        $("#div_voip_time_thead").empty();
        $("#div_voip_time_tbody").empty();

        if(headerTime != ''){
            $('#alert_success_voip').hide();
            $('#div_voip_paging').show();
        } else {
            $('#alert_success_voip').show();
            $('#div_voip_paging').hide();
        }
    }

    function showModalLanWanWLan(data1, statisticType, type){

        var dataTableFix = '';
        var headerTime = '';
        var temp = '';
        var dataTime = '';
        var nameInterface = [];
        var array = [];

        $("#div_lan_wan_wLan_fix_tbody").empty();
        $("#div_lan_wan_wLan_fix_thead").empty();
        $("#div_lan_wan_wLan_time_thead").empty();
        $("#div_lan_wan_wLan_time_tbody").empty();

        var performanceTypeName = '';
        if(statisticType == 'LAN'){
            performanceTypeName = 'eth';
        } else if(statisticType == 'WAN'){
            performanceTypeName = 've';
        } else if(statisticType == 'WLAN'){
            performanceTypeName = 'wl';
        }

        $(data1).each(function (idx, o) {
            data2 = jQuery.parseJSON(JSON.stringify(o));
            if(data2.headersNameInterface != null){
                $.each(data2.headersNameInterface, function(index, value) {
                    array.push(value);
                    value1 = value.split("|");
                    headerTime += ','+value1[0];
                });
            }

        });

        if(headerTime != ''){
            $('#alert_success_lan_wan_wLan').hide();
            $('#div_lan_wan_wlan_paging').show();
            var countCheckBox =0;
            $(data1).each(function (idx, o) {
                data2 = jQuery.parseJSON(JSON.stringify(o));

                if(data2.totalPage != null){
                    console.log('data2.indexPage : '+data2.indexPage);
                    console.log('data2.totalPage : '+data2.totalPage);
                    console.log('data2.lastPage : '+data2.lastPage);

                    $("#data-ram-cpu-paginator").attr("data-number", data2.indexPage);
                    $("#data-ram-cpu-paginator").attr("data-totalPages", data2.totalPage);
                    $("#data-ram-cpu-paginator").attr("data-lastPage", data2.lastPage);

                    $(".go-page-input-ram-cpu").prop('min',1);
                    $(".go-page-input-ram-cpu").prop('max',data2.lastPage);
                }

                $.each(data2, function(key, value){
                    if(key.startsWith(performanceTypeName)){
                        dataTableFix += '<tr data-keyRow="'+key+'">'
                                    + '<td style="padding: 10px;"><center><input type="checkbox" id="'+countCheckBox+data2.id+'" name="'+key+'" class="myCheckBoxLanWanWWLan"/></center></td>'
                                    + '<td nowrap style="text-align: start;">'+data2.manufacture+'</td>'
                                    + '<td nowrap style="text-align: start;">'+data2.modelName+'</td>'
                                    + '<td nowrap style="text-align: start;">'+data2.serialNumber+'</td>';
                        nameInterface.push(key);
                        $(value).each(function (idx1, o1) {
                            dataTableFix += '<td nowrap style="text-align: start;">'+key+'</td>'
                            + '<td nowrap style="text-align: start;">'+o1.bytesFinal+'</td>'
                            + '<td nowrap style="text-align: start;">'+o1.pktsFinal+'</td>'
                            + '<td nowrap style="text-align: start;">'+o1.errorsFinal+'</td>'
                            + '<td nowrap style="text-align: start;">'+o1.dropsFinal+'</td>';
                        });
                         dataTableFix += '</tr>';
                         countCheckBox++;
                    }
                });
            });
//            console.log('countCheckBox : '+countCheckBox);
            dataHeaderTime = headerTime.substring(1,headerTime.length).split(",");
            dataTime = '';
            var realDataHeaderTime = '';
            var part1='';
            var part2='';
            var arrayTime=[];

            for(var i = 0;i<dataHeaderTime.length;i++){
                if(dataHeaderTime[i] != dataHeaderTime[i-1]){
                    part1 += '<th colspan="4" nowrap>'+dataHeaderTime[i]+'</th>';
                    part2 += '<th style="width: 10%;">Bytes</th>'
                            + '<th style="width: 10%;">Pkts</th>'
                            + '<th style="width: 10%;">Errors</th>'
                            + '<th style="width: 10%;">Drops</th>';
                    arrayTime.push(dataHeaderTime[i]);
                }
            }
            realDataHeaderTime = part1 + '<tr>' +part2+ '</tr>';
            var booleanCheckBoxRight = false;
            $(data1).each(function (idx, o) {
                data2 = jQuery.parseJSON(JSON.stringify(o));
                if(data2.data != null && booleanCheckBoxRight == false){
                    for(var interface = 0;interface<nameInterface.length;interface++) {
                        dataTimeTemp = '<tr>';
                        for(var time = 0;time<arrayTime.length;time++) {
                            check = false;
                            $.each(data2.data, function(index, value) {
                                if(arrayTime[time] == value.timestamp
                                        && nameInterface[interface] == value.name){
                                    if(value.bytes == null){
                                        dataTimeTemp += '<td data-keyRow="'+value.name+'">'+'0'+'</td>';
                                    } else {
                                        dataTimeTemp += '<td data-keyRow="'+value.name+'">'+value.bytes+'</td>';
                                    }
                                    if(value.pkts == null){
                                        dataTimeTemp += '<td>'+'0'+'</td>';
                                    } else {
                                        dataTimeTemp += '<td>'+value.pkts+'</td>';
                                    }
                                    if(value.errors == null){
                                        dataTimeTemp += '<td>'+'0'+'</td>';
                                    } else {
                                        dataTimeTemp += '<td>'+value.errors+'</td>';
                                    }
                                    if(value.drops == null){
                                        dataTimeTemp += '<td>'+'0'+'</td>';
                                    } else {
                                        dataTimeTemp += '<td>'+value.drops+'</td>';
                                    }
                                    check = true;
                                }
                            });

                            if(!check){
                                dataTimeTemp += '<td>'+'0'+'</td>';
                                dataTimeTemp += '<td>'+'0'+'</td>';
                                dataTimeTemp += '<td>'+'0'+'</td>';
                                dataTimeTemp += '<td>'+'0'+'</td>';
                            }
                        }
                        dataTime += dataTimeTemp + '</tr>';
                        dataTimeTemp = '';
                        if(interface == (countCheckBox - 1)){
                            booleanCheckBoxRight = true;
                        }
                    }

                }
            });

            var headerTableFix = '<tr>'
                                +'<th rowspan="2" style="padding: 11px;" nowrap><center><input type="checkbox" id="checkAllLanWanWLan" name="checkAllLanWanWLan"/></center></th>'
                                +'<th rowspan="2" nowrap>Manufacturer</th>'
                                +'<th rowspan="2" nowrap>Model name</th>'
                                +'<th rowspan="2" nowrap>Serial number</th>'
                                +'<th rowspan="2" nowrap>Interval</th>';
            if(type == 'TRANSMITTED'){
                headerTableFix += '<th colspan="4" nowrap>Traffic Transmitted</th>';
            } else {
                headerTableFix += '<th colspan="4" nowrap>Traffic Received</th>';
            }

            headerTableFix += '<tr>'
                                +'<th style="width: 10%;">Bytes</th>'
                                +'<th style="width: 10%;">Pkts</th>'
                                +'<th style="width: 10%;">Errors</th>'
                                +'<th style="width: 10%;">Drops</th>'
                                +'</tr>'
                                +'</tr>';

            $("#div_lan_wan_wLan_fix thead").append(headerTableFix);
            $("#div_lan_wan_wLan_fix tbody").append(dataTableFix);

            $("#div_lan_wan_wLan_time thead").append('<tr>'+realDataHeaderTime+'</tr>');
            $("#div_lan_wan_wLan_time tbody").append(dataTime);

            $('#checkAllLanWanWLan').unbind('change');
                $('#checkAllLanWanWLan').change(function() {
                    console.log('check');
                    if ($(this).is(":checked")) {
                        checkAllLanWanWLan();
                    } else {
                        unCheckAllLanWanWLan();
                    }
            });

        } else {
            $('#alert_success_lan_wan_wLan').show();
            $('#div_lan_wan_wlan_paging').hide();

        }
    }

    function checkAllLanWanWLan() {
        $('.myCheckBoxLanWanWWLan').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
        });
    }

    function unCheckAllLanWanWLan() {
        $('.myCheckBoxLanWanWWLan').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
        });
    }

    function getDataLanWanWLan(statisticType, deviceGroupId, performanceSettingId, textFromDate, textToDate,
                                monitoring, manufacturer, modelName, serialNumber, type){
        $.ajax({
        type: "GET",
        url: "/performance-setting/lan-wan-wLan-view-detail",
        data: {
            statisticType: statisticType,
            deviceGroupId: deviceGroupId,
            performanceSettingId: performanceSettingId,
            textFromDate: textFromDate,
            textToDate: textToDate,
            monitoring: monitoring,
            manufacturer: manufacturer,
            modelName: modelName,
            serialNumber: serialNumber,
            type: type,
            indexPage:'1',
            limitPage:'20'
        },
        dataType:"text",
        success: function (data) {
            data1 = JSON.parse(data);
            if(data1 != null){
                showModalLanWanWLan(data1, statisticType, type);
            }
            var name = '';
            console.log('statisticType : '+statisticType);
            if(statisticType == 'LAN'){
                name = 'LAN Statistic List';
            } else if(statisticType == 'WAN'){
                 name = 'WAN Statistic List';
            } else {
                name = 'WLAN Statistic List';
            }
            $(".modal-header #myModalLabelLanWanWLan").text( name );
            $('#modal_lan_wan_wlan_detail').modal({
                show: true
            });
        },
        error: function (e) {
            console.log('error : '+e);
        }
        });
    }


    function getDataRamCpu(statisticType, deviceGroupId, performanceSettingId, textFromDate, textToDate,
                                monitoring, manufacturer, modelName, serialNumber, type){
        $.ajax({
        type: "GET",
        url: "/performance-setting/ram-cpu-view-detail",
        data: {
            statisticType: statisticType,
            deviceGroupId: deviceGroupId,
            performanceSettingId: performanceSettingId,
            textFromDate: textFromDate,
            textToDate: textToDate,
            monitoring: monitoring,
            manufacturer: manufacturer,
            modelName: modelName,
            serialNumber: serialNumber
        },
        dataType:"text",
        success: function (data) {
            data1 = JSON.parse(data);
            console.log(typeof(data1));
            console.log(JSON.stringify(data1));
            if(data1 != null) {
                showModalRamCpu(data1, statisticType);
            }
            name = '';
            if(statisticType == 'RAM'){
                name = 'RAM Statistic List';
            } else {
                name = 'CPU Statistic List';
            }
            $(".modal-header #myModalLabelRamCpu").text( name );
            $('#modal_ram_cpu_detail').modal({
                        show: true
            });
        },
        error: function (e) {
            console.log('error : '+e);
        }
        });
    }

    function showModalRamCpu(data1, statisticType){

        var dataTableFix = '';
        var headerTime = '';
        var dataTime = '';
        $("#data-ram-cpu-paginator").removeAttr("data-number");
        $("#data-ram-cpu-paginator").removeAttr("data-totalPages");
        $("#data-ram-cpu-paginator").removeAttr("data-lastPage");

        $("#div_ram_cpu_fix_thead").empty();
        $("#div_ram_cpu_fix_tbody").empty();
        $("#div_ram_cpu_time_thead").empty();
        $("#div_ram_cpu_time_tbody").empty();

        $(data1).each(function (idx, o) {
            data2 = jQuery.parseJSON(JSON.stringify(o));
            if(data2.header != null){
                $.each(data2.header, function(index, value) {
                    value = value.replace('T',' ').replace('Z','');
                    headerTime += ','+value;
                });
            }
        });
        console.log('headerTime : '+headerTime);

        if(headerTime != ''){
            $('#alert_success_ram_cpu').hide();
            $('#div_ram_cpu_paging').show();
            var countCheckBoxR =0;
            $(data1).each(function (idx, o) {
                data2 = jQuery.parseJSON(JSON.stringify(o));

                if(data2.id != null){
                    dataTableFix += '<tr data-keyRow="'+countCheckBoxR+data2.id+'">'
                    dataTableFix += '<td style="padding: 10px;"><center><input type="checkbox" id="'+countCheckBoxR+data2.id+'" name="'+statisticType+'" class="myCheckBoxRamCpu"/></center></td>'
                    dataTableFix += '<td nowrap style="text-align: start;">'+data2.manufacture+'</td>';
                    dataTableFix += '<td nowrap style="text-align: start;">'+data2.modelName+'</td>';
                    dataTableFix += '<td nowrap style="text-align: start;">'+data2.serialNumber+'</td>';
                    dataTableFix += '<td nowrap style="text-align: start;">'+data2.cpuRamPercent+'</td></tr>';
                    var temp = '';
                    lol = headerTime.substring(1,headerTime.length).split(",");
                    for(var i = 0;i< lol.length;i++){
                        $(data2.data).each(function (idx1, o1) {
                            var d1 = jQuery.parseJSON(JSON.stringify(o1));
                            valueHeader = d1.timeHeader.replace('T',' ').replace('Z','');
                            if(lol[i] == valueHeader){
                                temp += '<td>'+d1.timeData+'</td>';
                            }
                        });
                    }
                    dataTime += '<tr data-keyRow="'+countCheckBoxR+data2.id+'">'+temp+'</tr>';
                    countCheckBoxR++;
                }
            });

            var headerTableFix = '<tr>'
                                +'<th rowspan="2" style="padding: 11px;" nowrap><center><input type="checkbox" id="checkAllRamCpu" name="checkAllRamCpu"/></center></th>'
                                +'<th nowrap>Manufacturer</th>'
                                +'<th nowrap>Model name</th>'
                                +'<th nowrap>Serial number</th>';
            if(statisticType == "CPU"){
                headerTableFix +='<th nowrap>CPU (%)</th></tr>';
            } else {
                headerTableFix +='<th nowrap>RAM (%)</th></tr>';
            }

            $("#div_ram_cpu_fix thead").append(headerTableFix);
            $("#div_ram_cpu_fix tbody").append(dataTableFix);


            dataHeaderTime = headerTime.substring(1,headerTime.length).split(",");
            var realDataHeaderTime = '';
            for(var i = 0;i< dataHeaderTime.length;i++){
                realDataHeaderTime += '<th  nowrap>'+dataHeaderTime[i]+'</th>';
            }
            $("#div_ram_cpu_time thead").append('<tr>'+realDataHeaderTime+'</tr>');
            $("#div_ram_cpu_time tbody").append(dataTime);

            $('#checkAllRamCpu').unbind('change');
            $('#checkAllRamCpu').change(function() {
                if ($(this).is(":checked")) {
                    checkAllRamCpu();
                } else {
                    unCheckAllRamCpu();
                }
            });

        } else {
            $('#alert_success_ram_cpu').show();
            $('#div_ram_cpu_paging').hide();
        }
    }

    function checkAllRamCpu() {
        $('.myCheckBoxRamCpu').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
        });
    }

    function unCheckAllRamCpu() {
        $('.myCheckBoxRamCpu').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
        });
    }

});