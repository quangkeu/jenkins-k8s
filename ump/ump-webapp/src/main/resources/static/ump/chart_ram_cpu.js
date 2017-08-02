$(document).ready(function(){

    var paginator_data = $('#data-ram-cpu-paginator');
    var indexPage = parseInt(paginator_data.attr('data-number'));
    var lastPagesVar = parseInt(paginator_data.attr('data-lastPage'));
    var startPageVar = parseInt(paginator_data.attr('data-number'));
    var limitPageInput = parseInt($('.limit-page-input-ram-cpu').val());

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
                var limitPageInput = parseInt($('.limit-page-input-ram-cpu').val());
                pagingDataModel(page, limitPageInput);

            }
        }
    });
    $('.go-page-btn').click(function () {
        var goPageInputVar = $('.go-page-input-ram-cpu').val();
        if(goPageInputVar.indexOf("-") >= 0
            || goPageInputVar.indexOf("+") >= 0 || goPageInputVar.indexOf(".") >= 0
            || goPageInputVar == ""){
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
        } else {
            var goPageInput = parseInt($('.go-page-input-ram-cpu').val());
            var limitPageInput = parseInt($('.limit-page-input-ram-cpu').val());
            if(0 < goPageInput && goPageInput <= lastPagesVar){
                pagingDataModel(goPageInput, limitPageInput);
            } else {
                $('#alert_danger span.text-bold').text("Invalid Number!");
                $('#alert_danger').show();
            }
        }

    });
    $('.limit-page-input-ram-cpu').change(function () {
        var limitPageInput = parseInt($('.limit-page-input-ram-cpu').val());
        var indexPage = parseInt(paginator_data.attr('data-number'));
        pagingDataModel(1, limitPageInput);
    });

    function pagingDataModel(index,limit){
    }

    $('#chart-opener-ram-cpu').click(function() {
        $.ajax({
            type: "POST",
            url: "/performance-setting/ram-cpu-service-chart",
            success: function (data) {
                console.log('success : '+data);
                showChart(data);
            },
            error: function (e) {
                console.log('error : '+e);
            }
        });
    });

    $('#refresh-opener-ram-cpu').click(function() {
        refreshRamCpu();
    });

    function refreshRamCpu(){
        dataRefresh = $('#performanceSettingRamCpu').val().split('/');
        $.ajax({
            type: "GET",
            url: "/performance-setting/ram-cpu-view-detail",
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
            },
            dataType:"text",
            success: function (data) {
                data1 = JSON.parse(data);
                if(data1 != null) {
                    showModalRamCpu(data1, dataRefresh[2]);
                }
            },
            error: function (e) {
                console.log('error : '+e);
            }
        });
    }

    $('#export-opener-ram-cpu').click(function() {
        paramExport = $('#performanceSettingRamCpu').val().replace(/\s/g, 'T');
        window.location.href = location.origin + '/performance-setting/exportExcel?paramExport='+paramExport;
    });

    $('#chooseDeviceToShowChart').on('hidden', function() {
        console.log('duyt');

    });

    $('#view-detail-ram-cpu-close').click(function() {
        $('#show-data-on-chart-dialog-ram-cpu').dialog('close');
    });


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

                if(data2.id != null){
                    dataTableFix += '<tr>';
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
                    dataTime += '<tr id="myTableTimeRow">'+temp+'</tr>';
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


    function showChart(data){
        $("#div_ram_cpu_chart_thead").empty();
        $("#div_ram_cpu_chart_tbody").empty();
        getDataChart(data);
        $('#modal_chart_ram_cpu').modal({show:true});
    }

    function getDataChart(data){
        var dataChart = data.split("|");
        var data1 = dataChart[0];
        var dataCategoriesChart = dataChart[1];
        var dataSerialNumber = dataChart[2];
        var dataTableSerialNumber = dataChart[3];
        getDataChartRamCpu(data1, dataCategoriesChart, dataSerialNumber);
        getDataTableRamCpu(dataCategoriesChart, data1,dataTableSerialNumber);
    }

    function getDataTableRamCpu(dataCategoriesChart, dataChartRamCpu,dataTableSerialNumber){
        data2 = jQuery.parseJSON(dataCategoriesChart);
        var dataChartArrayRamCpu = JSON.parse(dataChartRamCpu);
        dataTableSerialNumber1 = dataTableSerialNumber.split(',');
                console.log('dataChartArrayRamCpu : '+JSON.stringify(dataChartArrayRamCpu));
        var dataTableRamCpu = '<tr>';

        // header
        var headerTableRamCpu = '<tr><th nowrap>Time</th>';
        for(var header=0;header<dataTableSerialNumber1.length;header++){
            headerTableRamCpu += '<th nowrap>'+dataTableSerialNumber1[header]+'</th>';
        }
        headerTableRamCpu += '</tr>';

        //row
        for(var time=0;time<data2.length;time++){
            dataTableRamCpuTemp = '<tr>';
            for(var array=0;array<dataChartArrayRamCpu.length;array++){
                arrayTemp = dataChartArrayRamCpu[array];
                dataTableRamCpuTemp += '<td nowrap>'+data2[time]+'</td>';
                dataTableRamCpuTemp += '<td nowrap>'+arrayTemp.data[time]+'</td>';
                dataTableRamCpu += dataTableRamCpuTemp;
            }
            dataTableRamCpuTemp += '</tr>';
        }

        $("#div_ram_cpu_chart thead").append(headerTableRamCpu);
        $("#div_ram_cpu_chart tbody").append(dataTableRamCpu);
    }

    function getDataChartRamCpu(dataChart,dataCategoriesChart, dataChartSerial){

        var dataChartArray = JSON.parse(dataChart);
        var dataChartTitle = dataChartSerial;
        var dataCategoriesChartArray = JSON.parse(dataCategoriesChart);

        Highcharts.chart('byte_recv_ram_cpu', {
            exporting: { enabled: false },
            chart: {
                type: 'line'
            },
            title: {
                text: dataChartTitle
            },
            xAxis: {
                categories: dataCategoriesChartArray,
                title: {
                    text: 'Time'
                },
                min:1
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
    
    
    deleteIdsRamCpu = [];
    var totalInterfaceBeCheckedRamCpu = 0;
    
    $('.myCheckBoxRamCpu').change(function() {
        if ($(this).is(":checked")) {
            totalInterfaceBeCheckedRamCpu = totalInterfaceBeCheckedRamCpu + 1;
        } else {
            totalInterfaceBeCheckedRamCpu = totalInterfaceBeCheckedRamCpu - 1;
        }
    });

    $('.myCheckBoxRamCpu').each(function() {
        if ($(this).is(':checked')) {
            totalInterfaceBeCheckedRamCpu++;
        } else {
            if(totalInterfaceBeCheckedRamCpu != 0){totalInterfaceBeCheckedRamCpu--;}
        }
    });
    
    function checkAllRamCpu() {
        totalInterfaceBeCheckedRamCpu =0;
        $('.myCheckBoxRamCpu').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', true);
           totalInterfaceBeCheckedRamCpu++;
        });
    }

    function unCheckAllRamCpu() {
        totalInterfaceBeCheckedRamCpu = 0;
        $('.myCheckBoxRamCpu').each(function() {
            var id = $(this).attr('id');
            $('#' + id).prop('checked', false);
        });
    }

    $('#delete-opener-ram-cpu').click(function() {
        deleteIdsRamCpu = [];
        performanceSettingId = $('#performanceSettingRamCpu').val().split('/');
        $('.myCheckBoxRamCpu').each(function() {
            var id = $(this).attr('id');
            var name = $(this).attr('name');
            if ($(this).is(':checked')) {
                deleteIdsRamCpu.push(id);
            }
        });
        console.log('deleteIdsRamCpu : '+deleteIdsRamCpu);
        if(deleteIdsRamCpu != ''){
            $('#modal_confirm_delete_ram_cpu').modal({
                show: true
            });
            $('#dataDeleteRamCpu').val(deleteIdsRamCpu+'||'+performanceSettingId[1]);
        }
    });

    $('#confirm_delete_yes_ram_cpu').click(function() {
        dataDelete = $('#dataDeleteRamCpu').val().split('|');
        if(deleteIdsRamCpu != ''){
        $.ajax({
            type: "POST",
            url: "/performance-setting/delete-performance",
            data: {
                deleteIds: deleteIdsRamCpu+'@',
                deleteInterface: '@',
                performanceSettingId: dataDelete[2]
            },
            success: function (data) {
                $('#modal_confirm_delete_ram_cpu').modal('hide');
                // setTimeout(
                //   function()
                //   {
                //     $('#refresh-opener-ram-cpu').trigger('click');
                //   }, 1000);

                // Hide row after call remove api success
                var keyRemove = dataDelete[0].split(',');
                $.each(keyRemove, function (index, value) {
                    $("#div_ram_cpu_fix tbody").find('tr[data-keyRow="'+value+'"]').remove();
                    $("#div_ram_cpu_time tbody").find('tr[data-keyRow="'+value+'"]').remove();
                });

            },
            error: function (e) {
                console.log('error : '+e);
            }
        });
        }
    });


});