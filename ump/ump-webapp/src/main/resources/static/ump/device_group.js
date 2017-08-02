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
                                   "targets": [4],
                                   "visible": false,
                               },{
                                                          "targets": [5],
                                                          "visible": false,
                                                      },{
                                                                                                                 "targets": [6],
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
        var _rowId = table.row(this).index();
         var listFilter = table.cell(_rowId,6).data();
          $('#group_id').attr('name', table.cell(_rowId,1).data());
        $('#group_name').val(table.cell(_rowId,2).data());
        $('#group_oui').val(table.cell(_rowId,4).data());
        $('#group_product').val(table.cell(_rowId,5).data());
//    if(listFilter.indexOf("|") != -1) {
    var arr = listFilter.split("|");
    var html_filter = '<tr><td><INPUT type="checkbox" name="chk"/></td><td><select id="operator_i" name="operator_i"><option value="OR">OR</option><option value="AND">AND</option></select></td>'
                          +'<td><select id="columnName_i" name="columnName_i"><option value="Last inform">Last inform</option><option value="Serial Number">Serial Number</option><option value="Manufacturer">Manufacturer</option>'+
                          '<option value="Firmware version">Firmware version</option><option value="IP">IP</option></select></td><td><select id="compare_i" name="compare_i"><option value="=">=</option><option value="#">#</option>'+
                          '<option value="<"><</option><option value="<="><=</option><option value=">">></option><option value=">=">>=</option></select></td><td><input id="value_i" name="value_i" type="text" class="form-control"></td></tr>';
    for(i=0; i<arr.length; i++){
        var next_filter = arr[i];
        var detail = next_filter.split(",");
        var html_filter_replace = html_filter.replace(/\_i/g, '_'+i);
        $("#dataTableFilter").append(html_filter_replace);
       //set gia tri tu array
        var cp = detail[2];
        if(cp == '&lt;') cp = '<';
        if(cp == '&lt;=') cp = '<=';
        if(cp == '&gt;') cp = '>';
        if(cp == '&gt;=') cp = '>=';
        $('#operator_'+i).val(detail[0]);
        $('#columnName_'+i).val(detail[1]);
        $('#compare_'+i).val(cp);
        $('#value_'+i).val(detail[3]);
        }
//    }

        $('#edit-device-group-dialog').dialog('open');
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
            console.log('No Group to remove');
        } else {
            $.ajax({
                type: 'POST',
                url: "/device-group/remove",
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
        } else {
            listAreaBeRemove = totalAreaBeChecked + " groups";
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

    $("#form_edit_device_group").validate(ump.validator);

		function addRow(table) {
			var table = document.getElementById(table);

			var rowCount = table.rows.length;
			var row = table.insertRow(rowCount);

			var colCount = table.rows[0].cells.length;

			for(var i=0; i<colCount; i++) {

				var newcell	= row.insertCell(i);

				newcell.innerHTML = table.rows[0].cells[i].innerHTML;
				//alert(newcell.childNodes);
				switch(newcell.childNodes[0].type) {
					case "text":
							newcell.childNodes[0].value = "";
							break;
					case "checkbox":
							newcell.childNodes[0].checked = false;
							break;
					case "select-one":
							newcell.childNodes[0].selectedIndex = 0;
							break;
				}
			}
		}

		function addRowEdit(table) {
			var table = document.getElementById(table);
			var rowCount = table.rows.length;
			    var html_filter = '<tr><td><INPUT type="checkbox" name="chk"/></td><td><select id="operator_i" name="operator_i"><option value="OR">OR</option><option value="AND">AND</option></select></td>'
                                      +'<td><select id="columnName_i" name="columnName_i"><option value="Last inform">Last inform</option><option value="Serial Number">Serial Number</option><option value="Manufacturer">Manufacturer</option>'+
                                      '<option value="Firmware version">Firmware version</option><option value="IP">IP</option></select></td><td><select id="compare_i" name="compare_i"><option value="=">=</option><option value="#">#</option>'+
                                      '<option value="<"><</option><option value="<="><=</option><option value=">">></option><option value=">=">>=</option></select></td><td><input id="value_i" name="value_i" type="text" class="form-control"></td></tr>';
                    var html_filter_replace = html_filter.replace(/\_i/g, '_'+(rowCount));
                    $("#dataTableFilter").append(html_filter_replace);

		}

		function deleteRow(table) {
			try {
			var table = document.getElementById(table);
			var rowCount = table.rows.length;
			for(var i=0; i<rowCount; i++) {
				var row = table.rows[i];
				var chkbox = row.cells[0].childNodes[0];
				if(null != chkbox && true == chkbox.checked) {
					if(rowCount <= 1) {
						alert("Cannot delete all the rows.");
						break;
					}
					table.deleteRow(i);
					rowCount--;
					i--;
				}
			}
			}catch(e) {
				console.log('deleteRow  ' + e);
			}
		}


    $('#add_filter').on('click', function () {
        addRow("dataTable");
    });

        $('#add_edit_filter').on('click', function () {
            addRowEdit("dataTableFilter");
        });

    $('#remove_filter').on('click', function () {
               deleteRow("dataTable");
           });

               $('#remove_edit_filter').on('click', function () {
                   deleteRow("dataTableFilter");
               });

});