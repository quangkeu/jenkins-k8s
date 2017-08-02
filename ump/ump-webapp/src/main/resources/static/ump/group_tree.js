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

    var table = $('#DataTables_Table_0').DataTable({
        'columnDefs': [{
            'targets': 0,
            'searchable': false,
            'orderable': false,
            'width': '1%',
            'className': 'dt-body-center'
        },{
                          "targets": [3],
                          "visible": false,
                      },{
                                                 "targets": [4],
                                                 "visible": false,
                                             }],
        'order': [
            [1, 'asc']
        ],
        "sort": false,
        "paging": false,
        "info": false,
        "filter": false,
        "scrollY": "300px",
        "scrollCollapse": true
    });


    // for other page back to current page
    $('.myCheckBox').each(function() {
            if ($(this).is(':checked')) {
                    totalAreaBeChecked += 1;
            }
     });
          if (totalAreaBeChecked > 0) {
                showGroupButton(true);
            } else {
                showGroupButton(false)
            }

    $('#create-group-tree-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: 500
    });

        $('#edit-group-tree-dialog').dialog({
            autoOpen: false,
            modal: true,
            width: 500
        });

    $('#create-group-tree-dialog-opener').click(function() {
        $('#create-group-tree-dialog').dialog('open');
    });

         $('#create_group-tree-dialog-close').click(function() {
            $('#create-group-tree-dialog').dialog('close');
            $('#form_create_group_tree')[0].reset();
         });

         $('#edit_group-tree-dialog-close').click(function() {
            $('#edit-group-tree-dialog').dialog('close');
            $('#form_edit_group_tree')[0].reset();
         });

    $('#confirm-remove-dialog-close').click(function() {
        $('#confirm-remove-dialog').dialog('close');
    });
    $('#DataTables_Table_0 tbody').on('click', 'td.cannotChange', function(e) {
        var _rowId = table.row(this).index();
        var ip_range = table.cell(_rowId,1).data();
//         var parent = table.cell(_rowId,2).data();
          var pid = table.cell(_rowId,3).data();
          var id = table.cell(_rowId,4).data();
          var name = table.cell(_rowId,5).data();
          name = name.replace(/\-/g, '');
        $('#group_id').val(id);
        $('#group_name').val(name);
        $('#ip_range').val(ip_range);
        $('#group_parent').val(pid);
        $('#edit-group-tree-dialog').dialog('open');
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
        if (totalAreaBeChecked > 0) {
            showGroupButton(true);
        } else {
            showGroupButton(false)
        }
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
            console.log('No Area to remove');
        } else {
            $.ajax({
                type: 'POST',
                url: "/removeArea",
                data: JSON.stringify(areaNeedRemove),
                success: function(AcsResponse) {
                    var successAlert = "";
                    var inprogresAlert = "";
                    var failedAlert = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result == 200) // success
                        {
                            successAlert = successAlert + nameAreaNeedRemove[''+id] + ", ";
                            $('.'+id).remove();
                            $("#parentId option[value="+id+"]").remove();
                        } else if (result == 202) {
                            inprogresAlert = inprogresAlert + nameAreaNeedRemove[''+id] + ", ";
                        } // in progress
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
                    if (inprogresAlert !== "") {
                        var alert_danger = $('#alert_danger_input').attr('class');
                        inprogresAlert += alert_danger;
                        $('#alert_danger span.text-semibold').text(inprogresAlert);
                        $('#alert_danger').show();
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
        } else {
            listAreaBeRemove = totalAreaBeChecked + " areas";
        }
        listAreaBeRemove = listAreaBeRemove.substring(0, listAreaBeRemove.length - 2);
        listAreaBeRemove = listAreaBeRemove.replace(/\-/g, '');
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
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
            totalAreaBeChecked = totalAreaInPage;
        });
        showGroupButton(true);
    }


    /**
        function to unCheckAll device in page
    */
    function unCheckAll() {
        $('.myCheckBox').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
            totalAreaBeChecked = 0;
        });
        showGroupButton(false);
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

    $("#form_edit_group_tree").validate(ump.validator);
});