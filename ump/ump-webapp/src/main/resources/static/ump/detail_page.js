$(function() {
    $('#confirm-reset-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '20%'
    });

    $('#confirm-reset-dialog-opener').click(function() {
        $('#confirm-reset-dialog').dialog('open');
    });

    $('#confirm-reset-dialog-close').click(function() {
        $('#confirm-reset-dialog').dialog('close');
    });
    $('#confirm-reboot-dialog').dialog({
        autoOpen: false,
        modal: true,
        width: '20%'
    });

    $('#confirm-reboot-dialog-opener').click(function() {
        $('#confirm-reboot-dialog').dialog('open');
    });

    $('#confirm-reboot-dialog-close').click(function() {
        $('#confirm-reboot-dialog').dialog('close');
    });

    $('#confirm-reset-now-dialog').hide();
    $('#confirm-reboot-now-dialog').hide();

    $("select#team").on("change", function() {
        var selectedD = $(this).val();
        if (selectedD === '1') {
            //Next contact
            $('#confirm-reset-now-dialog').hide();
            $('#confirm-reset-next-dialog').show();
            $('#confirm-reboot-now-dialog').hide();
            $('#confirm-reboot-next-dialog').show();
        }
        if (selectedD === '2') {
            //Now
            $('#confirm-reset-now-dialog').show();
            $('#confirm-reset-next-dialog').hide();
            $('#confirm-reboot-now-dialog').show();
            $('#confirm-reboot-next-dialog').hide();
        }
    });

    $("#_componentSelectTag option:first-child").attr("selected", "selected");
      $('#_reloadTagTable').click(function(e, data) {
      var value = $('#_componentSelectTag').val();
      if (value !=null ){
            $("#dataTag").block({
            	    message: '<i class="icon-spinner4 spinner"></i>',
            	    timeout: 120000, //unblock after 1 minutes
            	    overlayCSS: {
            	        backgroundColor: '#fff',
            	        opacity: 0.8,
            	        cursor: 'wait'
            	    },
            	    css: {
            	        border: 0,
            	        padding: 0,
            	        backgroundColor: 'transparent'
            	    }
            	});
            var id = $('#deviceId').text();
            var statusDevice = $('#deviceStatus').text();
            if (data != null) {
                $.ajax({
                    type: 'GET',
                    url: "/devices/" + id + "/tags/" + value + "/status/offline",
                    success: function(response) {
                        $("#dataTag").html(response);
                    },
                    error: function() {
                        $("#dataInTag").html();
                    },
                    async: true
                });
            } else {
                $.ajax({
                    type: 'GET',
                    url: "/devices/" + id + "/tags/" + value + "/status/" + statusDevice,
                    success: function(response) {
                        $("#dataTag").html(response);
                    },
                    error: function() {
                        $("#dataInTag").html();
                    },
                    async: true
                });
            }
            }
        })


    $('#_componentSelectTag').change(function() {
        $("#_reloadTagTable").trigger("click", [{
            refreshNow: true
        }]);
    });
    $("#_componentSelectTag").trigger("change");


});

function fillTabTask(value) {
        $("#_tabTask").block({
                	    message: '<i class="icon-spinner9 spinner position-center"></i>',
                	    timeout: 30000, //unblock after 2 seconds
                	    overlayCSS: {
                	        backgroundColor: '#fff',
                	        opacity: 0.8,
                	        cursor: 'wait'
                	    },
                	    css: {
                	        border: 0,
                	        padding: 0,
                	        backgroundColor: 'transparent'
                	    }
                	});
//    $('#_tabTask').append('<i class="icon-spinner9 spinner position-right"></i>')
    var id = $('#deviceId').text();
    $("#tasks_tab").load("/tasks?deviceId=" + id, function(responseText, textStatus) {
    $("#_tabTask").unblock();
          if (textStatus === "error") {
            return "false";
         }
    });

    return false;
}

function fillTabDiagnostic(value) {
            $("#_tabDiagnostic").block({
                	    message: '<i class="icon-spinner9 spinner"></i>',
                	    timeout: 30000, //unblock after 2 seconds
                	    overlayCSS: {
                	        backgroundColor: '#fff',
                	        opacity: 0.8,
                	        cursor: 'wait'
                	    },
                	    css: {
                	        border: 0,
                	        padding: 0,
                	        backgroundColor: 'transparent'
                	    }
                	});
    var id = $('#deviceId').text();
    $("#diagnostic_tab").load("/diagnostic?deviceId=" + id, function(responseText, textStatus) {
    $("#_tabDiagnostic").unblock();
        if (textStatus === "error") {
            return "false";
        }
    });
    return false;
}

function fillTabConfiguration(value)
{
 $("#_tabConfiguration").block({
                	    message: '<i class="icon-spinner9 spinner"></i>',
                	    timeout: 30000, //unblock after 2 seconds
                	    overlayCSS: {
                	        backgroundColor: '#fff',
                	        opacity: 0.8,
                	        cursor: 'wait'
                	    },
                	    css: {
                	        border: 0,
                	        padding: 0,
                	        backgroundColor: 'transparent'
                	    }
                	});
    var id = $('#deviceId').text();
    $("#configuration_tab").load("/configuration?deviceId=" + id, function(responseText, textStatus) {
    $("#_tabConfiguration").unblock();
        if (textStatus === "error") {
            return "false";
        }
    });
    return false;
}