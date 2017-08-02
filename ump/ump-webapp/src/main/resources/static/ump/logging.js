$(function () {

    $('select.logType').change(function () {
        var currentLogType = $(this).val();
        if (currentLogType === 'deviceLog') {
            location.replace(location.origin + '/logging/device')
        } else if (currentLogType === 'userLog') {
            location.replace(location.origin + '/logging/user')
        }
    })

});