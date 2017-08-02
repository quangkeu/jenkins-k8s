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
    // get all device saved in p tag
    var totalAreaInPage = $.trim($('#totalAreaInPage').text());
    // variable to check number device being checked
    var totalAreaBeChecked = 0;
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
                                                 },{
                                                                                              "targets": [2],
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


    // for other page back to current page
    $('.myCheckBox').each(function() {
            if ($(this).is(':checked')) {
                    totalAreaBeChecked += 1;
            }
     });

     showRemoveButton(totalAreaBeChecked);

    $('#create-device-group-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: 800
    });

        $('#edit-device-group-dialog').dialog({
            autoOpen: false,
            modal: true,
            width: 800
        });

    $('#create-device-group-dialog-opener').click(function() {
        $('#create-device-group-dialog').dialog('open');
    });

         $('#create-device-group-dialog-close').click(function() {
            $('#create-device-group-dialog').dialog('close');
            $('#form_create_device_group')[0].reset();
         });

         $('#edit-device-group-dialog-close').click(function() {
            $('#edit-device-group-dialog').dialog('close');
            $('#form_edit_device_group')[0].reset();
         });

    $('#confirm-remove-dialog-close').click(function() {
        $('#confirm-remove-dialog').dialog('close');
    });
    $('#DataTables_Table_0 tbody').on('click', 'td.cannotChange', function(e) {
        $("#dataTableFilter").empty();
        e.stopPropagation();
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

    /**
        function event for remove Button
    */
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
                url: "/devices/device_logs/remove",
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
    /**
    function event for close dialogue
    */
    $('.close').click(function() {
        $(this).parent().hide();
    });

    $('#confirm-remove-dialog').dialog({
        autoOpen: false,
        modal: true,
        width:'50%',
        height: 'auto'
    });

    $('#confirm-remove-dialog-opener').click(function() {
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
            listAreaBeRemove = totalAreaBeChecked + " groups";
        }

//        var title_dialog = $( "#confirm-remove-dialog" ).dialog( "option", "title" );
        var title_dialog = $("#remove-dialog-input").attr('class');
        $('#confirm-remove-dialog').dialog('option', 'title', title_dialog + ' ' + listAreaBeRemove + '?');
//        $('.ui-dialog-title').html('Are you sure you want to remove ' + listAreaBeRemove + '?');
        $('#confirm-remove-dialog').dialog('open');
    });

    /**
        function to checkAll device in page
    */
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


    function showGroupButton(enable) {
        if (enable == true) {
            $('#confirm-remove-dialog-opener').show();
            $('#team').show();
        } else {
            $('#confirm-remove-dialog-opener').hide();
            $('#team').hide();
        }

    }

    $("#form_edit_device_group").validate(ump.validator);

    $('input[type=radio][name=show]').change(function() {
        if (this.value == 'showAll') {
            var deviceID = $("#searchByTime").attr('name');
            window.location.href = "/devices/device_logs/"+deviceID;
//            $("#fromDate").prop('disabled', true);
//            $("#toDate").prop('disabled', true);
//            $('#searchByTime').hide();
        }
        else if (this.value == 'showByDate') {
            $("#fromDate").prop('disabled', false);
            $("#toDate").prop('disabled', false);
//            $('#searchByTime').show();
        }
    });

        $("#searchByTime").click(function() {
           var fromDate = $('#fromDate').val();
           var toDate = $('#toDate').val();
           var deviceID = $(this).attr('name');
           if(fromDate != '' || toDate != '') {
                var totalParam = '';
                totalParam = "/devices/device_logs/"+deviceID+"?fromDate="+fromDate+'&toDate='+toDate;
                window.location.href = totalParam;
            }
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
            var deviceId = $("#remove-dialog-input").attr('value');
            var totalParam = '';
            totalParam = "/devices/device_logs/"+ deviceId +"?indexPage=" + index + "&limit=" + limit;
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

        var dataTemplateIdArrChecked = [];
        var remove_data_template_opener = $('#confirm-remove-dialog-opener');
        var remove_data_template_opener_text = remove_data_template_opener.html();
        function showRemoveButton(total) {
            if(total > 0) {
                remove_data_template_opener.show();
                remove_data_template_opener.html(remove_data_template_opener_text + ' ('+total+')')
            } else {
                $('#confirm-remove-dialog-opener').hide();
            }
        }

});