$(document).ready(function() {

    var totalDeviceBeChecked = 0;
    var deleteIds = [];
    var deleteNamesDisplay = [];


    var paginator_data = $('#data-voip-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input').val());

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
        if(index != null && index != ''
            && limit != null && limit != ''){
                $.ajax({
                    type: "GET",
                    url: "/voip-view-detail",
                    data: {
                        indexPage: index,
                        limit: limit
                    },
                    success: function(response) {
                        $(".edit-list-parameters").empty();
                        data1 = JSON.parse(JSON.stringify(response));
                        if(data1 != null){
                            console.log(JSON.stringify(data1));
                            var count = 0;
                            $(data1).each(function (idx, o) {
                                data2 = jQuery.parseJSON(JSON.stringify(o));
                                if(count == 0 ){
                                    $("#data-voip-paginator").attr("data-number", data2.indexPage);
                                    $("#data-voip-paginator").attr("data-totalPages", data2.totalPage);
                                    $("#data-voip-paginator").attr("data-lastPage", data2.lastPage);

                                    $("input[type='number']").prop('min',1);
                                    $("input[type='number']").prop('max',data2.lastPage);
                                } else {
                                    var newRow = $('.edit-list-parameters-template').html();
                                    newRow = newRow.split('{id}').join(data2.id);
                                    $("#1").attr("name", 'lol');
                                    newRow = newRow.split('{manufacturer}').join(data2.manufacture);
                                    newRow = newRow.split('{modelName}').join(data2.modelName);
                                    newRow = newRow.split('{serialNumber}').join(data2.serialNumber);
                                    newRow = newRow.split('{srcIP}').join(data2.srcIP);
                                    newRow = newRow.split('{destIP}').join(data2.destIP);
                                    newRow = newRow.split('{line}').join(data2.line);
                                    newRow = newRow.split('{packetLost}').join(data2.packetLost);
                                    newRow = newRow.split('{byteSent}').join(data2.byteSent);
                                    newRow = newRow.split('{byteRecv}').join(data2.byteReceive);
                                    $('.edit-list-parameters').append(newRow);
                                }
                                count++;
                            });

                        }
                    },
                    error: function(error) {
                        //not existed by default
                    }
                });
        }


    }



    $('#checkAllvoip').change(function() {
        if ($(this).is(":checked")) {
            checkAll();
        } else {
            unCheckAll();
        }
    });

    function checkAll() {
        totalDeviceBeChecked =0;
        $('.myCheckBoxvoip').each(function() {
            var id = $(this).attr('id');
            if(id != '{id}'){
                $('#' + id).prop('checked', true);
                totalDeviceBeChecked++;
            }

        });

    }
    function unCheckAll() {
        totalDeviceBeChecked = 0;
        $('.myCheckBoxvoip').each(function() {
            var id = $(this).attr('id');
            if(id != '{id}'){
                $('#' + id).prop('checked', false);
            }
        });

    }


    $('#confirm-delete-voip-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '50%',
        height: 'auto'
    });

    $('#delete-opener-voip').click(function() {
        deleteNamesDisplay = [];
        deleteIds = [];
        $('.myCheckBoxvoip').each(function() {
            var id = $(this).attr('data-id');
            if ($(this).is(':checked')) {
                deleteIds.push(id);
            }
        });

        $("#confirm-delete-voip-dialog").data('title.dialog', ' I am the new title');
        $('#confirm-delete-voip-dialog').dialog('open');
    });

    $('#confirm-delete-voip-yes-dialog').click(function() {
//            $.ajax({
//                type: 'POST',
//                url: "/file_management/delete/",
//                data: {
//                    deleteIds: deleteIds+"@",
//                    deleteNames: deleteNames+"@",
//                },
//                success: function(result) {
//                    window.location = "/file_management";
//                },
//                error: function(result) {
//                },
//                async: true
//            });

        $('#confirm-delete-voip-dialog').dialog('close');
    });
    $('#confirm-delete-voip-no-dialog').click(function() {
        $('#confirm-delete-voip-dialog').dialog('close');
    });



    $('#export-opener-voip').click(function() {
        paramExport = $('#performanceSettingVoip').val().replace(/\s/g, 'T');
        window.location.href = location.origin + '/performance-setting/exportExcel?paramExport='+paramExport;
    });

    $('#chart-opener-voip').click(function() {
        $('#form_chart_voip')[0].reset();
        dataRefresh = $('#performanceSettingVoip').val().split('/');
        if(dataRefresh[9] == '1'){
            dataManufacturer = JSON.parse('[{"id": "'+dataRefresh[6]+'", "text": "'+dataRefresh[6]+'"}]');
            dataModelName = JSON.parse('[{"id": "'+dataRefresh[7]+'", "text": "'+dataRefresh[7]+'"}]');
            dataSerialNumber = JSON.parse('[{"id": "'+dataRefresh[8]+'", "text": "'+dataRefresh[8]+'"}]');

            $("#manufacturerVoip").html('').select2({data: dataManufacturer});
            $("#modelNameVoip").html('').select2({data: dataModelName});
            $("#serialNumberVoip").html('').select2({data: dataSerialNumber});
        } else {
            data2 = 'deviceGroupId='+dataRefresh[0] + '&manufacturer='+dataRefresh[6]
                    + '&modelName='+dataRefresh[7] + '&monitoring='+dataRefresh[9] + '&performanceSettingId='+dataRefresh[1];
            $.ajax({
                type: "GET",
                url: "/performance-setting/data-confirm-chart?"+data2,
                success: function (data) {
                    data1 = data.split('|');
                    dataManufacturer = JSON.parse(data1[0]);
                    dataModelName = JSON.parse(data1[1]);
                    $("#manufacturerVoip").html('').select2({data: dataManufacturer});
                    $("#modelNameVoip").html('').select2({data: dataModelName});
                    $("#modelNameVoip").select2("val", "");
                },
                error: function (e) {
                    console.log('error : '+e);
                }
            });
        }
        $('#modal_confirm_chart_voip').modal({
            show: true
        });
    });

    $("#modelNameVoip").change(function(){
        var modelNameDataVar = $(this).val();
        if(modelNameDataVar != null && modelNameDataVar != ''){
            manufacturerDataVar = $('#manufacturerVoip').val();
            searchDevicesVoip(manufacturerDataVar,modelNameDataVar);
        }
    });

    function searchDevicesVoip(manufacturerDataVar,modelNameDataVar){
        $.ajax({
            type: "GET",
            url: "/performance-setting/data-confirm-chart",
            data: {
                manufacturer: manufacturerDataVar,
                modelName: modelNameDataVar
            },
            success: function (data) {
                dataSerialNumber = JSON.parse(data);
                $("#serialNumberVoip").html('').select2({data: dataSerialNumber});
            },
            error: function (e) {
                console.log('error : '+e);
            }
        });
    }


    $('#view-detail-voip-close').click(function() {
        $('#show-data-on-chart-dialog-voip').dialog('close');
        $('#form_chart_voip')[0].reset();
    });

    $("#form_chart_voip").on("submit", function(event) {
        event.preventDefault();
        check = $("#form_chart_voip").valid();
        if(check){
            var data = $('#form_chart_voip').serialize();
            $.ajax({
                type: "POST",
                url: "/performance-setting/voip-service-chart?"+data,
                success: function (data) {
                    showChartVoip(data);
                },
                error: function (e) {
                    console.log('error : '+e);
                }
            });
        }
    });

    function showChartVoip(data){
        getDataChartVoip(data);
        $('#modal_confirm_chart_voip').modal('hide');
        $('#modal_chart_voip').modal({
                    show: true
        });
    }

    function getDataChartVoip(data){
        var dataChart = data.split("|");
        getDataChartByteSentVoip(dataChart[0],dataChart[2]);
        getDataChartByteRecvVoip(dataChart[1],dataChart[2]);
    }

    function getDataChartByteSentVoip(dataChart,dataChartSerial){
        var columnX = ['Interface'];
        var columnY = ['Mb'];
        var columnTitle = ['Byte sent of '+ dataChartSerial];
        var dataNumberOfByteSent = JSON.parse(dataChart);
        Highcharts.chart('byte_sent_voip', {
            exporting: { enabled: false },
            chart: {
                type: 'column'
            },
            title: {
                text: columnTitle
            },
            xAxis: {
                categories: columnX,
                crosshair: true
            },
            yAxis: {
                min: 0,
                title: {
                    text: columnY
                }
            },
            tooltip: {
                headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
                pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                    '<td style="padding:0"><b>{point.y}</b></td></tr>',
                footerFormat: '</table>',
                shared: true,
                useHTML: true
            },
            plotOptions: {
                column: {
                    pointPadding: 0.2,
                    borderWidth: 0
                }
            },
            series: dataNumberOfByteSent
        });
    }

    function getDataChartByteRecvVoip(dataChart,dataChartSerial){
        var columnX = ['Interface'];
        var columnY = ['Mb'];
        var columnTitle = ['Byte recv of '+ dataChartSerial];
        var dataNumberOfByteRecv = JSON.parse(dataChart);
        Highcharts.chart('byte_recv_voip', {
            exporting: { enabled: false },
            chart: {
                type: 'column'
            },
            title: {
                text: columnTitle
            },
            xAxis: {
                categories: columnX,
                crosshair: true
            },
            yAxis: {
                min: 0,
                title: {
                    text: columnY
                }
            },
            tooltip: {
                headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
                pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                    '<td style="padding:0"><b>{point.y}</b></td></tr>',
                footerFormat: '</table>',
                shared: true,
                useHTML: true
            },
            plotOptions: {
                column: {
                    pointPadding: 0.2,
                    borderWidth: 0
                }
            },
            series: dataNumberOfByteRecv
        });
    }



});