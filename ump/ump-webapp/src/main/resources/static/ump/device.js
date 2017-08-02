function updateDataTableSelectAllCtrl(table) {
    var $table = table.table().node();
    var $chkbox_all = $('tbody input[type="checkbox"]', $table);
    var $chkbox_checked = $('tbody input[type="checkbox"]:checked', $table);
    var chkbox_select_all = $('thead input[name="checkAll"]', $table).get(0);

    // If none of the checkboxes are checked
    if ($chkbox_checked.length === 0) {
        chkbox_select_all.checked = false;
        if ('indeterminate' in chkbox_select_all) {
            $("#checkAll").prop("indeterminate", false);
        }

        // If all of the checkboxes are checked
    } else if ($chkbox_checked.length === $chkbox_all.length) {
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
    // global variable for sorting field
    var _sortingField = $('#sortField').text();
    // global variable for sorting type
    var _sortingType = $('#sortType').text();
    // get all device saved in p tag
    var totalDeviceInPage = $.trim($('#totalDeviceInPage').text());
    // variable to check number device being checked
    var totalDeviceBeChecked = 0;
    var rebootNowOrLast = false;
    var paginator_data = $('#data-device-paginator');
    $(document).on('click', 'th[name=sortColumn]', function() {
        var sortType = $(this).attr('class');
        var keySort = $(this).children("span").attr('id');
        var indexPage = parseInt(paginator_data.attr('data-number'));
        var limitPageInput = parseInt($('.limit-page-input').val());
        var tmp;
        var totalParam;
        var firmwareVersionDataVar = $('#firmwareVersionData').val();
        var modelNameVar = $("#modelNameData").val();
        var manufacturerDataVar = $("#manufacturerData").val();
        var textSearchVar = $("#textSearch").val();
        var textSearchLabelVar = $("#inputLabel").val();

        if (sortType === 'sorting_asc') {
            tmp = "sorting_desc";
        } else if (sortType === 'sorting_desc') {
            tmp = "sorting_asc";
        } else if (sortType === 'sorting sorting_disabled') {
            tmp = "sorting_asc";
        }
        totalParam = "/search?indexPage=" + indexPage + "&limit="+ limitPageInput
                + "&sortField="+keySort+"&sortType="+tmp;

            totalParam += "&Manufacturer=" + manufacturerDataVar

            totalParam += "&ModelName=" + modelNameVar

            totalParam += "&FirmwareVersion=" + firmwareVersionDataVar


        if(textSearchVar != ''){
            totalParam += "&searchAll=" + textSearchVar
        }

            totalParam += "&searchLabel=" + textSearchLabelVar;


        window.location.href = totalParam
    });
    var table = $('#DataTables_Table_0').DataTable({
        'columnDefs': [{
            'targets': 0,
            'searchable': false,
            'orderable': false,
            'width': '1%',
            'className': 'dt-body-center'
        }],
        'order': [
            [1, 'asc']
        ],
        "colReorder": {
           order: [0,2,1,6,4,5,3,7,8,9]
        },
        "responsive": true,
        "sort": false,
        "paging": false,
        "info": false,
        "filter": false,
        "scrollY": "305px",
        "scrollCollapse": true
    });

    // hiden ID column
    var column = table.column(1);
    // Toggle the visibility
    column.visible(!column.visible());

    var columnLastInform = table.column(8);
    // Toggle the visibility
    columnLastInform.visible(!columnLastInform.visible());

    // for other page back to current page
    $('.myCheckBox').each(function() {
        if ($(this).is(':checked')) {
            totalDeviceBeChecked += 1;
        }
    });
    if (totalDeviceBeChecked > 0) {
        showGroupButton(true);
    } else {
        showGroupButton(false)
    }
    $('#DataTables_Table_0 tbody').on('click', 'td[name=cannotChange]', function(e) {
        var _rowId = table.row(this).index();
        var idDevice = table.cell(_rowId, 1).data();
        e.stopPropagation();
        // go to  device detail page
        window.location.href = "/devices/" + idDevice;
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

    $('thead input[name="checkAll"]', table.table().container()).on('click', function(e) {
        if (this.checked) {
            $('#DataTables_Table_0 tbody input[type="checkbox"]:not(:checked)').trigger('click');
        } else {
            $('#DataTables_Table_0 tbody input[type="checkbox"]:checked').trigger('click');
        }
        // Prevent click event from propagating to parent
        e.stopPropagation();
    });

    // Handle table draw event
    table.on('draw', function() {
        // Update state of "Select all" control
        updateDataTableSelectAllCtrl(table);
    });

    $('#selectPage').on('change', function() {
        if (this.value !== 'default') {
            window.location.href = this.value
        }
    });

    /**
    function to checkall or uncheck all device when checkAll selectBox be checked
    */
    $('#checkAll').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }

    });

    /**
    function to checked or unchecked checkAll selectbox
    */

    $('.myCheckBox').change(function() {
        if ($(this).is(":checked")) {
            totalDeviceBeChecked = totalDeviceBeChecked + 1;
        } else {
            totalDeviceBeChecked = totalDeviceBeChecked - 1;
        }
        if (totalDeviceInPage === totalDeviceBeChecked) {
            $('#checkAll').prop('checked', true);
        } else {
            $('#checkAll').prop('checked', false);
        }
        // if non device be checkek -> disable button reboot and reset factory
        if (totalDeviceBeChecked > 0) {
            showGroupButton(true);
        } else {
            showGroupButton(false)
        }
    });

    /**
        function event for reboot Button
    */
    $('#confirm-reboot-now-dialog').click(function() {
        var hasDevice = false;
        var deviceNeedReboot = [];
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            if ($(this).is(':checked')) {
                deviceNeedReboot.push(id);
                hasDevice = true;
            }
        });

        if (hasDevice === false) {
            alert("No device being reboot");
        } else {
            buttonLoading("confirm-reboot-dialog-opener","start");
            $.ajax({
                type: 'POST',
                url: "/rebootMultiDevice?now=" + rebootNowOrLast,
                data: JSON.stringify(deviceNeedReboot),
                success: function(AcsResponse) {
                    buttonLoading("confirm-reboot-dialog-opener","end");
                    var successAlert = "";
                    var inprogresAlert = "";
                    var failedAlert = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result == 200) // success
                        {
                            successAlert = successAlert + id + ", ";
                        } else if (result === 202) {
                            inprogresAlert = inprogresAlert + id + ", ";
                        } // in progress
                        else { // failed
                            failedAlert = failedAlert + id + ", ";
                        }
                    });
                    if (successAlert !== "") {
                        successAlert += "reboot success!";
                        $('#alert_success span.text-semibold').text(successAlert);
                        $('#alert_success').show();
                    }
                    if (inprogresAlert !== "") {
                        inprogresAlert += "Reboot in queue!";
                        $('#alert_danger span.text-semibold').text(inprogresAlert);
                        $('#alert_danger').show();
                    }
                    if (failedAlert !== "") {
                        failedAlert += "Reboot Failed!";
                        $('#alert_primary span.text-semibold').text(failedAlert);
                        $('#alert_primary').show();
                    }
                },
                async: true
            });
        }
        $('#confirm-reboot-dialog').dialog('close');
    });
    /**
    function event for close dialogue
    */
    $('.close').click(function() {
        $(this).parent().hide();
    });

    $('#confirm-reset-dialog').dialog({
        autoOpen: false,
        modal: true,
        height: 'auto',
        width: '50%'
    });
    $('#confirm-reboot-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });

    $('#confirm-reset-dialog-opener').click(function() {
        var listDeviceBeReboot = "";
        if (totalDeviceBeChecked <= 5 && totalDeviceBeChecked > 1) {
            $('.myCheckBox').each(function() {
                var id = $(this).attr('id');
                if ($(this).is(':checked')) {
                    listDeviceBeReboot = listDeviceBeReboot + ', <br> ' + id;
                }
            });
        var first = listDeviceBeReboot.indexOf(",");
        listDeviceBeReboot = listDeviceBeReboot.substring(0, first) + '' + listDeviceBeReboot.substring(first + 1)
        var pos = listDeviceBeReboot.lastIndexOf(",");
        listDeviceBeReboot = listDeviceBeReboot.substring(0, pos) + ' and ' + listDeviceBeReboot.substring(pos + 1)
        } else if (totalDeviceBeChecked === 1) {
            $('.myCheckBox').each(function() {
                var id = $(this).attr('id');
                if ($(this).is(':checked')) {
                    listDeviceBeReboot = id;
                }
            });
        } else {
            listDeviceBeReboot = totalDeviceBeChecked + " devices";
        }
        $('.ui-dialog-title').html('Are you sure you want to reset: ' + listDeviceBeReboot + '?');
        $('#confirm-reset-dialog').dialog('open');


    });

    $('#confirm-reset-dialog-close').click(function() {
        $('#confirm-reset-dialog').dialog('close');
    });


    $('#confirm-reboot-dialog-opener').click(function() {
        var listDeviceBeReboot = "";

        if (totalDeviceBeChecked <= 5 && totalDeviceBeChecked > 1) {
            $('.myCheckBox').each(function() {
                var id = $(this).attr('id');
                if ($(this).is(':checked')) {
                    listDeviceBeReboot = listDeviceBeReboot + ', <br>' + id;
                }
            });
            var first = listDeviceBeReboot.indexOf(",");
            listDeviceBeReboot = listDeviceBeReboot.substring(0, first) + '' + listDeviceBeReboot.substring(first + 1)
            var pos = listDeviceBeReboot.lastIndexOf(",");
            listDeviceBeReboot = listDeviceBeReboot.substring(0, pos) + ' and ' + listDeviceBeReboot.substring(pos + 1)
        } else if (totalDeviceBeChecked === 1) {
            $('.myCheckBox').each(function() {
                var id = $(this).attr('id');
                if ($(this).is(':checked')) {
                    listDeviceBeReboot = id;
                }
            });
        } else {
            listDeviceBeReboot = totalDeviceBeChecked + " devices";
        }
        $('.ui-dialog-title').html('Are you sure you want to reboot: ' + listDeviceBeReboot + '?');
        $('#confirm-reboot-dialog').dialog('open');
    });

    $('#confirm-reboot-dialog-close').click(function() {
        $('#confirm-reboot-dialog').dialog('close');
    });



    $("select#team").on("change", function() {
        var selectedD = $(this).val();
        if (selectedD === "1") {
            rebootNowOrLast = false;
            //Next contact
        }
        if (selectedD === "2") {
            //Now
            rebootNowOrLast = true;
        }
    });


    /**
    function event for resetFactory button
    */
    $('#confirm-reset-now-dialog').click(function() {
        var hasDevice = false;
        var deviceNeedReset = [];
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            if ($(this).is(':checked')) {
                deviceNeedReset.push(id);
                hasDevice = true;
            }
        });
        if (hasDevice === false) {
            alert("No device being reset");
        } else {
            buttonLoading("confirm-reset-now-dialog","start");
            $.ajax({
                type: 'POST',
                url: "/resetMultiDevice?now=" + rebootNowOrLast,
                data: JSON.stringify(deviceNeedReset),
                success: function(AcsResponse) {
                    buttonLoading("confirm-reset-now-dialog","end");
                    var successAlert = "";
                    var inprogresAlert = "";
                    var failedAlert = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result === 200) // success
                        {
                            successAlert = successAlert + id + ", ";
                        } else if (result === 202) {
                            inprogresAlert = inprogresAlert + id + ", ";
                        } // in progress
                        else { // failed
                            failedAlert = failedAlert + id + ", ";
                        }
                    });
                    if (successAlert !== "") {
                        successAlert += "reset success!";
                        $('#alert_success span.text-semibold').text(successAlert);
                        $('#alert_success').show();
                    }
                    if (inprogresAlert !== "") {
                        inprogresAlert += "Reset in queue!";
                        $('#alert_danger span.text-semibold').text(inprogresAlert);
                        $('#alert_danger').show();
                    }
                    if (failedAlert !== "") {
                        failedAlert += "Reset Failed!";
                        $('#alert_primary span.text-semibold').text(failedAlert);
                        $('#alert_primary').show();
                    }
                },
                error: function(data, status) {

                },
                async: true
            });
        }
        $('#confirm-reset-dialog').dialog('close');


    });

    /**
        function to checkAll device in page
    */
    function checkAll() {
        totalDeviceBeChecked =0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
           totalDeviceBeChecked++;
        });
        showGroupButton(true);
    }


    /**
        function to unCheckAll device in page
    */
    function unCheckAll() {
        totalDeviceBeChecked = 0;
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
        });
        showGroupButton(false);
    }


    function showGroupButton(enable) {
        if (enable === true) {
            $('#confirm-delete-device').removeClass("disabled");
        } else {
            $('#confirm-delete-device').addClass("disabled");
        }

    }

    $('.select').select2();
    function searchDevices(manufacturer, modelName,firmwareVersion) {
//            var sortType = $('th[name=sortColumn]').attr('class');
//            var keySort = $('th[name=sortColumn]').children("span").attr('id');
            var limitPageInput = parseInt($('.limit-page-input').val());
            if(isNaN(limitPageInput)){
                limitPageInput = '20';
//                sortType = 'sorting_asc';
//                keySort = '_deviceId._Manufacturer';
            }
//            var tmp;
            var totalParam = '';
//                if (sortType === 'sorting_asc') {
//                    tmp = "sorting_desc";
//                } else if (sortType === 'sorting_desc') {
//                    tmp = "sorting_asc";
//                } else if (sortType === 'sorting sorting_disabled') {
//                    tmp = "sorting_asc";
//                }
            var keySort = getParameterByName('sortField');
            var sortType = getParameterByName('sortType');

            totalParam = "/search?"
                +"indexPage=1&limit="+ limitPageInput;
            if(keySort != null){
                totalParam += "&sortField=" + keySort + "&sortType=" + sortType;
            }

                totalParam += "&Manufacturer=" + manufacturer;

                totalParam += "&ModelName=" + modelName;

                totalParam += "&FirmwareVersion=" + firmwareVersion;

                totalParam += "&searchLabel=" + $("#inputLabel").val();

            window.location.href = totalParam;
     }
    $("#manufacturerData").change(function(){
        var manufacturerDataVar = $(this).val();
        searchDevices(manufacturerDataVar,"","");
    });
    $("#modelNameData").change(function(){
        var modelNameVar = $(this).val();
        var manufacturerDataVar = $("#manufacturerData").val();
        searchDevices(manufacturerDataVar,modelNameVar,"");
    });
    $("#firmwareVersionData").change(function(){
        var firmwareVersionDataVar = $(this).val();
        var modelNameVar = $("#modelNameData").val();
        var manufacturerDataVar = $("#manufacturerData").val();
        searchDevices(manufacturerDataVar,modelNameVar,firmwareVersionDataVar);
    });

    $('#searchBtn').click(function() {
        var textSearchVar = $("#textSearch").val();
        var limitPageInput = parseInt($('.limit-page-input').val());
        var tmp;
        if(isNaN(limitPageInput)){
            limitPageInput = '20';
        }
        var totalParam = '';

        totalParam = "/search?"
            + "indexPage=1&limit=" + limitPageInput

        var keySort = getParameterByName('sortField');
        var sortType = getParameterByName('sortType');

        totalParam = "/search?"
            +"indexPage=1&limit="+ limitPageInput;
        if(keySort != null){
            totalParam += "&sortField=" + keySort + "&sortType=" + sortType;
        }

        totalParam += "&searchAll=" + textSearchVar;

        window.location.href = totalParam;
    });

    $('#confirm-delete-device').click(function() {
        $('#confirm-delete-dialog').dialog('open');
    });
    $('#confirm-delete-device-dialog-temporarily').click(function() {
        deleteObj("temporarily");
    });
    $('#confirm-delete-device-dialog-permanently').click(function() {
        deleteObj("permanently");
    });

    function deleteObj(mode){
        var hasDevice = false;
        var deleteIds = [];
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            if ($(this).is(':checked')) {
                deleteIds.push(id);
                hasDevice = true;
            }
        });
        $.ajax({
            type: 'POST',
            url: "/deleteDevices",
            data: {
                mode : mode,
                deleteIds: deleteIds+"@"
            },
            success: function(response) {
                location.reload();
            },
            async: true
        });
    }

    $(".row_deviceList").click(function() {
        window.location = $(this).data("href");
    });

    $("#inputLabel").keyup(function(event){
        if(event.keyCode == 13){
            var inputText = $(this).val();
            var indexPage = $('#indexPage').text();
            var tmp;
            var totalParam = '';
            var limitPageInput = parseInt($('.limit-page-input').val());
            if(isNaN(limitPageInput)){
                limitPageInput = '20';
            }
            var indexPage = parseInt(paginator_data.attr('data-number'));
            totalParam = "/search?"
                + "indexPage=1&limit="+limitPageInput;

            var keySort = getParameterByName('sortField');
            var sortType = getParameterByName('sortType');

            if(keySort != null){
                totalParam += "&sortField=" + keySort + "&sortType=" + sortType;
            }

            totalParam += "&Manufacturer=" +  $("#manufacturerData").val();
            totalParam += "&ModelName=" + $("#modelNameData").val();
            totalParam += "&FirmwareVersion=" +  $("#firmwareVersionData").val();
            totalParam += "&searchLabel=" + inputText;

             window.location.href = totalParam;

        }
    });

    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    if(isNaN(lastPagesVar)){lastPagesVar = '1';}
    if(isNaN(startPageVar)){startPageVar = '1';}
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
        pagingDataModel('1', limitPageInput);

    });
    function pagingDataModel(index, limit) {
            var _manufacturer = $("#manufacturerData").val();
            var _modelName = $("#modelNameData").val();
            var _firmwareVersion = $("#firmwareVersionData").val();
            var inputLabel = $('#inputLabel').text();
            var totalParam = '';

            var keySort = getParameterByName('sortField');
            var sortType = getParameterByName('sortType');

            totalParam = "/search?indexPage=" + index + "&limit=" + limit;
            if(keySort != null){
                totalParam += "&sortField=" + keySort + "&sortType=" + sortType;
            }

                totalParam += "&Manufacturer=" + _manufacturer;

                totalParam += "&ModelName=" + _modelName;

                totalParam += "&FirmwareVersion=" + _firmwareVersion;

                totalParam += "&searchLabel=" + inputLabel;


            textSearchVar = $("#textSearch").val();
            if(textSearchVar != ''){
                totalParam += "&searchAll=" + textSearchVar
            }


            window.location.href = totalParam;
     }

     function getParameterByName(name) {
         url = window.location.href;
         name = name.replace(/[\[\]]/g, "\\$&");
         var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
             results = regex.exec(url);
         if (!results) return null;
         if (!results[2]) return '';
         return decodeURIComponent(results[2].replace(/\+/g, " "));
     }
});