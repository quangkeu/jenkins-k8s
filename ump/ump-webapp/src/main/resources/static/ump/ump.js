/**
 * Created by vietnq on 11/3/16.
 */
$(function(){

    $('#create-device-type-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: 500
    });

    $('#create-device-type-dialog-opener').click(function() {
        $('#create-device-type-dialog').dialog('open');
    });
});