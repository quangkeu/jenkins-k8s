$(function(){
    var paginator_data = $('#data-data-model-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input').val());
    var rowDelete = [];
    var rowDeleteName = [];

    var table = $('#DataTables_Table_0').DataTable({
        'columnDefs': [{
            'targets': 0,
            'searchable': false,
            'orderable': false,
            'width': '1%',
            'className': 'dt-body-center'
        },{
                                            "targets": [0],
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

    $('.close').click(function() {
        $(this).parent().hide();
    });

        $(".filter-edit-btn").click(function() {
            var id = $(this).attr('id');
            var name = $(this).attr('name');
            var type = $(this).attr('type');
            var using = "";
            $.ajax({
                type: 'POST',
                url: "/group-filter/"+id+"/checkJob",
                success: function(AcsResponse) {
                    var checkFilterAlert = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result != 200) // success
                        {
                            using = result;
                            checkFilterAlert = checkFilterAlert + name + ", ";
                            $('#filterName').val('');
                            $('#inputLabel').val('');
                            $("#manufacture").val('').trigger("change");
                            $("#modelName").val('').trigger("change");
                            $("#firmwareVersion").val('').trigger("change");
                             $("#groupId").prop('value', '');
                        }
                        else {
                            var arr = type.split("|");
                            $('#filterName').val(name);
                            $('#inputLabel').val(arr[3]);
                            $("#manufacture").val(arr[0]).trigger("change");
                            $("#modelName").val(arr[1]).trigger("change");
                            $("#firmwareVersion").val(arr[2]).trigger("change");
                            $("#groupId").prop('value', id);
                            $("#groupName").prop('value', name);
                        }
                    });
                    if (checkFilterAlert !== "") {
                        var alert_Exits = $('#alert_danger_input').attr('class');
                        checkFilterAlert += alert_Exits;
                        if(using.indexOf("404")  != -1)  checkFilterAlert += " Policy, "
                        if(using.indexOf("403") != -1)  checkFilterAlert += " Users, "
                        if(using.indexOf("402") != -1)  checkFilterAlert += " Alarm, "
                        if(using.indexOf("401") != -1)  checkFilterAlert += " Performance, "
                        checkFilterAlert = checkFilterAlert.substring(0,checkFilterAlert.length - 2);
                        $('#alert_danger span.text-semibold').text(checkFilterAlert);
                        $('#alert_danger').show();
                    }
                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });
        });

//    $('#DataTables_Table_0 tbody').on('click', 'b[type="edit"]', function(e) {
//
//        var _rowId = table.row("#DataTables_Table_0 tbody").index();
//        var id = table.cell(_rowId,0).data();
//        var filterName = table.cell(_rowId,1).data();
//        var manufacture = table.cell(_rowId,2).data();
//        var modelName = table.cell(_rowId,3).data();
//        var firmwareVersion = table.cell(_rowId,4).data();
//        var label = table.cell(_rowId,5).data();
//
//        $('#filterName').val(filterName);
//        $('#inputLabel').val(label);
//        $("#manufacture").val(manufacture).trigger("change");
//        $("#modelName").val(modelName).trigger("change");
//        $("#firmwareVersion").val(firmwareVersion).trigger("change");
//        $("#groupId").prop('value', id);
//        e.stopPropagation();
//    });

        $("#group-filter-close").click(function() {
                $('#filterName').val('');
                $('#inputLabel').val('');
                $("#manufacture").val('').trigger("change");
                $("#modelName").val('').trigger("change");
                $("#firmwareVersion").val('').trigger("change");
                 $("#groupId").prop('value', '');
                $("#groupName").prop('value', '');
        });


        $( "#filterName" ).focusout(function() {
                var filterName = $(this).val();
                var id = $("#groupId").val();
                var oldName  = $("#groupName").val();
                if(filterName != '' && oldName !== filterName) {
                $.ajax({
                type: 'POST',
                url: "/group-filter/"+filterName+"/checkFilter",
                success: function(AcsResponse) {
                    var successAlert = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result == 200) // success
                        {
                            successAlert = successAlert + filterName + ", ";
                        }
                    });
                    if (successAlert !== "") {
                        if(id == '') $("#filterName").val('');
                        else $("#filterName").val(oldName);
                        var alert_success = $('#alert_existed_input').attr('class');
                        successAlert += alert_success;
                        $('#alert-existed span.text-semibold').text(successAlert);
                        $('#alert-existed').show();
                    }
                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });
                } else {
                     $("#filterName").val(oldName);
                }
         });
    // ADVANCE SEARCH
    $('.select2').select2({
        allowClear: true
    });

    $("#manufacture").change(function(){
        var manufacturerDataVar = $(this).val();
        var resultJSON = $("#addNewFileParam").val();
        var result = JSON.parse(resultJSON);
        $('#firmwareVersion').html('').select2({data: [{id: '', text: ''}]});
        $('#modelName').html('').select2({data: [{id: '', text: ''}]});
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
        $("#modelName").html('').select2({data: dataArray});
    });

    $("#modelName").change(function(){
        var manufacturerDataVar = $("#manufacture").val();
        var modelName = $(this).val();
        var resultJSON = $("#addNewFileParam").val();
        var result = JSON.parse(resultJSON);
        $('#firmwareVersion').html('').select2({data: [{id: '', text: ''}]});
        var dataArray = [];
        var detail = [];
        dataArray.push({id: '', text: ''});
        $.each(result, function(k, v) {
            if(manufacturerDataVar == k){
                $.each(v, function( index, value ) {
                      $.each(value, function( k, v ) {
                            if(modelName == k) {

                                $.each(v, function(index_firmware, firmware) {
                                    dataArray.push({id: firmware, text: firmware});
                                });
                            }
                      });
                });
            }
        });
        $("#firmwareVersion").html('').select2({data: dataArray});
    });

    $('#confirm-remove-dialog-close').click(function() {
        $('#confirm-remove-dialog').dialog('close');
    });

    $('.filter-remove-btn').click(function() {
        rowDelete = [];
        rowDeleteName = [];
        var id = $(this).attr('id');
        var name = $(this).attr('name');
        rowDelete.push(id);
        rowDeleteName[''+id] = name;
        var title_dialog = $("#remove-dialog-input").attr('class');
        $('#confirm-remove-dialog').dialog('option', 'title', title_dialog + ' ' + name + '?');
        $('#confirm-remove-dialog').dialog('open');
    });

        $("#confirm-remove-now-dialog").click(function() {
            $.ajax({
                type: 'POST',
                url: "/group-filter/"+rowDelete+"/checkJob",
                success: function(AcsResponse) {
                    var checkFilterAlert = "";
                    var using = "";
                    $.each(AcsResponse.mapResult, function(id, result) {

                        if (result != 200) // success
                        {
                            using = result;
                            checkFilterAlert = checkFilterAlert + rowDeleteName[''+id] + ", ";
                        }
                        else {
            $.ajax({
                type: 'POST',
                url: "/group-filter/remove/"+rowDelete,
                success: function(AcsResponse) {
                    var successAlert = "";
                    var failedAlert = "";
                    $.each(AcsResponse.mapResult, function(id, result) {
                        if (result == 200) // success
                        {
                            successAlert = successAlert + rowDeleteName[''+id] + ", ";
                            $('.'+id).remove();
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
                        }
                    });
                    if (checkFilterAlert !== "") {
                        var alert_Exits = $('#alert_danger_input').attr('class');
                        checkFilterAlert += alert_Exits;
                        console.log(using);
                        if(using.indexOf("404") != -1)  checkFilterAlert += " Policy, "
                        if(using.indexOf("403") != -1)  checkFilterAlert += " Users, "
                        if(using.indexOf("402") != -1)  checkFilterAlert += " Alarm, "
                        if(using.indexOf("401") != -1)  checkFilterAlert += " Performance, "
                        checkFilterAlert = checkFilterAlert.substring(0,checkFilterAlert.length - 2);
                        $('#alert_danger span.text-semibold').text(checkFilterAlert);
                        $('#alert_danger').show();
                    }
                },
                error: function(data) {
                    console.log('Error ' + JSON.stringify(data));
                },
                async: true
            });
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
            totalParam = "/group-filter?indexPage=" + index + "&limit=" + limit;
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

});
