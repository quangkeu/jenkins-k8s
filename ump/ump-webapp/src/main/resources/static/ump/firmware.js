/**
 * Created by vietnq on 11/3/16.
 */
$(function(){

    var a =1;
    // Device types tree
    $(".tree-default").fancytree({
        icon: false,
        click: function(event, data) {
            if(data.node.data.href && typeof data.node.data.href != 'undefined') {
                window.location.replace(document.location.origin + data.node.data.href);
            }
        }
    });

    // File input style
    $(".file-styled").uniform({
        fileButtonClass: 'action btn btn-default'
    });

    $('#create-device-type-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: 500
    });

    $('#create-device-type-dialog-opener').click(function() {
        $('#create-device-type-dialog').dialog('open');
    });

    $('#create-firmware-tasks-type-dialog').dialog({
            autoOpen: false,
            modal: true,
            width: 500
    });

    $('#create-firmware-tasks-type-dialog-opener').click(function() {
            $('#create-firmware-tasks-type-dialog').dialog('open');
    });

    $('#create_device-dialog-close').click(function() {
                $('#create-firmware-tasks-type-dialog').dialog('close');
        });

    $('.show-device-type-actions').hover(
        function() {
            $(this).find('.device-type-action-add').show();
        },
        function() {
            $(this).find('.device-type-action-add').hide();
        }
    );

    $('#create-firmware-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: 500
    });

    $('i.device-type-action-add').on('click',function(e) {
        e.stopPropagation();
        var dataDeviceType = $(this).attr("data-device-type");
        console.debug('deviceTypeId');
        var deviceTypeObject = jQuery.parseJSON(dataDeviceType);
         console.debug(deviceTypeObject.manufacturer);
        $('#create-firmware-dialog').find('input[name="deviceTypeId"]').attr("value",deviceTypeObject.id);
        $('#create-firmware-dialog').find('input[name="manufacture"]').attr("value",deviceTypeObject.manufacturer);
        $('#create-firmware-dialog').find('input[name="oui"]').attr("value",deviceTypeObject.oui);
        $('#create-firmware-dialog').find('input[name="name"]').attr("value",deviceTypeObject.name);
        $('#create-firmware-dialog').find('input[name="productClass"]').attr("value",deviceTypeObject.productClass);
        $(".filename").html("");
        $('input[name="version"]').val(null);
        $('#create-firmware-dialog').dialog('open');
        $('#saveBtn').attr("disabled", true);
        $('#inputFile').change(function() {
               $('#saveBtn').attr("disabled", false);
        });
    });

  $('div.load_detail').on('click',function(e) {
      //e.stopPropagation();
       $('#show-detail').show();
       $('#showLinkHeader').show();

  });

  $('#confirm-delete-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: 350
  });

  $('#confirm-delete-dialog-opener').click(function() {
        $('#confirm-delete-dialog').dialog('open');
   });
  $('#confirm-delete-dialog-close').click(function() {
           $('#confirm-delete-dialog').dialog('close');
  });

  // Task
    $('#confirm-delete-task-dialog').dialog({
          autoOpen: false,
          modal: true,
          width: 350
    });

    $('.confirm-delete-task-dialog-opener').click(function() {
          var dialog = $('#confirm-delete-task-dialog');
          var fwTaskId = $(this).attr('firmwareTaskId');
          $(dialog).find('input[name="firmwareTaskId"]').val(fwTaskId);
          dialog.dialog('open');

     });
    $('#confirm-delete-task-dialog-close').click(function() {
             $('#confirm-delete-task-dialog').dialog('close');
    });
  //

  //Edit
  $('.edit-task-dialog-opener').click(function() {
        var dialog = $('#create-firmware-tasks-type-dialog');
                  var fwTaskId = $(this).attr('firmwareTaskId');
                  var groupId = $(this).attr('groupId');
                  var type = $(this).attr('type');
                  $(dialog).find('select#groupId').val(groupId);
                  $(dialog).find('select#type').val(type);
                  $(dialog).find('input#id').val(fwTaskId);
       $('#create-firmware-tasks-type-dialog').dialog('open');

  });

  //

  $('#showLinkHeader').hide();
  $('#fileNameOfFirstVersion').hide();
  $(".filename").html($("#fileNameOfFirstVersion").html());
  $('#inputFileEdit').change(function() {
        $('#show-detail').find('input[name="flagEnableSaveFile"]').attr("value","1");
  });

    $("#create-firmware-form").validate(ump.validator);
    $("#edit-firmware-form").validate(ump.validator);

    $(".modal-cancel").click(function () {
        $('#create-firmware-dialog').dialog('close');
    })
});