$(document).ready(function() {
    $('.select2').select2({
        allowClear: true
    });

    var deleteIds = [];
    var deleteParams = [];
    var deleteStatus = [];
    var url ='';

    $("#serial-number-search").keyup(function(event){
        if(event.keyCode == 13){
            searchHard();
        }
    });
    $("#group-filter-search").change(function(){
        searchHard();
    });
    $("#severity-data-search").change(function(){
        searchHard();
    });
    $("#alarm-type-search").change(function(){
        searchHard();
    });
    $("#status-search").change(function(){
        searchHard();
    });
    $("#alarm-name-search").change(function(){
        searchHard();
    });

    $('#search-all-btn').click(function() {
        var searchAll = $('#search-all').val();
        if(searchAll != ''){
            totalParams = "/alarm/search?"
                              + "indexPage=1&limit="+limitPageInput
                              + "&search_all=" + searchAll;
         } else {
            totalParams = "/alarm";
         }

         window.location.href = totalParams;

    });

    $("#raised-from-search").keyup(function(event){
        if(event.keyCode == 13){
             if(validateRaisedTimeSearch()){
             searchHard();}
        }
    });
    $("#raised-to-search").keyup(function(event){
        if(event.keyCode == 13){

            if(validateRaisedTimeSearch()){
            searchHard();}
        }
    });

    function validateRaisedTimeSearch(){
        var raisedTimeFrom = $("#raised-from-search").val();
        var raisedTimeTo = $("#raised-to-search").val();

        if(raisedTimeFrom == '' && raisedTimeTo == ''){
            $('#raised-from-search-label').removeClass("red");
            $('#raised-to-search-label').removeClass("red");
            return false;
        }

        if(raisedTimeFrom == '' && raisedTimeTo != ''){
            $('#raised-from-search-label').addClass("red");
            $('#raised-to-search-label').removeClass("red");
            return false;
        }

        if(raisedTimeFrom != '' && raisedTimeTo == ''){
            $('#raised-to-search-label').addClass("red");
            $('#raised-from-search-label').removeClass("red");
            return false;
        }

        return true;
    }

    function searchHard(indexPage,limitPageInput){

        var serialNumber = $("#serial-number-search").val();
        var severity = $("#severity-data-search").val();
        var alarmType = $("#alarm-type-search").val();
        var status = $("#status-search").val();
        var groupFilter = $("#group-filter-search").val();
        var alarmName = $("#alarm-name-search").val();
        var raisedTimeFrom = $("#raised-from-search").val();
        var raisedTimeTo = $("#raised-to-search").val();

        var totalParams = '';

        if(isNaN(indexPage)){
            indexPage = '1';
        }
        if(isNaN(limitPageInput)){
            limitPageInput = '20';
        }
        totalParams = "/alarm/search?"
            + "indexPage="+indexPage+"&limit="+limitPageInput;

        if(serialNumber != ''){
            totalParams += "&device_id=" + serialNumber;
        }
        if(severity != ''){
            totalParams += "&severity=" + severity;
        }
        if(alarmType != ''){
            if(alarmType == 'REQUEST_FAIL') {alarmType = "Request failed";alarmName = '';}
            if(alarmType == 'CONFIGURATION_FAIL') {alarmType = "Configuration device failed";alarmName = '';}
            if(alarmType == 'UPDATE_FIRMWARE_FAIL') {alarmType = "Update firmware failed";alarmName = '';}
            if(alarmType == 'REBOOT_FAIL') {alarmType = "Reboot failed";alarmName = '';}
            if(alarmType == 'FACTORY_RESET_FAIL') {alarmType = "Factory reset failed";}
            totalParams += "&alarm_type_name=" + alarmType;
        }
//        else {alarmName = '';}
        if(status != ''){
            totalParams += "&status=" + status;
        }
        if(groupFilter != ''){
            totalParams += "&device_groups=" + groupFilter;
        }
        if(alarmName != ''){
            totalParams += "&alarm_name=" + alarmName;
        }

        if(raisedTimeFrom != '' && raisedTimeTo != ''){
            totalParams += "&raised_from=" + raisedTimeFrom + "&raised_to=" + raisedTimeTo;
        }

         window.location.href = totalParams;
    }


    $('#confirm-single-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });
    $('#confirm-single-remove-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });
    $('#confirm-clear-multi-alarm').click(function() {
        deleteIds = [];
        deleteStatus = [];
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            var name = $(this).attr('name');
            var status = $(this).attr('status');
            if ($(this).is(':checked')) {
                deleteIds.push(id);
                nameAndStatus = name.split('#');
                deleteParams.push(nameAndStatus[0]);
                deleteStatus.push(nameAndStatus[1]);
            }
        });
        url = '/clearAlarm';
        showDialogClearAlarm('clear', deleteIds, deleteIds.length + ' alarms');

    });

    $('#confirm-remove-multi-alarm').click(function() {
        deleteIds = [];
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            var name = $(this).attr('name');
            var status = $(this).attr('status');
            if ($(this).is(':checked')) {
                nameAndStatus = name.split('#');
                if(nameAndStatus[1] == 'Cleared'){
                    deleteIds.push(id);
                    deleteParams.push(nameAndStatus[0]);
                }
            }
        });

        if(deleteIds.length > 0){
            url = '/removeAlarm';
            showDialogClearAlarm('remove', deleteIds, deleteIds.length + ' alarms');
        } else {
            $('#remove_alert_danger').show();
        }

    });

    $('#alert_danger_btn').click(function() {
        $('#remove_alert_danger').hide();
    });

    $('.alarm-clear-single-opener').click(function() {
        deleteIds = [];
        deleteStatus = [];
        alarmClearId = $(this).attr('data-alarm');
        alarmClearName = $(this).attr('data-alarmName');
        deleteStatus.push($(this).attr('data-status'));
        url = '/clearAlarm';
        showDialogClearAlarm('clear', alarmClearId, alarmClearName);

    });
    $('.alarm-remove-single-opener').click(function() {
        deleteIds = [];
        alarmRemoveId = $(this).attr('data-alarm');
        alarmRemoveName = $(this).attr('data-alarmName');
        alarmRemoveStatus = $(this).attr('data-alarmStatus');
        if(alarmRemoveStatus == 'Cleared'){
            url = '/removeAlarm';
            showDialogClearAlarm('remove', alarmRemoveId, alarmRemoveName);
        }


    });

    function showDialogClearAlarm(clear, alarmClearId, alarmClearName){

        deleteIds.push(alarmClearId);
        $('.ui-dialog-title').html('Do you want to ' + clear + ' ' + alarmClearName +' ?');
        if(clear == 'clear'){$('#confirm-single-dialog').dialog('open');}
        else {$('#confirm-single-remove-dialog').dialog('open');}

    }

    $('#confirm-single-remove-dialog-later').click(function() {
        $('#confirm-single-remove-dialog').dialog('close');
    });
    $('#confirm-single-remove-dialog-now').click(function() {
        removeClearAction();
    });

    $('#confirm-single-dialog-later').click(function() {
        $('#confirm-single-dialog').dialog('close');
    });
    $('#confirm-single-dialog-now').click(function() {
        removeClearAction();
    });

    function removeClearAction(){
        $.ajax({
            type: 'POST',
            url: url,
            data: {
                deleteIds: deleteIds+"@",
                deleteStatus: deleteStatus+'@'
            },
            success: function(response) {
                if(response == 'successfully'){
                    deleteIds = [];
                    location.reload();
                }
            },
            async: true
        });
    }


    var paginator_data = $('#data-alarm-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input').val());
    var totalAlarmBeChecked = 0;

    var confirmRemoveMultiAlarm = $('#confirm-remove-multi-alarm');
    var confirmRemoveMultiAlarm_html = confirmRemoveMultiAlarm.html();
    var confirmClearMultiAlarm = $('#confirm-clear-multi-alarm');
    var confirmClearMultiAlarm_html = confirmClearMultiAlarm.html();
    confirmRemoveMultiAlarm.addClass("disabled");
