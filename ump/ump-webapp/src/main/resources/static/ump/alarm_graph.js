$(document).ready(function() {

    var isValid = true;

    $('#search-by-time').click(function() {
        var groupFilter = $('#group-filter-search').val();
        var startTime = $('#start-time').val();
        var endTime = $('#end-time').val();

        validateInput(groupFilter, startTime, endTime);

        if(isValid){
            totalParam = "groupFilter="+groupFilter + "&startTime="+startTime
                            + "&endTime="+endTime;
            window.location.href = "/alarm-graph/view-graph?"+totalParam;
        }

    });

    function validateInput(groupFilter, startTime, endTime){
//        if(groupFilter == '' && startTime == '' && endTime == ''){
//            isValid = false;
//            return;
//        }

        if(startTime == '' && endTime != ''){
            isValid = false;
            $('#raised-time-from').addClass("red");
            $('#raised-time-to').removeClass("red");
            return;
        }

        if(startTime != '' && endTime == ''){
            isValid = false;
            $('#raised-time-to').addClass("red");
            $('#raised-time-from').removeClass("red");
            return;
        }
    }

// Build the chart
//    var dataSeverityAlarm = [{
//                          "name": 'Critical',
//                          "y": 56.33,
//                          "sliced": true,
//                          "selected": true
//                      }, {
//                          "name": 'Major',
//                          "y": 24.03
//                      }, {
//                          "name": 'Minor',
//                          "y": 10.38
//                      }, {
//                          "name": 'Warning',
//                          "y": 4.77
//                      }, {
//                          "name": 'Info',
//                          "y": 0.91
//                      }];
    var dataSeverityAlarm = JSON.parse($('#data-severity-alarm').val());
    Highcharts.chart('severity-alarm-chart', {
        colors: ['#ff0000', '#ff9933', '#ffff00', '#0099ff','#009900'],
        exporting: { enabled: false },
          chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            type: 'pie'
        },
        title: {
            text: 'Severity of alarm'
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: false
                },
                showInLegend: true,
            }
        },
        series: [{
            name: 'Brands',
            colorByPoint: true,
            point: {
                events: {
                    legendItemClick: function () {
                        return false;
                    }
                }
            },
            data: dataSeverityAlarm
        }]
    });

//    var dataNumberOfAlarmType = [
//                                    {
//                                    name: 'Tokyo',
//                                    data: [135.6]
//
//                                    }, {
//                                        name: 'New York',
//                                        data: [106.0]
//                                    }, {
//                                        name: 'London',
//                                        data: [38.8]
//                                    }, {
//                                        name: 'Berlin',
//                                        data: [75.5]
//                                    },{
//                                      name: 'Ha Noi',
//                                      data: [39.1]
//
//                                    },{
//                                      name: 'Berlin',
//                                      data: [51.1]
//                                    }
//                                ];
    var dataNumberOfAlarmTypeCategories = ['Alarm Type'];
    var dataNumberOfAlarmType = JSON.parse($('#data-number-of-alarm-type').val());
    Highcharts.chart('container', {
        exporting: { enabled: false },
        chart: {
            type: 'column'
        },
        title: {
            text: 'Number of alarm type'
        },
        xAxis: {
            categories: dataNumberOfAlarmTypeCategories,
            crosshair: true
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Value'
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
        series: dataNumberOfAlarmType
    });

});