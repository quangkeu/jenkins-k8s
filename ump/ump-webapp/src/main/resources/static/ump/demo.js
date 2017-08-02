$(document).ready(function() {
    $('#DataTables_Table_0 tbody').on("click","tr.viewDeviceTypeVersion",function(){
        var deviceTypeVersionId = $(this).find('.deviceTypeVersionId').text();
        window.location.href = "/data-models/"+deviceTypeVersionId+"/profile"
    })
})