//    confirmRemoveMultiAlarm.unbind( "click" );
    confirmClearMultiAlarm.addClass("disabled");
//    confirmClearMultiAlarm.unbind( "click" );

    $('#checkAll').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });

    $('.myCheckBox').change(function() {
        if ($(this).is(":checked")) {
            totalAlarmBeChecked = totalAlarmBeChecked + 1;
        } else {
            totalAlarmBeChecked = totalAlarmBeChecked - 1;
        }

        if (totalAlarmBeChecked > 0) {
            showGroupButton(true);
        } else {
            showGroupButton(false)
        }
    });

    $('.myCheckBox').each(function() {
        if ($(this).is(':checked')) {
            totalAlarmBeChecked++;
        } else {
            if(totalAlarmBeChecked != 0){totalAlarmBeChecked--;}
        }
    });

    if (totalAlarmBeChecked > 0) {
        showGroupButton(true);
    } else {
        showGroupButton(false)
    };

    function checkAll() {
        totalAlarmBeChecked =0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
           totalAlarmBeChecked++;
        });
        showGroupButton(true);
    }

    function unCheckAll() {
        totalAlarmBeChecked = 0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
        });
        showGroupButton(false);
    }

    function showGroupButton(enable) {
        if (enable) {
//            confirmRemoveMultiAlarm.bind( "click" );
            confirmRemoveMultiAlarm.removeClass("disabled");
            confirmRemoveMultiAlarm.html(confirmRemoveMultiAlarm_html + ' ('+totalAlarmBeChecked+')');
//
//            confirmClearMultiAlarm.bind( "click" );
            confirmClearMultiAlarm.removeClass("disabled");
            confirmClearMultiAlarm.html(confirmClearMultiAlarm_html + ' ('+totalAlarmBeChecked+')');
        } else {
            confirmRemoveMultiAlarm.html(confirmRemoveMultiAlarm_html);
            confirmRemoveMultiAlarm.addClass("disabled");
//            confirmRemoveMultiAlarm.unbind( "click" );
//
            confirmClearMultiAlarm.html(confirmClearMultiAlarm_html);
            confirmClearMultiAlarm.addClass("disabled");
//            confirmClearMultiAlarm.unbind( "click" );
        }
    }

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
            if(0 < goPageInput && goPageInput <= lastPagesVar){
                pagingDataModel(goPageInput, limitPageInput);
            } else {
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
            }
        }

    });
    $('.limit-page-input').change(function () {
        var limitPageInput = parseInt($('.limit-page-input').val());
        var indexPage = parseInt(paginator_data.attr('data-number'));
        pagingDataModel(1, limitPageInput);

    });

    function pagingDataModel(index, limit) {
        searchHard(index,limit);
    }

    function hihi(){
        $('#dt_alarms tr').each(function() {
        var severity = $(this).find(".severity").html();
        if(severity != null){
            if(severity == 'Critical'){
                $(this).css('background-color', '#ff0000');
            } else if (severity == 'Major'){
                $(this).css('background-color', '#ff9933');
            } else if (severity == 'Minor'){
                $(this).css('background-color', '#ffff00');
            } else if (severity == 'Warning'){
                $(this).css('background-color', '#0099ff');
            } else {
                $(this).css('background-color', '#009900');
            }
        }
     });
    }
    hihi();
});