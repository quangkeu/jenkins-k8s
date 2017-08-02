$(document).ready(function(){

    var paginator_data = $('#data-lan-wan-wlan-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input-lan-wan-wlan').val());

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
                var limitPageInput = parseInt($('.limit-page-input-lan-wan-wlan').val());
                pagingDataModel(page, limitPageInput);

            }
        }
    });
    $('.go-page-btn').click(function () {
        var goPageInputVar = $('.go-page-input-lan-wan-wlan').val();
        if(goPageInputVar.indexOf("-") >= 0
            || goPageInputVar.indexOf("+") >= 0 || goPageInputVar.indexOf(".") >= 0
            || goPageInputVar == ""){
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
        } else {
            var goPageInput = parseInt($('.go-page-input-lan-wan-wlan').val());
            var limitPageInput = parseInt($('.limit-page-input-lan-wan-wlan').val());
            if(0 < goPageInput && goPageInput <= lastPagesVar){
                pagingDataModel(goPageInput, limitPageInput);
            } else {
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
            }
        }

    });
    $('.limit-page-input-lan-wan-wlan').change(function () {
        var limitPageInput = parseInt($('.limit-page-input-lan-wan-wlan').val());
        var indexPage = parseInt(paginator_data.attr('data-number'));
        pagingDataModel(1, limitPageInput);
    });

    function pagingDataModel(index,limit){
    }



    $('#view-detail-lan-close').click(function() {
        $('#show-data-on-chart-dialog-lan').dialog('close');
    });

    $('#refresh-opener-lan').click(function() {
        refreshInterface();
    });

    function refreshInterface(){
        dataRefresh = $('#performanceSettingLanWanWLan').val().split('/');
        console.log('dataRefresh : '+dataRefresh);
        $.ajax({
            type: "GET",
            url: "/performance-setting/lan-wan-wLan-view-detail",
            data: {
                statisticType: dataRefresh[2],
                deviceGroupId: dataRefresh[0],
                performanceSettingId: dataRefresh[1],
                textFromDate: dataRefresh[3],
                textToDate: dataRefresh[4],
                monitoring: dataRefresh[9],
                manufacturer: dataRefresh[6],
                modelName: dataRefresh[7],
                serialNumber: dataRefresh[8],
                type: dataRefresh[5],
                indexPage:'1',
                limitPage:'20'
            },
            dataType:"text",
            success: function (data) {
                console.log('/performance-setting/lan-wan-wLan-view-detail');
                data1 = JSON.parse(data);
                if(data1 != null){
                    showModalLanWanWLan(data1, dataRefresh[2], dataRefresh[5]);
                }
            },
            error: function (e) {
                console.log('error : '+e);
            }
        });
    }

    $('#chart-opener-lan').click(function() {
        $('#form_chart_lan_wan_wLan')[0].reset();
        $("#modelName").select2("val", "");
        $("#serialNumber").select2("val", "");
        dataRefresh = $('#performanceSettingLanWanWLan').val().split('/');
        if(dataRefresh[9] == '1'){
            dataManufacturer = JSON.parse('[{"id": "'+dataRefresh[6]+'", "text": "'+dataRefresh[6]+'"}]');
            dataModelName = JSON.parse('[{"id": "'+dataRefresh[7]+'", "text": "'+dataRefresh[7]+'"}]');
            dataSerialNumber = JSON.parse('[{"id": "'+dataRefresh[8]+'", "text": "'+dataRefresh[8]+'"}]');

            $("#manufacturer").html('').select2({data: dataManufacturer});
            $("#modelName").html('').select2({data: dataModelName});
            $("#serialNumber").html('').select2({data: dataSerialNumber});
        } else {
            data2 = 'deviceGroupId='+dataRefresh[0] + '&manufacturer='+dataRefresh[6]
                    + '&modelName='+dataRefresh[7] + '&monitoring='+dataRefresh[9] + '&performanceSettingId='+dataRefresh[1];
                    console.log('data2 : '+data2);
                $.ajax({
                    type: "GET",
                    url: "/performance-setting/data-confirm-chart?"+data2,
                    success: function (data) {
                        data1 = data.split('|');
                        dataManufacturer = JSON.parse(data1[0]);
                        dataModelName = JSON.parse(data1[1]);
                        $("#manufacturer").html('').select2({data: dataManufacturer});
                        $("#modelName").html('').select2({data: dataModelName});
                        $("#modelName").select2("val", "");
                    },
                    error: function (e) {
                        console.log('error : '+e);
                    }
                });
        }
        $('#modal_confirm_chart_lan_wan_wLan').modal({
            show: true
        });

    });

    $("#modelName").change(function(){
        var modelNameDataVar = $(this).val();
        if(modelNameDataVar != null && modelNameDataVar != ''){
            manufacturerDataVar = $('#manufacturer').val();
            searchDevices(manufacturerDataVar,modelNameDataVar);
        }
    });

    function searchDevices(manufacturerDataVar,modelNameDataVar){
        $.ajax({
            type: "GET",
            url: "/performance-setting/data-confirm-chart",
            data: {
                manufacturer: manufacturerDataVar,
                modelName: modelNameDataVar
            },
            success: function (data) {
                dataSerialNumber = JSON.parse(data);
                $("#serialNumber").html('').select2({data: dataSerialNumber});
            },
            error: function (e) {
                console.log('error : '+e);
            }
        });

    }

    $('#export-opener-lan').click(function() {
        paramExport = $('#performanceSettingLanWanWLan').val().replace(/\s/g, 'T');
        window.location.href = location.origin + '/performance-setting/exportExcel?paramExport='+paramExport;
    });

    $("#form_chart_lan_wan_wLan").on("submit", function(event) {
        event.preventDefault();
        check = $("#form_chart_lan_wan_wLan").valid();
        performanceSettingId = $('#performanceSettingLanWanWLan').val().split('/');
        if(check){
            var data = $('#form_chart_lan_wan_wLan').serialize();
            $.ajax({
                type: "POST",
                url: "/performance-setting/lan-service-chart?"+data
                        +'&statisticType='+ performanceSettingId[2]
                        +'&performanceSettingId='+ performanceSettingId[1]
                        +'&type='+ performanceSettingId[5],
                success: function (data) {
                    console.log('success : '+data);
                    showChart(data, performanceSettingId[5]);
                },
                error: function (e) {
                    console.log('error : '+e);
                }
            });
        }
    });

    function showChart(data, type){
        getDataChart(data, type);
        $('#modal_confirm_chart_lan_wan_wLan').modal('hide');
        $('#modal_chart_lan_wan_wLan').modal({
                    show: true
        });
    }

    function getDataChart(data, type){
        var dataChart = data.split("|");
        var data1 = dataChart[0];
        var data2 = dataChart[1];
        var data3 = dataChart[2];
        var data4 = dataChart[3];
        var dataCategoriesChart = dataChart[4];
        var dataSerialNumber = dataChart[5];
        var title = '';
        if(type == 'TRANSMITTED'){
            title = "Total bytes transmitted of "+dataSerialNumber;
        } else {
            title = "Total bytes received of "+dataSerialNumber;
        }
        getDataChartByteRecv(data1, dataCategoriesChart, title);
        getDataChartByteTransmitted(data2, dataCategoriesChart, title);
        getDataChartError(data3, dataCategoriesChart, title);
        getDataChartDrop(data4, dataCategoriesChart, title);
    }


    function getDataChartByteRecv(dataChart,dataCategoriesChart, dataChartSerial){
        var dataChartArray = JSON.parse(dataChart);
        var dataCategoriesChartArray = JSON.parse(dataCategoriesChart);

        Highcharts.chart('byte_recv_lan', {
            exporting: { enabled: false },
            chart: {
                type: 'line'
            },
            title: {
                text: dataChartSerial
            },
            xAxis: {
                categories: dataCategoriesChartArray,
                title: {
                    text: 'Time'
                },
                min:1
            },
            yAxis: {
                title: {
                    text: 'Mb'
                }
            },
            plotOptions: {
                line: {
                    dataLabels: {
                        enabled: true
                    },
                    enableMouseTracking: true
                }
            },
            scrollbar: {
                enabled: true
            },
            series: dataChartArray
        });
    }

    function getDataChartByteTransmitted(dataChart, dataCategoriesChart, dataChartSerial){

        var dataChartArray = JSON.parse(dataChart);
        var dataCategoriesChartArray = JSON.parse(dataCategoriesChart);
        Highcharts.chart('byte_transmit_lan', {
            exporting: { enabled: false },
            chart: {
                type: 'line'
            },
            title: {
                text: dataChartSerial
            },
            xAxis: {
                categories: dataCategoriesChartArray,
                title: {
                    text: 'Time'
                },
                min:1
            },
            yAxis: {
                title: {
                    text: 'Mb'
                }
            },
            plotOptions: {
                line: {
                    dataLabels: {
                        enabled: true
                    },
                    enableMouseTracking: true
                }
            },

            scrollbar: {
                enabled: true
            },
            series: dataChartArray
        });
    }

    function getDataChartError(dataChart, dataCategoriesChart, dataChartSerial){
        var dataChartArray = JSON.parse(dataChart);
        var dataCategoriesChartArray = JSON.parse(dataCategoriesChart);
        Highcharts.chart('error_recv_lan', {
            exporting: { enabled: false },
            chart: {
                type: 'line'
            },
            title: {
                text: dataChartSerial
            },
            xAxis: {
                categories: dataCategoriesChartArray,
                title: {
                    text: 'Time'
                },
                min:1
            },
            yAxis: {
                title: {
                    text: 'Mb'
                }
            },
            plotOptions: {
                line: {
                    dataLabels: {
                        enabled: true
                    },
                    enableMouseTracking: true
                }
            },

            scrollbar: {
                enabled: true
            },
            series: dataChartArray
        });
    }

    function getDataChartDrop(dataChart, dataCategoriesChart, dataChartSerial){
        var dataChartArray = JSON.parse(dataChart);
        var dataCategoriesChartArray = JSON.parse(dataCategoriesChart);
        Highcharts.chart('drop_transmit_lan', {
           exporting: { enabled: false },
           chart: {
               type: 'line'
           },
           title: {
               text: dataChartSerial
           },
           xAxis: {
               categories: dataCategoriesChartArray,
               title: {
                   text: 'Time'
               },
               min:1
           },
           yAxis: {
               title: {
                   text: 'Mb'
               }
           },
           plotOptions: {
               line: {
                   dataLabels: {
                       enabled: true
                   },
                   enableMouseTracking: true
               }
           },
           scrollbar: {
                enabled: true
           },
           series: dataChartArray
        });
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
            console.log('headerTime : '+headerTime);
            if(headerTime != ''){
                $('#alert_success_lan_wan_wLan').hide();
                $('#div_lan_wan_wlan_paging').show();
                var countCheckBox =0;
                $(data1).each(function (idx, o) {
                    data2 = jQuery.parseJSON(JSON.stringify(o));
                    $.each(data2, function(key, value){
                        if(key.startsWith(performanceTypeName)){
                            dataTableFix += '<tr>'
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
                                            dataTimeTemp += '<td>'+'0'+'</td>';
                                        } else {
                                            dataTimeTemp += '<td>'+value.bytes+'</td>';
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

                headerTableFix +='<tr>'
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

    var deleteIds = [];
    var deleteInterface = [];
    var totalInterfaceBeChecked = 0;

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

    $('.myCheckBoxLanWanWWLan').change(function() {
        if ($(this).is(":checked")) {
            totalInterfaceBeChecked = totalInterfaceBeChecked + 1;
        } else {
            totalInterfaceBeChecked = totalInterfaceBeChecked - 1;
        }
    });

    $('.myCheckBoxLanWanWWLan').each(function() {
        if ($(this).is(':checked')) {
            totalInterfaceBeChecked++;
        } else {
            if(totalInterfaceBeChecked != 0){totalInterfaceBeChecked--;}
        }
    });

    $('#delete-opener-lan').click(function() {
        deleteIdsLanWanWLan = [];
        deleteInterface = [];
        performanceSettingId = $('#performanceSettingLanWanWLan').val().split('/');;
        $('.myCheckBoxLanWanWWLan').each(function() {
            var id = $(this).attr('id');
            var name = $(this).attr('name');
            if ($(this).is(':checked')) {
                deleteIdsLanWanWLan.push(id);
                deleteInterface.push(name);
            }
        });

//        var modal = $("#modal_confirm_delete_lan_wan_wlan");
//      var body = $(window);
//      // Get modal size
//      var w = modal.width();
//      var h = modal.height();
//      // Get window size
//      var bw = body.width();
//      var bh = body.height();
//
//      modal.css({
//        "position": "absolute",
//        "top": ((bh - h) / 2) + "px",
//        "left": ((bw - w) / 2) + "px"
//      })

        if(deleteIdsLanWanWLan != ''){
            $('#modal_confirm_delete_lan_wan_wlan').modal({
                show: true
            });
            $('#dataDeleteLanWanWlan').val(deleteIdsLanWanWLan+'|'+deleteInterface+'|'+performanceSettingId[1]);
        }


    });

    $('#confirm_delete_yes_lan_wan_wlan').click(function() {
        dataDelete = $('#dataDeleteLanWanWlan').val().split('|');
        if(deleteIdsLanWanWLan != ''){
            $.ajax({
                type: "POST",
                url: "/performance-setting/delete-performance",
                data: {
                    deleteIds: dataDelete[0]+'@',
                    deleteInterface: dataDelete[1]+'@',
                    performanceSettingId: dataDelete[2]
                },
                success: function (data) {
                    console.log('/performance-setting/delete-performance');
                    $('#modal_confirm_delete_lan_wan_wlan').modal('hide');
                    // setTimeout(
                    //   function()
                    //   {
                    //     $('#refresh-opener-lan').trigger('click');
                    //   }, 1000);

                    // Hide row after call remove api success
                    var keyRemove = dataDelete[1].split(',');
                    $.each(keyRemove, function (index, value) {
                        $("#div_lan_wan_wLan_fix tbody").find('tr[data-keyRow="'+value+'"]').remove();
                        $("#div_lan_wan_wLan_time tbody").find('td[data-keyRow="'+value+'"]').parent('tr').remove();
                    });

                },
                error: function (e) {
                    console.log('error : '+e);
                }
            });
        }
    });
});