$(document).ready(function() {

    /*
    remocce loadding icon in tab
    */
    $("#_tabTask i").remove();

    /**
        init table
    */
    var table = $('#_tableTask').DataTable({
        "paging": false,
        "info": false,
        "filter": false,
        "scrollY": "500px",
        "scrollCollapse": true
    });


    // hiden ID column
    var column0 = table.column(0);
    var column1 = table.column(1);
    // Toggle the visibility
    column0.visible(!column0.visible());
    column1.visible(!column1.visible());

    /**
    event for close dialuge success or failed
    */
    $('.close').click(function() {
        $('#_taskAlertSuccess').hide();
        $('#_taskAlertPrimary').hide();
    })

});
/**
load data to task table
*/
function fillTab(deviceId) {
    $("#_tableTask_wrapper").load("/tasks?deviceId=" + deviceId);
    return false;
}

/**
delete a task with taskId.
Param deviceId to show task table again
*/
function deleteTask(taskId, deviceId) {
    console.log("taskId" + taskId);
    console.log("DeviceID" + deviceId);
    var id = "btnDelete_" + taskId;
    var iconTurnAround = '<i class="icon-spinner9 spinner position-right"></i>';
    $('#' + id).append(iconTurnAround);
    $('#' + id).addClass('disabled');
    $.ajax({
        type: 'GET',
        url: "/tasks/delete?deviceId=" + deviceId + "&taskId=" + taskId,
        success: function() {
            $('#idRow_' +taskId).remove();
//            fillTab(deviceId);
            $('#_taskAlertSuccess span.text-semibold').text("Delete Success!");
            $('#_taskAlertSuccess').show(2000);
        },
        error: function(data) {
            console.log(JSON.stringify(data));
            $('#' + id).addClass('enable');
            $('#' + id + " i").remove();
            $('#_taskAlertPrimary span.text-semibold').text("Delete Failed!");
            $('#_taskAlertPrimary').show(2000);
        },
        async: true
    });

}
/**
 retry a task with taskId.
 Param deviceId to show task table again
 */
function retryTask(taskId, deviceId) {
    console.log("taskId" + taskId);
    console.log("DeviceID" + deviceId);
    var id = "btnRetry_" + taskId;
    var iconTurnAround = '<i class="icon-spinner9 spinner position-right"></i>';
    $('#' + id).append(iconTurnAround);
    $('#' + id).addClass('disabled');
    $.ajax({
        type: 'GET',
        url: "/tasks/retry?deviceId=" + deviceId + "&taskId=" + taskId,
        success: function() {
            fillTab(deviceId);
            $('#_taskAlertSuccess span.text-semibold').text("Retry Success!");
            $('#_taskAlertSuccess').show();
        },
        error: function() {
            $('#' + id).addClass('enable');
            $('#' + id + " i").remove();
            $('#_taskAlertPrimary span.text-semibold').text("Retry Failed!");
            $('#_taskAlertPrimary').show();
        },
        async: true
    });


}